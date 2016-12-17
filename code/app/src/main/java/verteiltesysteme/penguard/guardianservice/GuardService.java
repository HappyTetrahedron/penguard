package verteiltesysteme.penguard.guardianservice;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ListView;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import verteiltesysteme.penguard.GGroupMergeRequestsActivity;
import verteiltesysteme.penguard.GGuardActivity;
import verteiltesysteme.penguard.GPenguinDetailActivity;
import verteiltesysteme.penguard.R;
import verteiltesysteme.penguard.lowLevelNetworking.ListenerCallback;
import verteiltesysteme.penguard.lowLevelNetworking.UDPDispatcher;
import verteiltesysteme.penguard.lowLevelNetworking.UDPListener;
import verteiltesysteme.penguard.protobuf.PenguardProto;

public class GuardService extends Service implements ListenerCallback{

    private final boolean SHUTUP = false;

    final static String EXTRA_IP = "RequestIP";
    final static String EXTRA_PORT = "RequestedPort";
    final static String EXTRA_NAME = "RequestedName";

    // group state: penguins, guardians, and state sequence number
    private final Vector<Penguin> penguins = new Vector<>();
    private final Vector<Guardian> guardians = new Vector<>();
    private int seqNo = 0;
    private final int MERGE_NOTIFICATION_ID = 123;
    private Vector<PenguardProto.PGPMessage> pendingMergeRequests = new Vector<>();

    private Guardian myself = new Guardian();

    // networking infrastructure
    private UDPDispatcher dispatcher;
    private UDPListener listener;
    private DatagramSocket sock;

    // Networking constants
    private final static int SOCKETS_TO_TRY = 5;
    private final static int NETWORK_TIMEOUT = 5000; // Network timeout in ms
    private final static int JOIN_REQ_TIMEOUT = 20 * 1000; // Timeout for join requests. Should be upped to 5 minutes.
    private final static int PING_INTERVAL = 5000;

    private String plsIp = "";
    private int plsPort = 0;

    private final static int NOTIFICATION_ID = 1;

    private RegistrationState regState = new RegistrationState();
    private JoinState joinState = new JoinState();
    private CommitmentState commitState = new CommitmentState();

    private Handler handler;

    BluetoothThread bluetoothThread;
    Thread pingThread;
    BroadcastReceiver bluetoothBroadcastReceiver;
    private MediaPlayer alarmPlayer;

    private PenguinAdapter penguinListAdapter;
    private GuardianAdapter guardianListAdapter;
    private SharedPreferences sharedPref;

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        //get the ip/port from the settings
        updateIpPortFromSettings();

        bluetoothThread = new BluetoothThread(penguins, this);

        handler = new Handler();

