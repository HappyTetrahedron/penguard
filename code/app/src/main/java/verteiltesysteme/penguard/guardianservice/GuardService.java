package verteiltesysteme.penguard.guardianservice;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.Vector;

public class GuardService extends Service {

    private final Vector<Penguin> penguins = new Vector<>(); //a vector is the same as an arraylist only that it can grow and shrink

    BluetoothThread bluetoothThread;

    @Override
    public void onCreate() {
        super.onCreate();
        bluetoothThread = new BluetoothThread(penguins, this);
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

    public void addPenguin(BluetoothDevice device){
        Penguin penguin = new Penguin(device, "Penguin " + device.getName());
        penguins.add(penguin);
        debug("Penguin added.");
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
