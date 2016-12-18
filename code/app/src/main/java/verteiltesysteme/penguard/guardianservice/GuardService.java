package verteiltesysteme.penguard.guardianservice;

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
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
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


    // Extras
    final static String EXTRA_IP = "RequestIP";
    final static String EXTRA_PORT = "RequestedPort";
    final static String EXTRA_NAME = "RequestedName";

    // Notification IDs
    private final static int SERVICE_NOTIFICATION_ID = 1;
    private final static int MERGE_NOTIFICATION_ID = 123;
    final static int ALARM_NOTIFICATION_ID = 124;

    // Networking constants
    private final static int SOCKETS_TO_TRY = 5;
    private final static int NETWORK_TIMEOUT = 5000; // Network timeout in ms
    private final static int JOIN_REQ_TIMEOUT = 20 * 1000; // Timeout for join requests.
    private final static int PING_INTERVAL = 5000;
    private final static int GUARDIAN_SEEN_TIMEOUT = 30 * 1000; // After this many milliseconds, we assume that we don't have connection to a guardian.

    // group state: penguins, guardians, and state sequence number
    private final PenguinList penguins = new PenguinList(this);
    private final Vector<Guardian> guardians = new Vector<>();
    private int seqNo = 0;

    // Reference to the guardian object corresponding to myself
    private Guardian myself = new Guardian();

    // networking infrastructure
    private UDPDispatcher dispatcher;
    private UDPListener listener;
    private DatagramSocket sock;

    // PLS data
    private String plsIp = "";
    private int plsPort = 0;
    private long plsLastSeen = 0;

    // State classes
    private RegistrationState regState = new RegistrationState();
    private JoinState joinState = new JoinState();
    private CommitmentState commitState = new CommitmentState();

    // Adapters for guardian and penguin lists
    private PenguinAdapter penguinListAdapter;
    private GuardianAdapter guardianListAdapter;

    // Handler for regular tasks
    private Handler handler;

    // Worker threads
    BluetoothThread bluetoothThread;
    Thread jobThread;

    BroadcastReceiver bluetoothBroadcastReceiver;

    private MediaPlayer alarmPlayer;

    private SharedPreferences sharedPref;

    // Callback that cancels alarm and notification for a given penguin
    private PenguinSeenCallback penguinSeenCallback = new PenguinSeenCallback() {
        @Override
        public void penguinRediscovered(Penguin p) {
            cancelAlarmAndNotificationForPenguin(p);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        //get the ip/port from the settings
        updateIpPortFromSettings();

        bluetoothThread = new BluetoothThread(penguins, this);

        handler = new Handler();

        // Create ongoing notification needed to be able to make this a foreground service
        Intent notificationIntent = new Intent(getApplicationContext(), GGuardActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Context appContext = getApplicationContext();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext)
                .setContentTitle(getString(R.string.penguard_active))
                .setSmallIcon(R.drawable.icon)
                .setOngoing(true)
                .setColor(ContextCompat.getColor(this, R.color.orange))
                .setContentIntent(PendingIntent.getActivity(appContext, 0,
                        notificationIntent, 0));

        // make this service a foreground service, supplying the notification
        startForeground(SERVICE_NOTIFICATION_ID, builder.build());

        // interfaceRegister the BluetoothThread's BroadcastReceiver
        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothThread.getBroadcastReceiver(), intentFilter);

        // interfaceRegister our own BroadcastReceiver
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
        penguinListAdapter = new PenguinAdapter(this, penguins.getUnmodifiableList());

        //create guard array adapter
        guardianListAdapter = new GuardianAdapter(this, guardians, myself);

        jobThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (dispatcher.isOpen()) {
                    try {
                        Thread.sleep(PING_INTERVAL);
                    } catch (InterruptedException e) {
                        // pass
                    }
                    doJobSendPings();
                    doJobCheckPenguinTimeouts();
                    doJobCheckBadNat();
                }
                onJobThreadEnded();
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
            // pass
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

        if (!bluetoothThread.isAlive()) {
            bluetoothThread.start();
            debug("BluetoothThread started");
        }
        else{
            debug("BluetoothThread already running");
        }

        if (!jobThread.isAlive()) {
            jobThread.start();
        }

        return super.onStartCommand(intent, flags, startId);

    }



    @Override
    public IBinder onBind(Intent intent) {
        return new PenguinGuardBinder();
    }

