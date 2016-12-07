package verteiltesysteme.penguard.guardianservice;

import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import verteiltesysteme.penguard.GGuardActivity;
import verteiltesysteme.penguard.GLoginCallback;
import verteiltesysteme.penguard.R;
import verteiltesysteme.penguard.lowLevelNetworking.ListenerCallback;
import verteiltesysteme.penguard.lowLevelNetworking.UDPDispatcher;
import verteiltesysteme.penguard.lowLevelNetworking.UDPListener;
import verteiltesysteme.penguard.protobuf.PenguardProto;

public class GuardService extends Service implements ListenerCallback{

    // group state: penguins, guardians, and state sequence number
    private final Vector<Penguin> penguins = new Vector<>();
    private final Vector<Guardian> guardians = new Vector<>();
    private int seqNo = 0;

    private Guardian myself = new Guardian();

    // networking infrastructure
    private UDPDispatcher dispatcher;
    private UDPListener listener;
    private DatagramSocket sock;

    // Networking constants
    private final static int SOCKETS_TO_TRY = 5;
    private final static int NETWORK_TIMEOUT = 5000; // Network timeout in ms
    private final static int PORT = 6789; //TODO put this in settings? See issue #14

    // To be removed
    private String plsIp = "192.168.0.113"; //default values... that actual values will be read from the settings
    private int plsPort = 6789;

    private final static int NOTIFICATION_ID = 1;

    private RegistrationState regState = new RegistrationState();
    private JoinState joinState = new JoinState();

    private Handler handler;

    BluetoothThread bluetoothThread;

    BroadcastReceiver bluetoothBroadcastReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
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
                sock = new DatagramSocket(PORT);
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // kill our networking structure
        sock.close();
        try {
            listener.join();
        } catch (InterruptedException e) {
            // do nothing? TODO how do we handle this?
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
        return super.onStartCommand(intent, flags, startId);
    }



    @Override
    public IBinder onBind(Intent intent) {
        return new PenguinGuardBinder();
    }

    boolean register(String username, GLoginCallback callback) {

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

    boolean joinGroup(String groupUN){
        //TODO #17
        if (joinState.state != JoinState.STATE_NOT_JOINED) return false;

        debug("joining the group of: "+ groupUN);
        joinState.startGroupJoin(groupUN);

        //create join message for the server
        PenguardProto.PGPMessage joinMessage = PenguardProto.PGPMessage.newBuilder()

                .setType(PenguardProto.PGPMessage.Type.GS_GROUP_REQ)
                .setGroupReq(PenguardProto.GroupReq.newBuilder().setName(groupUN))
                .build();

        //send to PLS
        updateIpPortFromSettings();
        dispatcher.sendPacket(joinMessage, plsIp, plsPort);

        //Timeout
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (joinState.state != JoinState.STATE_JOINED) joinState.joinFailed();
            }
        }, NETWORK_TIMEOUT);

        return true;
    }

    /**
     * Adds a new penguin to the list of tracked penguins. If a penguin with the same HW address is already
     * in the list, the list is not changed.
     * @param penguin Penguin to be added
     */
    void addPenguin(Penguin penguin){
        if (!penguins.contains(penguin)) {
            penguins.add(penguin);
            debug("Penguin added.");
        }
        debug("penguin already there");
    }

    @Override
    public void onReceive(PenguardProto.PGPMessage parsedMessage, InetAddress address, int port) {
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
                statusUpdateReceived(parsedMessage, address.getHostName(), port);
                break;
            case GG_GRP_CHANGE:
                grpChangeReceived(parsedMessage);
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
            default:
                debug("Packet with unexpected type arrived");
                break;
        }
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
        // TODO implement method. See issue #31
    }

    private void voteNoReceived(PenguardProto.PGPMessage message){
        // TODO implement method. See issue #32
    }

    private void voteYesReceived(PenguardProto.PGPMessage message){
        // TODO implement method. See issue #33
    }

    private void abortReceived(PenguardProto.PGPMessage message){
        // TODO implement method. See issue #34
    }

    private void commitReceived(PenguardProto.PGPMessage message){
        // TODO implement method. See issue #35
    }

    private void grpChangeReceived(PenguardProto.PGPMessage message){
        // TODO implement method. See issue #36
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

    private void mergeReqReceived(PenguardProto.PGPMessage message){
        // TODO implement method. See issue #39
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
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        plsIp = sharedPref.getString(getString(R.string.pref_key_server_address), getString(R.string.pref_default_server));
        String plsPortstring = sharedPref.getString(getString(R.string.pref_key_port), getString(R.string.pref_default_port));
        plsPort = Integer.parseInt(plsPortstring);
    }

    private void turnOnBluetooth() {
        Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        enableBluetoothIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(enableBluetoothIntent);
    }

    // Binder used for communication with the service. Do not use directly. Use GuardianServiceConnection instead.
    class PenguinGuardBinder extends Binder{

        GuardService getService() {
            return GuardService.this;
        }
    }

    private void debug(String msg) {
        Log.d("GuardService", msg);
    }

}
