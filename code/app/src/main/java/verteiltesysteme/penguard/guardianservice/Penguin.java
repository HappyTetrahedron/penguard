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
    private String name;
    private String address;
    private BluetoothDevice device = null;
    private BluetoothGatt gatt;
    private BluetoothManager bluetoothManager;
    private int rssiValue;
    private int minDistanceRssi;
    private int maxDistanceRssi;

    /* Couldn't come up with a good name. This factor determines how much lower than the maxDistanceRssi
     * a Penguin's RSSI can be to still be qualified as 'seen'.
     */
    private final double RSSI_SEEN_THRESHOLD_LEVERAGE = 1.2;

    private boolean userNotifiedOfMissing = true;

    /* In case you're wondering why this boolean suddenly turned into a class, it's because the alternative (keeping it a simple boolean)
     * would lead to bugs with absolute certainty, since we're at least once going to forget setting userNotifiedOfMissing to false every time seen is
     * set to true.
     */
    private Seen seen = new Seen();
    private class Seen {
        private boolean seen = false;

        private void updateSeen(boolean seen){
            this.seen = seen;
            if (seen) {
                userNotifiedOfMissing = false;
            }
        }

        private boolean isSeen() {
            return seen;
        }
    }

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
                seen.updateSeen(false);
                debug(name + " disconnected");
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            debug(name + " has RSSI " + (float)rssi);
            Penguin.this.rssiValue = rssi;

            if(maxDistanceRssi == 0 && minDistanceRssi == 0) { // distance threshold not set
                debug("Penguin seen");
                seen.updateSeen(true);
                return;
            }
            else{
                debug("minDistanceRssi: " + minDistanceRssi);
                debug("maxDistanceRssi: " + maxDistanceRssi);
            }

            // distance threshold set
            if (rssiValue >= Penguin.this.getRssiSeenThreshold()) {
                seen.updateSeen(true);
            }
            else {
                seen.updateSeen(false);
            }
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

    private double getRssiSeenThreshold() {
        return (minDistanceRssi - RSSI_SEEN_THRESHOLD_LEVERAGE * (minDistanceRssi - maxDistanceRssi));
    }

    void setSeenBy(Guardian guardian, boolean newSeenStatus) {
        if (newSeenStatus && !seenBy.contains(guardian)) {
            seenBy.add(guardian);
            userNotifiedOfMissing = false;
        }
        if (!newSeenStatus && seenBy.contains(guardian)) seenBy.remove(guardian);
    }

    boolean isSeenByAnyone(){
        return !seenBy.isEmpty() || seen.isSeen();
    }

    boolean isInitialized() {
        return this.device != null && this.bluetoothManager != null;
    }

    boolean isSeen() {
        return seen.isSeen();
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
        minDistanceRssi = calibratedValues[0];
        maxDistanceRssi = calibratedValues[1];
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

    void setUserNotifiedOfMissing(boolean userNotifiedOfMissing){
        this.userNotifiedOfMissing = userNotifiedOfMissing;
    }

    boolean isUserNotifiedOfMissing() {
        return userNotifiedOfMissing;
    }

}
