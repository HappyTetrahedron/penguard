package verteiltesysteme.penguard.guardianservice;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.UUID;
import java.util.Vector;

import verteiltesysteme.penguard.GGuardActivity;
import verteiltesysteme.penguard.GLoginCallback;
import verteiltesysteme.penguard.R;
import verteiltesysteme.penguard.lowLevelNetworking.ListenerCallback;
import verteiltesysteme.penguard.lowLevelNetworking.UDPDispatcher;
import verteiltesysteme.penguard.lowLevelNetworking.UDPListener;
import verteiltesysteme.penguard.protobuf.PenguardProto;

import static android.R.attr.port;
import static android.R.attr.y;
import static android.R.id.message;
import static verteiltesysteme.penguard.R.string.join;
import static verteiltesysteme.penguard.protobuf.PenguardProto.PGPMessage.Type.GG_ABORT;
import static verteiltesysteme.penguard.protobuf.PenguardProto.PGPMessage.Type.GG_ACK;
import static verteiltesysteme.penguard.protobuf.PenguardProto.PGPMessage.Type.GG_COMMIT;
import static verteiltesysteme.penguard.protobuf.PenguardProto.PGPMessage.Type.GG_GRP_CHANGE;
import static verteiltesysteme.penguard.protobuf.PenguardProto.PGPMessage.Type.GS_DEREGISTER;
import static verteiltesysteme.penguard.protobuf.PenguardProto.PGPMessage.Type.GS_REGISTER;
import static verteiltesysteme.penguard.protobuf.PenguardProto.PGPMessage.Type.SG_ACK;
import static verteiltesysteme.penguard.protobuf.PenguardProto.PGPMessage.Type.SG_ERR;
import static verteiltesysteme.penguard.protobuf.PenguardProto.PGPMessage.Type.SG_MERGE_REQ;

public class GuardService extends Service implements ListenerCallback{

    private final Vector<Penguin> penguins = new Vector<>();

    private UDPDispatcher dispatcher;
    private UDPListener listener;
    private DatagramSocket sock;

    private final static int SOCKETS_TO_TRY = 5;
    private final static int NETWORK_TIMEOUT = 5000; // Network timeout in ms
    private final static int PORT = 6789; //TODO put this in settings? See issue #14

    private String plsIp = "10.2.129.106"; //TODO just for debugging. Put these in settings and read from there. See issue #14
    private int plsPort = 6789;

    private final static int NOTIFICATION_ID = 1;

    private RegistrationState regState = new RegistrationState();

    private Handler handler;

    BluetoothThread bluetoothThread;

    @Override
    public void onCreate() {
        super.onCreate();
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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO detect whether thread is already started. Only re-start it if not. See issue #25
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
        regState.registrationProcessStarted(username, callback);

        // create registration message
        PenguardProto.PGPMessage regMessage = PenguardProto.PGPMessage.newBuilder()
                .setType(GS_REGISTER)
                .setName(username)
                .build();

        // send it to PLS
        dispatcher.sendPacket(regMessage, plsIp, plsPort);

        // once timeout ticks off, cancel registration iff it is still in progress
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (regState.state != RegistrationState.STATE_REGISTERED) regState.registrationFailed();
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
    public void onReceive(PenguardProto.PGPMessage parsedMessage) {
        debug(parsedMessage.toString());

        switch(parsedMessage.getType()){
            case SG_ERR:
                regState.registrationFailed();
                break;
            case SG_ACK:
                regState.registrationSucceeded(UUID.fromString(parsedMessage.getAck().getUuid()));
                break;
            case SG_MERGE_REQ:
                mergeReqReceived();
                break;
            case GG_STATUS_UPDATE:
                statusUpdateReceived();
                break;
            case GG_ACK:
                guardianAckReceived();
                break;
            case GG_GRP_CHANGE:
                grpChangeReceived();
                break;
            case GG_COMMIT:
                commitReceived();
                break;
            case GG_ABORT:
                abortReceived();
                break;
            case GG_VOTE_YES:
                voteYesReceived();
                break;
            case GG_VOTE_NO:
                voteNoReceived();
                break;
            case GG_GRP_INFO:
                grpInfoReceived();
                break;
            default:
                debug("Packet with unexpected type arrived");
                break;
        }
    }

    private void grpInfoReceived(){
        // TODO implement method. See issue #31
    }

    private void voteNoReceived(){
        // TODO implement method. See issue #32
    }

    private void voteYesReceived(){
        // TODO implement method. See issue #33
    }

    private void abortReceived(){
        // TODO implement method. See issue #34
    }

    private void commitReceived(){
        // TODO implement method. See issue #35
    }

    private void grpChangeReceived(){
        // TODO implement method. See issue #36
    }

    private void guardianAckReceived(){
        // TODO implement method. See issue #37
    }

    private void statusUpdateReceived(){
        // TODO implement method. See issue #38
    }

    private void mergeReqReceived(){
        // TODO implement method. See issue #39
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
