package verteiltesysteme.penguard;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Vector;

//in here we do abslutely everything

public class GuardService extends Service {

    IBinder binder = new PenguinGuardBinder();

    //private BluetoothGattCallback bluetoothGattCallback;
    //private boolean ready;

    private Vector<Penguin> penguins = new Vector<Penguin>(); //a vector is the same as an arraylist only that it can grow and shrink
    private TextView rssiTV; //this will in the futur be replaced but for the moment this is easier


    public GuardService() {
    }

    public void setTV(TextView tv){
        this.rssiTV = tv;
    }

    /*private void readRssi(){
        bluetoothGattCallback = new BluetoothGattCallback() {

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                if (newState == BluetoothProfile.STATE_CONNECTED){
                    debug("Connected");
                    ready = true;
                    gatt.readRemoteRssi();
                    rssiTV.setText("test");
                }
                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    debug("Disconnected");
                    ready = false;
                    rssiTV.setText("Connection lost");//TODO this is a magic string i.e. not part of the final project
                }
            }
            @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                super.onReadRemoteRssi(gatt, rssi, status);
                debug("Read RSSI " + (float)rssi);
                rssiTV.setText("RSSI value:"+ rssi); //TODO this is not the final version
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    debug("Sleep interrupted");
                }
                if(ready) gatt.readRemoteRssi();
            }
        };


    }*/


    public void doWork(){
        //TODO find a better version to do this

        BluetoothThread btThread = new BluetoothThread();
        btThread.start();
    }

    private void readRSSIForAllPenguins(){
        StringBuffer text = new StringBuffer();

        for (Penguin p: penguins){
            text.append(p.device.getAddress()+" has rssi value: "+ p.getRssi());
        }
        rssiTV.setText(text);
    }

    public void addPenguin(BluetoothDevice device){
        Penguin penguin = new Penguin(device, this);
        penguins.add(penguin);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class PenguinGuardBinder extends Binder{
        GuardService getService(){
            return GuardService.this;
        }
    }

    private void debug(String msg) {
        Log.d("GuardService", msg);
    }

    public class BluetoothThread extends Thread{
        BluetoothThread(){
        }

        public void run(){
            StringBuffer text = new StringBuffer();
            while (true) {

                text.delete(0, text.capacity()); //this should empty the string buffer

                for (Penguin p : penguins) {
                    text.append(p.device.getAddress() + " has rssi value: " + p.getRssi());
                }
                rssiTV.setText(text);

                //put the thread to sleep for 5 seconds
                try {
                    sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }

    }
}