// =================================================================================================
//                  JOBS TO BE DONE REGULARLY
// =================================================================================================

    private void doJobCheckPenguinTimeouts() {
        // Check if any penguins have gone missing. If so, ring the alarm.
        for (Penguin penguin : penguins) {
            if (penguin.needsAlarm()) {
                penguinGoneMissing(penguin);
            }
        }
    }

    private void doJobCheckBadNat() {
        if (System.currentTimeMillis() - plsLastSeen < GUARDIAN_SEEN_TIMEOUT
                && !groupIsEmpty()
                && !anyGuardianConnected()) {
            debug("Can reach server but no other guardian, assuming bad nat");
            myself.setBadNat(true);
        }
    }

    private void doJobSendPings(){
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
            PenguardProto.PGPMessage.Builder status = PenguardProto.PGPMessage.newBuilder()
                    .setType(PenguardProto.PGPMessage.Type.GG_STATUS_UPDATE)
                    .setName(myself.getName())
                    .setGroup(PenguardProto.Group.newBuilder()
                            .setSeqNo(seqNo)
                            .addAllGuardians(ListHelper.convertToPGPGuardianList(guardians))
                            .addAllPenguins(ListHelper.convertToPGPPenguinList(penguins)));

            sendToAllGuardians(status);
        }
    }

// =================================================================================================
//                 INTERNAL CALLBACK FUNCTIONS
// =================================================================================================

    private void onJobThreadEnded() {
        debug("Job thread ended, cleaning up...");
        // Stop playing the alarm if one is playing.
        if(alarmPlayer != null && alarmPlayer.isPlaying()) {
            alarmPlayer.stop();
            Looper.prepare();
        }

        // Remove any leftover alarm notifications.
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        for (Penguin p : penguins) {
            String mac = p.getAddress();
            notificationManager.cancel(p.getNotificationId());
        }
    }

    private void onIpChanged() {
        // We could be in a good NAT again.
        debug("IP changed, assuming new NAT is good");
        myself.setBadNat(false);
    }

