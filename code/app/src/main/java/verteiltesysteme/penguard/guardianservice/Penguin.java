package verteiltesysteme.penguard.guardianservice;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import java.util.Vector;

//this class is for the penguins

public class Penguin {
    private int rssiValue;
    private String name;
    private String address;
    private BluetoothDevice device = null;
    private BluetoothGatt gatt;

    Vector<Guardian> seenBy = new Vector<>();

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
            this.name = name;
            this.address = address;
    }

    public void initialize() {
        if (BluetoothAdapter.checkBluetoothAddress(address)) {
            this.device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
        }
        else {
                throw new IllegalStateException("Address is not a valid Bluetooth hardware address");
        }
    }

    public void setSeenBy(Guardian guardian, boolean newSeenStatus) {
        if (newSeenStatus && !seenBy.contains(guardian)) seenBy.add(guardian);
        if (!newSeenStatus && seenBy.contains(guardian)) seenBy.remove(guardian);
    }

    public boolean isInitialized() {
        return this.device != null;
    }

    public boolean isSeen() {
        return (gatt != null && gatt.getConnectionState(device) == BluetoothGatt.STATE_CONNECTED);
        // TODO also report 'false' if RSSI is under threshold, see issue #23
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Penguin){
            return this.address.equals(((Penguin) obj).address);
        }
        return false;
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

    String getAddress() {
        return address;
    }
    private void debug(String msg) {
        Log.d("PenguinClass", msg);
    }

}
