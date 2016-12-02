package verteiltesysteme.penguard.guardianservice;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
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

public class GuardService extends Service implements ListenerCallback{

    private final Vector<Penguin> penguins = new Vector<>();

    private UDPDispatcher dispatcher;
    private UDPListener listener;
    private DatagramSocket sock;

    private final static int SOCKETS_TO_TRY = 5;
    private final static int PORT = 6789; //TODO put this in settings? See issue #14

    private String plsIp = "10.2.129.106"; //TODO just for debugging. Put these in settings and read from there. See issue #14
    private int plsPort = 6789;

    private final static int NOTIFICATION_ID = 1;

    private RegistrationState regState = new RegistrationState();

    BluetoothThread bluetoothThread;

    @Override
    public void onCreate() {
        super.onCreate();
        bluetoothThread = new BluetoothThread(penguins, this);

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
                // TODO how do we properly handle this? See issue #24
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
        bluetoothThread.start();
        debug("Thread started");

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new PenguinGuardBinder();
    }

    boolean register(String username, GLoginCallback callback) {

        if (regState.state != regState.STATE_UNREGISTERED) return false;
        debug("Registering " + username);
        regState.registrationProcessStarted(username, callback);

        // create registration message
        PenguardProto.PGPMessage regMessage = PenguardProto.PGPMessage.newBuilder()
                .setType(PenguardProto.PGPMessage.Type.GS_REGISTER)
                .setName(username)
                .build();

        // send it to PLS
        dispatcher.sendPacket(regMessage, plsIp, plsPort);

        //TODO detect timeout, see issue #27
        // We'll need to abort waiting for the packet to arrive after a certain time. Probably a good idea to do
        // that within this class, so we can protect ourselves to highly delayed ACKs/ERRs from the server.

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

        //TODO add more switch-case mayhem to distinguish between all the message types, see issue #26

        switch(parsedMessage.getType()){
            case SG_ERR:
                regState.registrationFailed();
                break;
            case SG_ACK:
                regState.registrationSucceeded(UUID.fromString(parsedMessage.getAck().getUuid()));
                break;
            default:
                debug("Packet with unexpected type arrived");
                break;

        }
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