// =================================================================================================
//                  INTERFACE FUNCTIONS, CALLED FROM SERVICE CONNECTION
// =================================================================================================

    boolean interfaceRegister(String username, LoginCallback callback) {

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

    boolean interfaceReregister(String username, String uuid, LoginCallback callback){
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

    void interfaceDeregister(String username, String uuid) {
        debug("deregistering " + username);

        PenguardProto.PGPMessage dereg = PenguardProto.PGPMessage.newBuilder()
                .setType(PenguardProto.PGPMessage.Type.GS_DEREGISTER)
                .setName(username)
                .setGoodbye(PenguardProto.GoodBye.newBuilder()
                    .setUuid(uuid))
                .build();

        dispatcher.sendPacket(dereg, plsIp, plsPort);
    }

    boolean interfaceJoinGroup(final String groupUN, GroupJoinCallback callback){
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
                    joinState.joinFailed("");
                }
            }
        }, JOIN_REQ_TIMEOUT);

        return true;
    }

    PenguinSeenCallback interfaceGetPenguinSeenCallback(){
        return penguinSeenCallback;
    }

    void interfaceKickGuardian(Guardian guardian, TwoPhaseCommitCallback callback){
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
        PenguardProto.PGPMessage.Builder kick = PenguardProto.PGPMessage.newBuilder()
                .setType(PenguardProto.PGPMessage.Type.GG_KICK);
        sendToGuardian(kick, guardian);
    }

    void interfaceRemovePenguin(String mac, TwoPhaseCommitCallback callback) {
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

    Penguin interfaceGetPenguin(String mac) {
        return ListHelper.getPenguinByAddress(penguins, mac);
    }

    String interfaceGetPenguinSeenByString(String mac) {
        Penguin p = ListHelper.getPenguinByAddress(penguins, mac);
        if (p != null) return p.getSeenByInfo();
        else return getString(R.string.unknown);
    }

    boolean interfaceIsRegistered(){
        return (regState.state==RegistrationState.STATE_REGISTERED);
    }

    void interfaceStopAlarm(Penguin penguin) {
        debug("Stopped alarm.");
        penguin.setUserNotifiedOfMissing(true);

        if (alarmPlayer != null) {
            if (!isAnyPenguinSoundingAlarm()){
                alarmPlayer.setLooping(false);
            }
        }
    }

    /**
     * Adds a new penguin to the list of tracked penguins. If a penguin with the same HW address is already
     * in the list, the list is not changed.
     * @param penguin Penguin to be added
     */
    void interfaceAddPenguin(Penguin penguin, TwoPhaseCommitCallback callback){
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
    }

    void interfaceSubscribeListViewToPenguinAdapter(ListView listView) {
        listView.setAdapter(penguinListAdapter);
    }

    void interfaceSubscribeListViewToGuardianAdapter(ListView listView){
       listView.setAdapter(guardianListAdapter);
    }

    Guardian interfaceGetMyself(){
        return myself;
    }


// =================================================================================================
//                 RECEIVED MESSAGE HANDLER FUNCTIONS
// =================================================================================================

    @Override
    public void onReceive(PenguardProto.PGPMessage parsedMessage, InetAddress address, int port) {

        // debug only the not-so-common messages
        if (parsedMessage.getType() != PenguardProto.PGPMessage.Type.SG_ACK
                && parsedMessage.getType() != PenguardProto.PGPMessage.Type.GG_ACK
                && parsedMessage.getType() != PenguardProto.PGPMessage.Type.GG_STATUS_UPDATE)
            debug(parsedMessage.toString());

        //If the sender was one of the guardians from the group, update his timestamp.
        Guardian sender = ListHelper.getGuardianByName(guardians, parsedMessage.getName());
        if (sender != null) {
            sender.updateTime();
        }

        switch(parsedMessage.getType()){
            case SG_ERR:
                onServerErrReceived(parsedMessage);
                break;
            case SG_ACK:
                onServerAckReceived(parsedMessage);
                break;
            case SG_MERGE_REQ:
                onMergeReqReceived(parsedMessage);
                break;
            case GG_STATUS_UPDATE:
                if (sender != null) {
                    onStatusUpdateReceived(parsedMessage);
                } // we only want to accept status updates from group members not formally kicked out people
                break;
            case GG_GRP_CHANGE:
                onGrpChangeReceived(parsedMessage);
                break;
            case GG_COMMIT:
                onCommitReceived(parsedMessage);
                break;
            case GG_ABORT:
                onAbortReceived(parsedMessage);
                break;
            case GG_VOTE_YES:
                onVoteYesReceived(parsedMessage);
                break;
            case GG_VOTE_NO:
                onVoteNoReceived(parsedMessage);
                break;
            case GG_GRP_INFO:
                onGrpInfoReceived(parsedMessage);
                break;
            case GG_ACK:
                // ignore
                break;
            case GG_KICK:
                onKickReceived(parsedMessage);
                break;
            default:
                debug("Packet with unexpected type arrived");
                break;
        }
    }

    private void onKickReceived(PenguardProto.PGPMessage message) {
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

        initiateGroupChange(newGroup, null);
    }

    private void onServerAckReceived(PenguardProto.PGPMessage message) {
        plsLastSeen = System.currentTimeMillis();
        if (regState.state == RegistrationState.STATE_REGISTRATION_IN_PROGRESS) {
            regState.registrationSucceeded(UUID.fromString(message.getAck().getUuid()));
        }

        // update my information about myself.
        if (myself.getIp() != null && !myself.getIp().equals(message.getAck().getIp())) {
            onIpChanged();
        }
        myself.setPort(message.getAck().getPort());
        myself.setIp(message.getAck().getIp());
    }

    private void onServerErrReceived(PenguardProto.PGPMessage message) {
        if (regState.state == RegistrationState.STATE_REGISTRATION_IN_PROGRESS) {
            // error during registration
            regState.registrationFailed(message.getError().getError());
        }

        if (joinState.state == JoinState.STATE_JOIN_REQ_SENT) {
            // error during join
            joinState.joinFailed(message.getError().getError());
        }
    }

    private void onGrpInfoReceived(PenguardProto.PGPMessage message){
        if (CommitmentState.STATE_COMMIT_REQ_SENT == commitState.state){
            // duplicate message, do nothing
            debug("duplicate message");
            return;
        }


        List<PenguardProto.PGPGuardian> mergedGuardians = ListHelper.mergeGuardiansList(
                ListHelper.convertToPGPGuardianList(guardians),
                message.getGroup().getGuardiansList()
        );
        List<PenguardProto.PGPPenguin> mergedPenguins = ListHelper.mergePenguinLists(
                ListHelper.convertToPGPPenguinList(penguins),
                message.getGroup().getPenguinsList()
        );
        int newSeqNo = Math.max(seqNo, message.getGroup().getSeqNo()) + 1;

             PenguardProto.Group newGroup = PenguardProto.Group.newBuilder()
                .setSeqNo(newSeqNo)
                .addAllGuardians(mergedGuardians)
                .addAllPenguins(mergedPenguins)
                .build();
        debug("Starting big big commit for group merge.");


        debug(" committment state is " + commitState.state);
        if ((commitState.state != CommitmentState.STATE_IDLE)){
            joinState.joinFailed(getString(R.string.toast_merge_failed_busy));
        }

        else {
            joinState.joinReqAccepted();

            TwoPhaseCommitCallback callback = new TwoPhaseCommitCallback() {
                @Override
                public void onCommit(String message) {
                    joinState.joinSuccessful();
                }

                @Override
                public void onAbort(String error) {
                    joinState.joinFailed(error);
                    if (groupIsEmpty()) {
                        // We're alone, and we couldn't commit. One possible reason is a bad NAT.
                        debug("alone in group and commit failed - assuming bad nat");
                        myself.setBadNat(true);
                    }
                    debug(error);
                }
            };

            initiateGroupChange(newGroup, callback);
        }
    }

    private void onVoteNoReceived(PenguardProto.PGPMessage message){
        if (commitState.state == CommitmentState.STATE_COMMIT_REQ_SENT
                && message.getSeqNo().getSeqno() == commitState.groupUpdate.getSeqNo()) {
            commitState.voteNoReceived();
        }
    }

    private void onVoteYesReceived(PenguardProto.PGPMessage message){
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

    private void onAbortReceived(PenguardProto.PGPMessage message){
        if (joinState.state == joinState.STATE_JOIN_INPROGRESS) {
            joinState.joinFailed(getString(R.string.toast_merge_failed_abort));
        }

        if (commitState.state == CommitmentState.STATE_VOTED_YES
                && commitState.initiant.getName().equals(message.getName())
                && commitState.groupUpdate.getSeqNo() == message.getSeqNo().getSeqno()) {
            debug("State update " + message.getSeqNo().getSeqno() + " aborted");
            commitState.reset();
        }
    }

    private void onCommitReceived(PenguardProto.PGPMessage message){
        if (joinState.state == JoinState.STATE_JOIN_INPROGRESS) {
            joinState.joinSuccessful();
        }

        if (commitState.state == CommitmentState.STATE_VOTED_YES
                && commitState.initiant.getName().equals(message.getName())
                && commitState.groupUpdate.getSeqNo() == message.getSeqNo().getSeqno()) {
            debug("Committing state number " + message.getSeqNo().getSeqno());
            updateStatus(commitState.groupUpdate);
            commitState.reset();
        }
    }

    private void onGrpChangeReceived(PenguardProto.PGPMessage message){
        PenguardProto.PGPGuardian initiant = ListHelper.getPGPGuardianByName(
                message.getGroup().getGuardiansList(),
                message.getName()
        );

        if (initiant == null) return; // initiant is not in group. abort.

        if (commitState.state != CommitmentState.STATE_IDLE) { //We're already busy with a different commit
            debug("voting no because already in a different commit");
            voteNo(message, initiant);
        }
        else if (message.getGroup().getSeqNo() <= seqNo) { // This update is older than our state. Heck no.
            debug("voting no because sequence number too small, " + message.getGroup().getSeqNo() + "<" + seqNo);
            voteNo(message, initiant);
        }
        else { // No objections
            debug("Voted yes on status update " + message.getGroup().getSeqNo());
            commitState.commitReqReceived(message, initiant);
            voteYes(message, initiant);
        }

    }

    private void onMergeReqReceived(PenguardProto.PGPMessage message){
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

    private void onStatusUpdateReceived(PenguardProto.PGPMessage message){
        // Reply with an ACK
        PenguardProto.PGPMessage.Builder ack = PenguardProto.PGPMessage.newBuilder()
                .setType(PenguardProto.PGPMessage.Type.GG_ACK)
                .setName(myself.getName());

        sendToGuardian(ack, ListHelper.getGuardianByName(guardians, message.getName()));

        updateSeenStatus(ListHelper.getGuardianByName(guardians, message.getName()), message.getGroup().getPenguinsList());
        updateGuardianStatus(ListHelper.getPGPGuardianByName(message.getGroup().getGuardiansList(), message.getName()));

        if (message.getGroup().getSeqNo() > seqNo) { // The other guardian's status is newer!
            updateStatus(message.getGroup());
        }
    }

// =================================================================================================
//                 PROTOCOL RELATED HELPER FUNCTIONS
// =================================================================================================


    private void voteYes(PenguardProto.PGPMessage message, PenguardProto.PGPGuardian initiant){
        PenguardProto.PGPMessage.Builder vote = PenguardProto.PGPMessage.newBuilder()
                .setType(PenguardProto.PGPMessage.Type.GG_VOTE_YES)
                .setName(myself.getName())
                .setSeqNo(PenguardProto.SeqNo.newBuilder()
                        .setSeqno(message.getGroup().getSeqNo()));
        sendToGuardian(vote, initiant);
    }

    private void voteNo(PenguardProto.PGPMessage message, PenguardProto.PGPGuardian initiant){
        PenguardProto.PGPMessage.Builder vote = PenguardProto.PGPMessage.newBuilder()
                .setType(PenguardProto.PGPMessage.Type.GG_VOTE_NO)
                .setName(myself.getName())
                .setSeqNo(PenguardProto.SeqNo.newBuilder()
                        .setSeqno(message.getGroup().getSeqNo()));
        sendToGuardian(vote, initiant);
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
            commitState.initiateCommit(group, myself.toProto(), callback);
            PenguardProto.PGPMessage.Builder commit = PenguardProto.PGPMessage.newBuilder()
                    .setName(myself.getName())
                    .setType(PenguardProto.PGPMessage.Type.GG_GRP_CHANGE)
                    .setGroup(group);
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

    private void sendToAllGuardians(PenguardProto.PGPMessage.Builder message) {
        for (Guardian g : guardians) {
            if (!g.equals(myself)) {
                sendToGuardian(message, g);
            }
        }
    }

    private void sendCommitToAllGuardians(PenguardProto.PGPMessage.Builder message) {
        List<PenguardProto.PGPGuardian> pgpGuardians = commitState.groupUpdate.getGuardiansList();
        for (PenguardProto.PGPGuardian g : pgpGuardians) {
            if (!g.getName().equals(myself.getName())){
                sendToGuardian(message, g);
            }
        }
    }

    void sendGroupTo(String ip, int port){
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

        PenguardProto.PGPMessage.Builder groupMessage = PenguardProto.PGPMessage.newBuilder()

                .setType(PenguardProto.PGPMessage.Type.GG_GRP_INFO)
                .setRecipientIp(ip)
                .setRecipientPort(port)
                .setName(myself.getName())
                .setGroup(group);

        final int oldGuardianCount = guardians.size();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (guardians.size() == oldGuardianCount) {
                    // Our group has not changed even though it should have.
                    // Assume bad nat.
                    debug("Sent group to other guardian, but group hasn't changed since. Assuming bad nat.");
                    myself.setBadNat(true);
                }
            }
        }, NETWORK_TIMEOUT * 2);

        debug("about to send group to: " + ip + ":" + port);
        checkNatAndSend(groupMessage, ip, port);

        //also sending it to the server for relay purposes
        dispatcher.sendPacket(groupMessage.build(), plsIp, plsPort);
    }

    private void checkAndCommitOrAbort() {
        debug("checking back on previously initiated group change");
        if (commitState.state != CommitmentState.STATE_COMMIT_REQ_SENT) return;

        debug("onVoteYesReceived: " + commitState.voteYesReceived);
        debug("onVoteNoReceived: " + commitState.voteNoReceived);

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
        PenguardProto.PGPMessage.Builder commit = PenguardProto.PGPMessage.newBuilder()
                .setName(myself.getName())
                .setType(PenguardProto.PGPMessage.Type.GG_COMMIT)
                .setSeqNo(PenguardProto.SeqNo.newBuilder()
                        .setSeqno(commitState.groupUpdate.getSeqNo()));
        sendCommitToAllGuardians(commit);
    }

    private void sendAbort() {
        PenguardProto.PGPMessage.Builder abort = PenguardProto.PGPMessage.newBuilder()
                .setName(myself.getName())
                .setType(PenguardProto.PGPMessage.Type.GG_ABORT)
                .setSeqNo(PenguardProto.SeqNo.newBuilder()
                        .setSeqno(commitState.groupUpdate.getSeqNo()));
        sendCommitToAllGuardians(abort);
    }

// =================================================================================================
//                 SENDERS
// =================================================================================================

    private void sendToGuardian(PenguardProto.PGPMessage.Builder message, Guardian guardian) {
        if (myself.hasBadNat() || guardian.hasBadNat()) {
            sendViaPls(message, guardian.getIp(), guardian.getPort());
        }
        else {
            // send directly
            dispatcher.sendPacket(message.build(), guardian.getIp(), guardian.getPort());
        }
    }

    private void sendToGuardian(PenguardProto.PGPMessage.Builder message, PenguardProto.PGPGuardian guardian) {
        if (myself.hasBadNat() || guardian.getBadNat()) {
            sendViaPls(message, guardian.getIp(), guardian.getPort());
        }
        else {
            // send directly
            dispatcher.sendPacket(message.build(), guardian.getIp(), guardian.getPort());
        }
    }

    private void checkNatAndSend(PenguardProto.PGPMessage.Builder message, String host, int port) {
        if (myself.hasBadNat()) {
            sendViaPls(message, host, port);
        }
        else {
            dispatcher.sendPacket(message.build(), host, port);
        }
    }

    private void sendViaPls(PenguardProto.PGPMessage.Builder message, String host, int port) {
        message.setRecipientPort(port);
        message.setRecipientIp(host);
        dispatcher.sendPacket(message.build(), plsIp, plsPort);
    }

// =================================================================================================
//                 PENGUIN HELPERS
// =================================================================================================

    private void penguinGoneMissing(Penguin penguin) {
        debug("Penguin missing, derpo");;
        // When the user clicks the notification, switch to PenguinDetailActivity. The PenguinDetailActivity is responsible for stopping the alarm.
        Intent resultIntent = new Intent(this, GPenguinDetailActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        resultIntent.putExtra(GPenguinDetailActivity.EXTRA_PENGUIN_MAC, penguin.getAddress());
        TaskStackBuilder stackBuilderResult = TaskStackBuilder.create(this);
        stackBuilderResult.addParentStack(GPenguinDetailActivity.class);
        stackBuilderResult.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilderResult.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);

        // When the user dismisses the notification, start a service via intent to stop the alarm.
        Intent dismissIntent = new Intent(this, StopAlarmService.class);
        dismissIntent.putExtra(GPenguinDetailActivity.EXTRA_PENGUIN_MAC, penguin.getAddress());
        PendingIntent dismissPendingIntent = PendingIntent.getService(this, 1, dismissIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Build penguin-missing notification.
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon)
                .setColor(ContextCompat.getColor(this, R.color.orange))
                .setContentTitle(getText(R.string.notification_penguin_missing_title))
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true)
                .setDeleteIntent(dismissPendingIntent);

        // Set the notification text
        if (Math.random() < 0.98) {
            mBuilder.setContentText(penguin.getName() + " " + getText(R.string.notification_penguin_missing));
        }
        else {
            mBuilder.setContentText(penguin.getName() + " " + getText(R.string.notification_penguin_missing2));
        }

        // Activate notification. We hash the penguin address to get unique notifications for each missing penguin.
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(penguin.getNotificationId(), mBuilder.build());

        // Initialize alarmPlayer if not done already.
        if (alarmPlayer == null) {
            alarmPlayer = MediaPlayer.create(this, R.raw.alarm);
            alarmPlayer.setVolume(1.0f, 1.0f);
        }

        // Start ringing the alarm. We only ring the alarm if it isn't already ringing, otherwise the alarm sounds stack.
        if (!alarmPlayer.isPlaying()) {
            alarmPlayer.setLooping(true);
            alarmPlayer.start();
        }
    }

    private boolean isAnyPenguinSoundingAlarm() {
        for (Penguin p : penguins) {
            if (p.isMissing() && !p.isUserNotifiedOfMissing()) {
                return true;
            }
        }
        return false;
    }

    // Cancel any missing-notifications and alarm this penguin causes.
    void cancelAlarmAndNotificationForPenguin(Penguin p){
        int notificationId = p.getNotificationId();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);
        if (alarmPlayer != null) {
            if (!isAnyPenguinSoundingAlarm()){
                alarmPlayer.setLooping(false);
            }
        }
    }

