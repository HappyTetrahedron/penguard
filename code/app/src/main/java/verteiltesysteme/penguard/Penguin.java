package verteiltesysteme.penguard;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

/**
 * Created by nicole on 23.11.16.
 */

//this class is for the penguins

public class Penguin {
    int rssiValue;
    String name;
    String adress;
    BluetoothDevice device;
    Context context;

    boolean ready;

    public Penguin(BluetoothDevice device, Context context){
        this.context = context;
        this.device = device;
        //not sure if the rest is really useful anyway it's in for the moment
    }

    public BluetoothDevice getDevice(){
        return device;
    }

    public int getRssi(){
       BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                if (newState == BluetoothProfile.STATE_CONNECTED){
                    debug("Connected");
                    ready = true;
                    gatt.readRemoteRssi();
                    //rssiTV.setText("test");
                }
                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    debug("Disconnected");
                    ready = false;
                    //rssiTV.setText("Connection lost");//TODO this is a magic string i.e. not part of the final project
                }
            }
            @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                super.onReadRemoteRssi(gatt, rssi, status);
                debug("Read RSSI " + (float)rssi);
                //rssiTV.setText("RSSI value:"+ rssi); //TODO this is not the final version
                rssiValue = rssi;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    debug("Sleep interrupted");
                }
                if(ready) gatt.readRemoteRssi();
            }
        };
        BluetoothGatt bluetoothGatt = device.connectGatt(context, false, bluetoothGattCallback);
        return rssiValue;
    }

    private void debug(String msg) {
        Log.d("PenguinClass", msg);
    }

}
