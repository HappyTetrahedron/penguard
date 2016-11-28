package verteiltesysteme.penguard.guardianservice;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

//this class is for the penguins

public class Penguin {
    private int rssiValue;
    private String name;
    private String address;
    private BluetoothDevice device;
    private BluetoothGatt gatt;

    final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED){
                debug(name + " connected");
                gatt.readRemoteRssi();
            }
            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                debug(name + " disconnected");
            }
        }
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            debug(name + " has RSSI " + (float)rssi);
            Penguin.this.rssiValue = rssi;
        }
    };

    public Penguin(BluetoothDevice device, String name){
        this.device = device;
        this.address = device.getAddress();
        this.name = name;
    }

    public Penguin(String address, String name) {
        if (BluetoothAdapter.checkBluetoothAddress(address)) {
            this.name = name;
            this.address = address;
            this.device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
        }
        else throw new IllegalArgumentException("Address must be valid Bluetooth hardware address");
    }

    public boolean isSeen() {
        return (gatt != null && gatt.getConnectionState(device) == BluetoothGatt.STATE_CONNECTED);
        // TODO also report 'false' if RSSI is under threshold
    }

    public String getName() {
        return name;
    }

    public int getRssi(){
        return rssiValue;
    }

    BluetoothGatt getGatt() {
        return gatt;
    }

    void setGatt(BluetoothGatt gatt) {
        this.gatt = gatt;
    }

    BluetoothDevice getDevice() {
        return device;
    }

    private void debug(String msg) {
        Log.d("PenguinClass", msg);
    }

}
