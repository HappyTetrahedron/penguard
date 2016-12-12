package verteiltesysteme.penguard.guardianservice;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
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
    private BluetoothManager bluetoothManager;
    private double minDistance;
    private double maxDistance;

    private boolean seen = false;

    private Vector<Guardian> seenBy = new Vector<>();

    final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED){
                debug(name + " connected");
                gatt.readRemoteRssi();
            }
            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                seen = false;
                debug(name + " disconnected");
            }
        }
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            debug(name + " has RSSI " + (float)rssi);
            Penguin.this.rssiValue = rssi;
            seen = true;
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

    void initialize(BluetoothManager bm) {
        this.bluetoothManager = bm;
        if (BluetoothAdapter.checkBluetoothAddress(address)) {
            this.device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
        }
        else {
                throw new IllegalStateException("Address is not a valid Bluetooth hardware address");
        }
    }

    void setSeenBy(Guardian guardian, boolean newSeenStatus) {
        if (newSeenStatus && !seenBy.contains(guardian)) seenBy.add(guardian);
        if (!newSeenStatus && seenBy.contains(guardian)) seenBy.remove(guardian);
    }

    boolean isInitialized() {
        return this.device != null && this.bluetoothManager != null;
    }

    boolean isSeen() {
        //debug(name + (seen ? " is visible." : " is gone."));
        return seen;

        // TODO also report 'false' if RSSI is under threshold, see issue #23
        // TODO take other guardians into account
    }

    String getSeenByInfo(){
        if (seenBy.size() == 0) return "Seen by noone else";

        String answer = "Seen by ";
        for (Guardian g : seenBy) {
            answer += g.getName();
            answer += ", ";
        }
        answer = answer.substring(0, answer.length() - 2);
        return answer;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Penguin && this.address.equals(((Penguin) obj).address);
    }

    void disconnect() {
        if (gatt != null) {
            gatt.disconnect();
            gatt.close();
        }
    }

    public String getName() {
        return name;
    }

    public int getRssi(){
        return rssiValue;
    }

    public void setCalibratedValues(int[] calibratedValues){
        minDistance = calibratedValues[0];
        maxDistance = calibratedValues[1];
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

    public void readRssi(){
        gatt.readRemoteRssi();
    }

    public String getAddress() {
        return address;
    }

    private void debug(String msg) {
        Log.d("PenguinClass", msg);
    }

}
