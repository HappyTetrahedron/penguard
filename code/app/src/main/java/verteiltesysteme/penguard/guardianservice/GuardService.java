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

    private final static int PORT = 6789; //TODO put this in settings?

    private String plsIp = "10.2.131.82"; //TODO just for debugging. Put these in settings and read from there.
    private int plsPort = 6789;

    private final static int NOTIFICATION_ID = 1;

    BluetoothThread bluetoothThread;

    private int registrationState = 1;
    private final static int REG_STATE_UNREGISTERED = 1;
    private final static int REG_STATE_REGISTERED = 2;
    private String username = "";
    private UUID uuid = null;


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
        try {
            sock = new DatagramSocket(PORT);
            listener = new UDPListener(sock);
            dispatcher = new UDPDispatcher(sock);
        } catch (SocketException e) {
            debug("Error creating socket: " + e.getMessage());
            //TODO how do we properly handle this?
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

        bluetoothThread.start();
        debug("Thread started");

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new PenguinGuardBinder();
    }


    boolean register(String username) {
        if (registrationState != REG_STATE_UNREGISTERED) return false;
        debug("Registering " + username);

        this.username = username;

        // create registration message
        PenguardProto.PGPMessage regMessage = PenguardProto.PGPMessage.newBuilder()
                .setType(PenguardProto.PGPMessage.Type.GS_REGISTER)
                .setName(username)
                .build();

        // send it to PLS

        dispatcher.sendPacket(regMessage, plsIp, plsPort);

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

        //TODO add if-else mayhem to distinguish between all the message types
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