// =================================================================================================
//                 UPDATERS
// =================================================================================================

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

    private void updateGuardianStatus(PenguardProto.PGPGuardian protoGuardian) {
        if (protoGuardian != null) {
            Guardian g = ListHelper.getGuardianByName(guardians, protoGuardian.getName());
            if (g != null) {
                g.setBadNat(protoGuardian.getBadNat());
                g.setIp(protoGuardian.getIp());
                g.setPort(protoGuardian.getPort());
            }
        }
    }

    private void updateStatus(PenguardProto.Group group) {
        ListHelper.copyPenguinListFromProtobufList(penguins, group.getPenguinsList());
        ListHelper.copyGuardianListFromProtobufList(guardians, group.getGuardiansList());
        this.seqNo = group.getSeqNo();
    }

    // Binder used for communication with the service. Do not use directly. Use GuardianServiceConnection instead.
    class PenguinGuardBinder extends Binder {

        GuardService getService() {
            return GuardService.this;
        }
    }

// =================================================================================================
//                 VARIOUS HELPER FUNCTIONS
// =================================================================================================

    private void turnOnBluetooth() {
        Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        enableBluetoothIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(enableBluetoothIntent);
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

    private boolean anyGuardianConnected() {
        for (Guardian g : guardians) {
            if (System.currentTimeMillis() - g.getTimeStamp() < GUARDIAN_SEEN_TIMEOUT) {
                return true;
            }
        }
        return false;
    }

    private boolean groupIsEmpty() {
        return guardians.size() <= 1;
    }

    private void debug(String msg) {
        Log.d("GuardService", msg);
    }

}