        // create ongoing notification needed to be able to make this a foreground service
        Context appContext = getApplicationContext();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext)
                .setContentTitle(getString(R.string.penguard_active))
                .setSmallIcon(R.drawable.icon)
                .setOngoing(true)
                .setColor(ContextCompat.getColor(this, R.color.orange))
                .setContentIntent(PendingIntent.getActivity(appContext, 0,
                        new Intent(appContext, GGuardActivity.class), 0));

        // make this service a foreground service, supplying the notification
        startForeground(NOTIFICATION_ID, builder.build());

        // register the BluetoothThread's BroadcastReceiver
        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothThread.getBroadcastReceiver(), intentFilter);

        // register our own BroadcastReceiver
        bluetoothBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                        == BluetoothAdapter.STATE_OFF) {
                    turnOnBluetooth();
                }
            }
        };
        registerReceiver(bluetoothBroadcastReceiver, intentFilter);

        // create networking infrastructure
        int i = 0;
        boolean creationSuccess = false;
        while (i < SOCKETS_TO_TRY && !creationSuccess){
            try {
                sock = new DatagramSocket(Integer.parseInt(sharedPref.getString(getString(R.string.pref_key_port), getString(R.string.pref_default_port))));
                listener = new UDPListener(sock);
                dispatcher = new UDPDispatcher(sock);
                creationSuccess = true;
            } catch (SocketException e) {
                debug("Error creating socket: " + e.getMessage());
            }
            i++;
        }
        if (!creationSuccess){
            throw new IllegalStateException("Socket creation failed. Probably too many ports are in use. Ask your local sysadmin for help.");
        }
        listener.registerCallback(this);
        listener.start();

        // create penguin array adapter
        penguinListAdapter = new PenguinAdapter(this, penguins);

        //create guard array adapter
        //TODO change the layout
        guardianListAdapter = new GuardianAdapter(this, guardians, myself);

        pingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (dispatcher.isOpen()) {
                    try {
                        Thread.sleep(PING_INTERVAL);
                    } catch (InterruptedException e) {
                        // pass
                    }
                    sendPings();

                    // Check if any penguins have gone missing. If so, ring the alarm.
                    for (Penguin penguin : penguins) {
                        if (!penguin.isSeenByAnyone() && !penguin.isUserNotifiedOfMissing()) {
                            penguinGoneMissing(penguin);
                        }
                    }
                }
            }
        });

        updateUsernameFromSettings();
        guardians.add(myself);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();

        // kill our networking structure
        sock.close();
        try {
            listener.join();
        } catch (InterruptedException e) {
            // do nothing
        }

        bluetoothThread.stopScanning();

        unregisterReceiver(bluetoothThread.getBroadcastReceiver());
        unregisterReceiver(bluetoothBroadcastReceiver);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //detect whether bluetooth is turned on. Turn it on if not.
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            turnOnBluetooth();
        }

        //detect whether thread is already started. Only re-start it if not. See issue #25
        if (!bluetoothThread.isAlive()) {
            bluetoothThread.start();
            debug("BluetoothThread started");
        }
        else{
            debug("BluetoothThread already running");
        }

        if (!pingThread.isAlive()) {
            pingThread.start();
        }

        return super.onStartCommand(intent, flags, startId);

    }



    @Override
    public IBinder onBind(Intent intent) {
        return new PenguinGuardBinder();
    }

    boolean register(String username, LoginCallback callback) {

        if (regState.state != RegistrationState.STATE_UNREGISTERED) return false;

        debug("Registering " + username);
        myself.setName(username);
        regState.registrationProcessStarted(username, callback);

        // create registration message
        PenguardProto.PGPMessage regMessage = PenguardProto.PGPMessage.newBuilder()

                .setType(PenguardProto.PGPMessage.Type.GS_REGISTER)
                .setName(username)
                .build();

        // send it to PLS
        updateIpPortFromSettings(); //just in case the user decided to change it in the meantime
        debug("Sending to PLS: " + plsIp + ":" + plsPort);
        dispatcher.sendPacket(regMessage, plsIp, plsPort);

        // once timeout ticks off, cancel registration iff it is still in progress
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (regState.state != RegistrationState.STATE_REGISTERED) regState.registrationFailed("Connection timed out");
            }
        }, NETWORK_TIMEOUT);
        return true;
    }

    private boolean isGroupMember(Guardian guardian){
        return guardians.contains(guardian);
    }

    public void kickGuardian(Guardian guardian, TwoPhaseCommitCallback callback){
        guardians.remove(guardian);
        List<PenguardProto.PGPGuardian> newGuardians = ListHelper.convertToPGPGuardianList(guardians);
        newGuardians.remove(ListHelper.getPGPGuardianByName(newGuardians, guardian.getName()));

        PenguardProto.Group newGroup = PenguardProto.Group.newBuilder()
                .setSeqNo(seqNo + 1)
                .addAllPenguins(ListHelper.convertToPGPPenguinList(penguins))
                .addAllGuardians(newGuardians)
                .build();

        initiateGroupChange(newGroup, callback);

        //send that guardian a kick message
        debug("kick sent");
        PenguardProto.PGPMessage kick = PenguardProto.PGPMessage.newBuilder()
                .setType(PenguardProto.PGPMessage.Type.GG_KICK)
                .build();
        dispatcher.sendPacket(kick, guardian.getIp(), guardian.getPort());
    }

    private void sendPings(){
        // send ping to server iff registered
        if (regState.state == RegistrationState.STATE_REGISTERED) {
            PenguardProto.PGPMessage ping = PenguardProto.PGPMessage.newBuilder()
                    .setType(PenguardProto.PGPMessage.Type.GS_PING)
                    .setPing(PenguardProto.Ping.newBuilder()
                            .setUuid(regState.uuid.toString()))
                    .setName(myself.getName())
                    .build();

            dispatcher.sendPacket(ping, plsIp, plsPort);
        }

        // send status to other guardians if there are any
        if (!groupIsEmpty()) {
            PenguardProto.PGPMessage status = PenguardProto.PGPMessage.newBuilder()
                    .setType(PenguardProto.PGPMessage.Type.GG_STATUS_UPDATE)
                    .setName(myself.getName())
                    .setGroup(PenguardProto.Group.newBuilder()
                            .setSeqNo(seqNo)
                            .addAllGuardians(ListHelper.convertToPGPGuardianList(guardians))
                            .addAllPenguins(ListHelper.convertToPGPPenguinList(penguins)))
                    .build();

            sendToAllGuardians(status);
        }
    }

    boolean reregister(String username, String uuid, LoginCallback callback){
        if (regState.state != RegistrationState.STATE_UNREGISTERED) return false;

        debug("Re-registering " + username);
        myself.setName(username);
        regState.registrationProcessStarted(username, callback);

        // create registration message
        PenguardProto.PGPMessage reregPing = PenguardProto.PGPMessage.newBuilder()

                .setType(PenguardProto.PGPMessage.Type.GS_PING)
                .setName(username)
                .setPing(PenguardProto.Ping.newBuilder()
                    .setUuid(uuid))
                .build();

        // send it to PLS
        updateIpPortFromSettings(); //just in case the user decided to change it in the meantime
        debug("Sending to PLS: " + plsIp + ":" + plsPort);
        dispatcher.sendPacket(reregPing, plsIp, plsPort);

        // once timeout ticks off, cancel registration iff it is still in progress
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (regState.state != RegistrationState.STATE_REGISTERED) regState.registrationFailed("Connection timed out");
            }
        }, NETWORK_TIMEOUT);
        return true;

    }

    void deregister(String username, String uuid) {
        debug("deregistering " + username);

        PenguardProto.PGPMessage dereg = PenguardProto.PGPMessage.newBuilder()
                .setType(PenguardProto.PGPMessage.Type.GS_DEREGISTER)
                .setName(username)
                .setGoodbye(PenguardProto.GoodBye.newBuilder()
                    .setUuid(uuid))
                .build();

        dispatcher.sendPacket(dereg, plsIp, plsPort);
    }

    boolean joinGroup(final String groupUN, GroupJoinCallback callback){
        if (joinState.state != JoinState.STATE_IDLE) {
            debug("Join not initiated because another one is in progress");
            return false;
        }

        debug("joining the group of: "+ groupUN);
        joinState.startGroupJoin(groupUN, callback);

        //create join message for the server
        PenguardProto.PGPMessage joinMessage = PenguardProto.PGPMessage.newBuilder()

                .setType(PenguardProto.PGPMessage.Type.GS_GROUP_REQ)
                .setName(myself.getName())
                .setGroupReq(PenguardProto.GroupReq.newBuilder().setName(groupUN))
                .build();

        //send to PLS
        dispatcher.sendPacket(joinMessage, plsIp, plsPort);

        //Timeout
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (joinState.groupUN.equals(groupUN) && joinState.state == JoinState.STATE_JOIN_REQ_SENT) {
                    joinState.joinFailed("Your friend did not accept your join.");
                }
            }
        }, JOIN_REQ_TIMEOUT);

        return true;
    }

    private void initiateGroupChange(PenguardProto.Group group, TwoPhaseCommitCallback callback) {
        if (group.getSeqNo() <= seqNo || (commitState.state != CommitmentState.STATE_IDLE)){
            return;
        }
        debug("Initiating commit for new group: " + group);
        // no objections

        if (group.getGuardiansList().size() == 1 && group.getGuardiansList().get(0).getName().equals(myself.getName())) {
            // The new group consists of myself only. Skip the 2 phase commit.
            updateStatus(group);
            callback.onCommit("");
        }
        else {
            // initiate 2 phase commit
            commitState.initiateCommit(group, myself, callback);
            PenguardProto.PGPMessage commit = PenguardProto.PGPMessage.newBuilder()
                    .setName(myself.getName())
                    .setType(PenguardProto.PGPMessage.Type.GG_GRP_CHANGE)
                    .setGroup(group)
                    .build();
            sendCommitToAllGuardians(commit);

            // check upon the commit after a timeout
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkAndCommitOrAbort();
                }
            }, NETWORK_TIMEOUT);
        }
    }

    private void checkAndCommitOrAbort() {
        debug("checking back on previously initiated group change");
        if (commitState.state != CommitmentState.STATE_COMMIT_REQ_SENT) return;

        debug("voteYesReceived: " + commitState.voteYesReceived);
        debug("voteNoReceived: " + commitState.voteNoReceived);

        if (commitState.voteYesReceived && !commitState.voteNoReceived) { // We got at least one yes, and no no
            debug("change is ok, commit");
            sendCommit();
            updateStatus(commitState.groupUpdate);
            commitState.commit();
            if (joinState.state == JoinState.STATE_JOIN_INPROGRESS) {
                joinState.joinSuccessful();
            }
        }
        else {
            debug("change is nok, abort");
            sendAbort();
            commitState.abort();
            if (joinState.state == JoinState.STATE_JOIN_INPROGRESS) {
                joinState.joinFailed(getString(R.string.joinFail));
            }
        }
    }

    private void sendCommit() {
        PenguardProto.PGPMessage commit = PenguardProto.PGPMessage.newBuilder()
                .setName(myself.getName())
                .setType(PenguardProto.PGPMessage.Type.GG_COMMIT)
                .setSeqNo(PenguardProto.SeqNo.newBuilder()
                    .setSeqno(commitState.groupUpdate.getSeqNo()))
                .build();
        sendCommitToAllGuardians(commit);
    }

    private void sendAbort() {
        PenguardProto.PGPMessage abort = PenguardProto.PGPMessage.newBuilder()
                .setName(myself.getName())
                .setType(PenguardProto.PGPMessage.Type.GG_ABORT)
                .setSeqNo(PenguardProto.SeqNo.newBuilder()
                        .setSeqno(commitState.groupUpdate.getSeqNo()))
                .build();
        sendCommitToAllGuardians(abort);
    }
    void removePenguin(String mac, TwoPhaseCommitCallback callback) {
        Penguin p = ListHelper.getPenguinByAddress(penguins, mac);
        if (p != null) {
            if (groupIsEmpty()) { //we're alone, no commitment needed
                penguins.remove(p);
            }
            else {
                List<PenguardProto.PGPPenguin> newPenguins = ListHelper.convertToPGPPenguinList(penguins);
                newPenguins.remove(ListHelper.getPGPPenguinByAddress(newPenguins, mac));

                PenguardProto.Group newGroup = PenguardProto.Group.newBuilder()
                        .setSeqNo(seqNo + 1)
                        .addAllPenguins(newPenguins)
                        .addAllGuardians(ListHelper.convertToPGPGuardianList(guardians))
                        .build();

                initiateGroupChange(newGroup, callback);
            }
            p.disconnect();
        }
    }

    Penguin getPenguin(String mac) {
        return ListHelper.getPenguinByAddress(penguins, mac);
    }

    String getPenguinName(String mac) {
        Penguin p = ListHelper.getPenguinByAddress(penguins, mac);
        if (p != null) return p.getName();
        else return getString(R.string.unknownPenguin);
    }

    String getPenguinSeenByString(String mac) {
        Penguin p = ListHelper.getPenguinByAddress(penguins, mac);
        if (p != null) return p.getSeenByInfo();
        else return getString(R.string.unknown);
    }

    boolean isRegistered(){
        return (regState.state==RegistrationState.STATE_REGISTERED);
    }

    /**
     * Adds a new penguin to the list of tracked penguins. If a penguin with the same HW address is already
     * in the list, the list is not changed.
     * @param penguin Penguin to be added
     */
    void addPenguin(Penguin penguin, TwoPhaseCommitCallback callback){
        if (!penguins.contains(penguin)) {
            debug("Adding penguin " + penguin.getName());
            if (groupIsEmpty()) { // we're alone, don't bother with commits or such
                penguins.add(penguin);
            }
            else {
                PenguardProto.Group newGroup = PenguardProto.Group.newBuilder()
                        .addAllGuardians(ListHelper.convertToPGPGuardianList(guardians))
                        .addAllPenguins(ListHelper.convertToPGPPenguinList(penguins))
                        .addPenguins(PenguardProto.PGPPenguin.newBuilder()
                            .setMac(penguin.getAddress())
                            .setName(penguin.getName())
                            .setSeen(penguin.isSeen()))
                        .setSeqNo(seqNo + 1)
                        .build();
                initiateGroupChange(newGroup, callback);
            }
        }
        debug("penguin already there");
    }

    private boolean groupIsEmpty() {
        return guardians.size() <= 1;
    }

    void subscribeListViewToPenguinAdapter(ListView listView) {
        listView.setAdapter(penguinListAdapter);
    }

    void subscribeListViewToGuardianAdapter(ListView listView){
       listView.setAdapter(guardianListAdapter);
    }

    private void sendToAllGuardians(PenguardProto.PGPMessage message) {
        for (Guardian g : guardians) {
            if (!g.equals(myself)) {
                dispatcher.sendPacket(message, g.getIp(), g.getPort());
            }
        }
    }

    private void sendCommitToAllGuardians(PenguardProto.PGPMessage message) {
        List<PenguardProto.PGPGuardian> pgpGuardians = commitState.groupUpdate.getGuardiansList();
        for (PenguardProto.PGPGuardian g : pgpGuardians) {
            if (!g.getName().equals(myself.getName())){
                dispatcher.sendPacket(message, g.getIp(), g.getPort());
            }
        }
    }

    public Guardian getMyself(){
        return myself;
    }

    @Override
    public void onReceive(PenguardProto.PGPMessage parsedMessage, InetAddress address, int port) {
        if (parsedMessage.getType() != PenguardProto.PGPMessage.Type.SG_ACK
                && parsedMessage.getType() != PenguardProto.PGPMessage.Type.GG_ACK
                && parsedMessage.getType() != PenguardProto.PGPMessage.Type.GG_STATUS_UPDATE)
            debug(parsedMessage.toString());

        //If the sender was one of the guardians from the group, update his state.
        Guardian sender = ListHelper.getGuardianByName(guardians, parsedMessage.getName());
        if (sender != null) {
            sender.setAddress(address);
            sender.setPort(port);
            sender.updateTime();
        }

        switch(parsedMessage.getType()){
            case SG_ERR:
                serverErrReceived(parsedMessage);
                break;
            case SG_ACK:
                serverAckReceived(parsedMessage);
                break;
            case SG_MERGE_REQ:
                mergeReqReceived(parsedMessage);
                break;
            case GG_STATUS_UPDATE:
                if (sender != null) {
                    statusUpdateReceived(parsedMessage, address.getHostName(), port);
                } // we only want to accept status updates from group members not formally kicked out people
                break;
            case GG_GRP_CHANGE:
                grpChangeReceived(parsedMessage, address.getHostName(), port);
                break;
            case GG_COMMIT:
                commitReceived(parsedMessage);
                break;
            case GG_ABORT:
                abortReceived(parsedMessage);
                break;
            case GG_VOTE_YES:
                voteYesReceived(parsedMessage);
                break;
            case GG_VOTE_NO:
                voteNoReceived(parsedMessage);
                break;
            case GG_GRP_INFO:
                grpInfoReceived(parsedMessage);
                break;
            case GG_ACK:
                // ignore
                break;
            case GG_KICK:
                debug("recieved a kick");
                kickRecieved(parsedMessage);
                break;
            default:
                debug("Packet with unexpected type arrived");
                break;
        }
    }

    private void kickRecieved(PenguardProto.PGPMessage message) {
        //TODO
        guardians.removeAllElements();
        guardians.add(myself);
        penguins.removeAllElements();
        List<PenguardProto.PGPGuardian> onlyMe = ListHelper.convertToPGPGuardianList(guardians); //i'm the only member in the new group as i was kicked out of the old one
        List<PenguardProto.PGPPenguin> noPenguins = ListHelper.convertToPGPPenguinList(penguins); //i have no penguins
        int newSeqNo = Math.max(seqNo, message.getGroup().getSeqNo()) + 1; //Should we leave it like this or change to 0?
        PenguardProto.Group newGroup = PenguardProto.Group.newBuilder()
                .setSeqNo(newSeqNo)
                .addAllGuardians(onlyMe)
                .addAllPenguins(noPenguins)
                .build();

        debug("======MY GROUP AFTER KICK=======");
        for (Guardian g : guardians ) debug(g.getName());
        for (Penguin p : penguins) debug(p.getName());
        debug("======END MY GROUP AFTER KICK=======");

        //callback does nothing
        TwoPhaseCommitCallback NotReallyUsefulCallback = new TwoPhaseCommitCallback() {
            @Override
            public void onCommit(String message) {
                debug(message);
            }

            @Override
            public void onAbort(String error) {
                debug(error);
            }
        };
        initiateGroupChange(newGroup, NotReallyUsefulCallback);


    }

    private void serverAckReceived(PenguardProto.PGPMessage message) {
        if (regState.state == RegistrationState.STATE_REGISTRATION_IN_PROGRESS) {
            regState.registrationSucceeded(UUID.fromString(message.getAck().getUuid()));
        }

        // update my information about myself.
        myself.setPort(message.getAck().getPort());
        myself.setIp(message.getAck().getIp());
    }

    private void serverErrReceived(PenguardProto.PGPMessage message) {
        if (regState.state == RegistrationState.STATE_REGISTRATION_IN_PROGRESS) {
            // error during registration
            regState.registrationFailed(message.getError().getError());
        }
    }

    private void grpInfoReceived(PenguardProto.PGPMessage message){
        if (CommitmentState.STATE_COMMIT_REQ_SENT == commitState.state && message.getName().equals(commitState.initiantName)){
            debug("dublicate message, do nothing");
            return;
        }


        List<PenguardProto.PGPGuardian> mergedGuardians = ListHelper.mergeGuardiansList(
                ListHelper.convertToPGPGuardianList(guardians),
                message.getGroupInfo().getGroup().getGuardiansList()
        );
        List<PenguardProto.PGPPenguin> mergedPenguins = ListHelper.mergePenguinLists(
                ListHelper.convertToPGPPenguinList(penguins),
                message.getGroupInfo().getGroup().getPenguinsList()
        );
        int newSeqNo = Math.max(seqNo, message.getGroupInfo().getGroup().getSeqNo()) + 1;

        debug("======MY GROUP=======");
        for (Guardian g : guardians ) debug(g.getName());
        for (Penguin p : penguins) debug(p.getName());
        debug("======END MY GROUP=======");

        debug("======OTHER GROUP=======");
        for (PenguardProto.PGPGuardian g : message.getGroupInfo().getGroup().getGuardiansList()) debug(g.toString());
        for (PenguardProto.PGPPenguin g : message.getGroupInfo().getGroup().getPenguinsList()) debug(g.toString());
        debug("======END OTHER GROUP=======");

        debug("======NEW GROUP=======");
        for (PenguardProto.PGPGuardian g : mergedGuardians) debug(g.toString());
        for (PenguardProto.PGPPenguin g : mergedPenguins) debug(g.toString());
        debug("======END NEW GROUP=======");

             PenguardProto.Group newGroup = PenguardProto.Group.newBuilder()
                .setSeqNo(newSeqNo)
                .addAllGuardians(mergedGuardians)
                .addAllPenguins(mergedPenguins)
                .build();
        debug("Starting big big commit for group merge.");
        if (newGroup.getSeqNo() <= seqNo || (commitState.state != CommitmentState.STATE_IDLE)){
            joinState.joinFailed(getString(R.string.toast_merge_failed_busy));
        }
        else {
            joinState.joinReqAccepted();

            /*
                InitiateGroupChange needs a callback and since we don't receive one from the
                request we just make something up. Could be null but why not make an example of
                what it could look like.
             */

            TwoPhaseCommitCallback NotReallyUsefulCallback = new TwoPhaseCommitCallback() {
                @Override
                public void onCommit(String message) {
                    debug(message);
                }

                @Override
                public void onAbort(String error) {
                    debug(error);
                }
            };
            initiateGroupChange(newGroup, NotReallyUsefulCallback);
        }
    }

    private void voteNoReceived(PenguardProto.PGPMessage message){
        if (commitState.state == CommitmentState.STATE_COMMIT_REQ_SENT
                && message.getSeqNo().getSeqno() == commitState.groupUpdate.getSeqNo()) {
            commitState.voteNoReceived();
        }
    }

    private void voteYesReceived(PenguardProto.PGPMessage message){
        if (commitState.state == CommitmentState.STATE_COMMIT_REQ_SENT
                && message.getSeqNo().getSeqno() == commitState.groupUpdate.getSeqNo()) {
            debug("Accepting a yes vote");
            commitState.voteYesReceived();
        }
        else {
            debug("Not accepting yes vote");
        }
        debug("state is " + commitState.state );
        debug("commit no is " + commitState.groupUpdate.getSeqNo() +", received no is " + message.getSeqNo().getSeqno());
    }

    private void abortReceived(PenguardProto.PGPMessage message){
        if (joinState.state == joinState.STATE_JOIN_INPROGRESS) {
            joinState.joinFailed(getString(R.string.toast_merge_failed_abort));
        }

        if (commitState.state == CommitmentState.STATE_VOTED_YES
                && commitState.initiantName.equals(message.getName())
                && commitState.groupUpdate.getSeqNo() == message.getSeqNo().getSeqno()) {
            debug("State update " + message.getSeqNo().getSeqno() + " aborted");
            commitState.reset();
        }
    }

    private void commitReceived(PenguardProto.PGPMessage message){
        if (joinState.state == JoinState.STATE_JOIN_INPROGRESS) {
            joinState.joinSuccessful();
        }

        if (commitState.state == CommitmentState.STATE_VOTED_YES
                && commitState.initiantName.equals(message.getName())
                && commitState.groupUpdate.getSeqNo() == message.getSeqNo().getSeqno()) {
            debug("Committing state number " + message.getSeqNo().getSeqno());
            updateStatus(commitState.groupUpdate);
            commitState.reset();
        }
    }

    private void grpChangeReceived(PenguardProto.PGPMessage message, String host, int port){
        if (commitState.state != CommitmentState.STATE_IDLE) { //We're already busy with a different commit
            debug("voting no because already in a different commit");
            voteNo(message, host, port);
        }
        else if (message.getGroup().getSeqNo() <= seqNo) { // This update is older than our state. Heck no.
            debug("voting no because sequence number too small, " + message.getGroup().getSeqNo() + "<" + seqNo);
            voteNo(message, host, port);
        }
        else { // No objections
            debug("Voted yes on status update " + message.getGroup().getSeqNo());
            commitState.commitReqReceived(message, host, port);
            voteYes(message, host, port);
        }

    }

    private void voteYes(PenguardProto.PGPMessage message, String host, int port){
        PenguardProto.PGPMessage vote = PenguardProto.PGPMessage.newBuilder()
                .setType(PenguardProto.PGPMessage.Type.GG_VOTE_YES)
                .setName(myself.getName())
                .setSeqNo(PenguardProto.SeqNo.newBuilder()
                        .setSeqno(message.getGroup().getSeqNo()))
                .build();
        dispatcher.sendPacket(vote, host, port);
    }

    private void voteNo(PenguardProto.PGPMessage message, String host, int port){
        PenguardProto.PGPMessage vote = PenguardProto.PGPMessage.newBuilder()
                .setType(PenguardProto.PGPMessage.Type.GG_VOTE_NO)
                .setName(myself.getName())
                .setSeqNo(PenguardProto.SeqNo.newBuilder()
                        .setSeqno(message.getGroup().getSeqNo()))
                .build();
        dispatcher.sendPacket(vote, host, port);
    }

    private void statusUpdateReceived(PenguardProto.PGPMessage message, String ip, int port){
        // Reply with an ACK
        PenguardProto.PGPMessage ack = PenguardProto.PGPMessage.newBuilder()
                .setType(PenguardProto.PGPMessage.Type.GG_ACK)
                .setName(myself.getName())
                .build();
        dispatcher.sendPacket(ack, ip, port);

        updateSeenStatus(ListHelper.getGuardianByName(guardians, message.getName()), message.getGroup().getPenguinsList());

        if (message.getGroup().getSeqNo() > seqNo) { // The other guardian's status is newer!
            updateStatus(message.getGroup());
        }
    }

    private void penguinGoneMissing(Penguin penguin) {
        Intent resultIntent = new Intent(this, GPenguinDetailActivity.class);
        resultIntent.putExtra(GPenguinDetailActivity.EXTRA_PENGUIN_MAC, penguin.getAddress());
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(GGroupMergeRequestsActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultpendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build penguin-missing notification.
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon)
                .setColor(ContextCompat.getColor(this, R.color.orange))
                .setContentTitle(getText(R.string.notification_penguin_missing_title))
                .setContentIntent(resultpendingIntent)
                .setAutoCancel(true);
        if (Math.random() < 0.98) {
            mBuilder.setContentText(penguin.getName() + " " + getText(R.string.notification_penguin_missing));
        }
        else {
            mBuilder.setContentText(penguin.getName() + " " + getText(R.string.notification_penguin_missing2));
        }

        // Activate notification.
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(MERGE_NOTIFICATION_ID, mBuilder.build());

        if (alarmPlayer == null) {
            alarmPlayer = MediaPlayer.create(this, R.raw.alarm);
        }
        alarmPlayer.setVolume(1.0f, 1.0f);
        alarmPlayer.setLooping(true);
        debug("alaaarm");
        alarmPlayer.start();
    }

    protected void stopAlarm(Penguin penguin) {
        if (alarmPlayer != null) {
            alarmPlayer.setLooping(false);
        }
        penguin.setUserNotifiedOfMissing(true);
    }

    private void mergeReqReceived(PenguardProto.PGPMessage message){
        //set Intent such that the user is directed to the MergeActivity
        Intent resultIntent = new Intent(this, GGroupMergeRequestsActivity.class);
        resultIntent.putExtra(EXTRA_IP, message.getMergeReq().getIp());
        resultIntent.putExtra(EXTRA_NAME, message.getMergeReq().getName());
        resultIntent.putExtra(EXTRA_PORT, message.getMergeReq().getPort());
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(GGroupMergeRequestsActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultpendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


        //builds notification and sends it to the system. thanks to the id, it will be updated when this is called again
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon)
                .setColor(ContextCompat.getColor(this, R.color.orange))
                .setContentTitle(getText(R.string.notification_merge_request_title))
                .setContentText(getText(R.string.notification_merge_request_text))
                .setContentIntent(resultpendingIntent)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setAutoCancel(true);


        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(MERGE_NOTIFICATION_ID, mBuilder.build());
    }

    public void sendGroupTo(String ip, int port){
        // create Group message
        Vector<PenguardProto.PGPPenguin> pgpPenguinVector = ListHelper.convertToPGPPenguinList(penguins);
        Vector<PenguardProto.PGPGuardian> pgpGuardianVector = ListHelper.convertToPGPGuardianList(guardians);
        debug("packing message to send to group");
        PenguardProto.Group group = PenguardProto.Group.newBuilder()

                .setSeqNo(seqNo)
                // add the others
                .addAllGuardians(pgpGuardianVector)
                .addAllPenguins(pgpPenguinVector)
                .build();

        PenguardProto.GroupInfo groupInfo = PenguardProto.GroupInfo.newBuilder()
                .setRecieverIP(ip)
                .setRecieverPort(port) //these are ip and port the server need to relay the message to
                .setGroup(group)
                .build();

        PenguardProto.PGPMessage groupMessage = PenguardProto.PGPMessage.newBuilder()

                .setType(PenguardProto.PGPMessage.Type.GG_GRP_INFO)
                .setGroupInfo(groupInfo)
                .setName(myself.getName())
                .build();

        debug("about to send group to: " + ip + ":" + port);
        dispatcher.sendPacket(groupMessage, ip, port);

        //also sending it to the server for relay purposes
        dispatcher.sendPacket(groupMessage, plsIp, plsPort);
    }

    /**
     * updates the 'seen by' status of all penguins regarding a specific guardian
     * @param guardian The guardian for which the status is updated
     * @param penguinStatus The list of PGPPenguins that guardian sent to us
     */
    private void updateSeenStatus(Guardian guardian, List<PenguardProto.PGPPenguin> penguinStatus) {
        for (PenguardProto.PGPPenguin protoPenguin : penguinStatus) {
            Penguin p = ListHelper.getPenguinByAddress(penguins, protoPenguin.getMac());
            if (p != null) p.setSeenBy(guardian, protoPenguin.getSeen());
        }
    }

    private void updateStatus(PenguardProto.Group group) {
        ListHelper.copyGuardianListFromProtobufList(guardians, group.getGuardiansList());
        ListHelper.copyPenguinListFromProtobufList(penguins, group.getPenguinsList());
        this.seqNo = group.getSeqNo();
    }

    private void updateIpPortFromSettings(){
        plsIp = sharedPref.getString(getString(R.string.pref_key_server_address), getString(R.string.pref_default_server));
        String plsPortstring = sharedPref.getString(getString(R.string.pref_key_server_port), getString(R.string.pref_default_server_port));
        plsPort = Integer.parseInt(plsPortstring);
    }

    private void updateUsernameFromSettings() {
        String user = sharedPref.getString(getString(R.string.pref_key_username), getString(R.string.pref_default_username));
        myself.setName(user);
    }

    private void turnOnBluetooth() {
        Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        enableBluetoothIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(enableBluetoothIntent);
    }

    // Binder used for communication with the service. Do not use directly. Use GuardianServiceConnection instead.
    class PenguinGuardBinder extends Binder {

        GuardService getService() {
            return GuardService.this;
        }
    }

    private void debug(String msg) {
        if (! SHUTUP) Log.d("GuardService", msg);
    }

}
