package verteiltesysteme.penguard.guardianservice;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Vector;

import verteiltesysteme.penguard.R;

//this class is for the penguins

public class Penguin {
    private String name;
    private String address;
    private BluetoothDevice device = null;
    private BluetoothGatt gatt;
    private BluetoothManager bluetoothManager;
    private Context context;
    private int rssiValue;
    private int minDistanceRssi;
    private int maxDistanceRssi;
    private long lastSeenTimestamp;

    /* Couldn't come up with a good name. This factor determines how much lower than the maxDistanceRssi
     * a Penguin's RSSI can be to still be qualified as 'seen'.
     */
    private final double RSSI_SEEN_THRESHOLD_LEVERAGE = 1.2;
    // Amount of seconds after which penguin is reported missing.
    private double penguinMissingThreshold = 30;
    private boolean userNotifiedOfMissing = false;

    private Vector<Guardian> seenBy = new Vector<>();

    /* A callback provided by the GuardService, that gets executed every time the penguins status gets set to seen.
     * This callback is used to cancel notifications and alarms.
     */
    private PenguinSeenCallback seenCallback;

    final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED){
                debug(name + " connected");
                updateTimestamp();
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

            if(maxDistanceRssi == 0 && minDistanceRssi == 0) { // distance threshold not set
                debug("Penguin seen");
                updateTimestamp();
                return;
            }
            else{
                debug("minDistanceRssi: " + minDistanceRssi);
                debug("maxDistanceRssi: " + maxDistanceRssi);
            }

            // distance threshold set
            if (rssiValue >= Penguin.this.getRssiSeenThreshold()) {
                updateTimestamp();
            }
        }
    };

    public Penguin(BluetoothDevice device, String name, Context context){
        this.device = device;
        this.address = device.getAddress();
        this.name = name;
        this.context = context;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        penguinMissingThreshold = Double.parseDouble(sharedPreferences.getString(context.getString(R.string.pref_key_penguin_missing_delay),
                context.getResources().getString(R.string.pref_default_penguin_missing_delay)));
    }

    public Penguin(String address, String name, Context context) {
        this.name = name;
        this.address = address;
        this.context = context;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        penguinMissingThreshold = Double.parseDouble(sharedPreferences.getString(context.getString(R.string.pref_key_penguin_missing_delay),
                context.getResources().getString(R.string.pref_default_penguin_missing_delay)));
    }

    public void registerSeenCallback(PenguinSeenCallback callback) {
        seenCallback = callback;
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
            seenCallback.penguinRediscovered(Penguin.this);
        }
        else if (!newSeenStatus && seenBy.contains(guardian)) {
            seenBy.remove(guardian);
        }
    }

    boolean needsAlarm() {
        return isMissing() && !userNotifiedOfMissing;
    }

    private boolean isMissing(){
        debug("Penguin " + getName() + " last seen " + ((System.currentTimeMillis() - lastSeenTimestamp) / 1000.0) + " seconds ago");
        return ((System.currentTimeMillis() - lastSeenTimestamp ) / 1000.0 > penguinMissingThreshold) && seenBy.isEmpty();
    }

    boolean isInitialized() {
        return this.device != null && this.bluetoothManager != null;
    }

    public boolean isSeen() {
        return (System.currentTimeMillis() - lastSeenTimestamp) / 1000.0 < penguinMissingThreshold;
    }

    /** Returns a String that states which guardians see the penguin.
     */
    String getSeenByInfo(){
        String response = context.getString(R.string.seen_by) + " ";
        if (isSeen()) response += context.getString(R.string.you_dativ) + context.getString(R.string.comma) + " ";
        for (Guardian g : seenBy) {
            if (!g.isGuardianMissing()) {
                response += g.getName();
                response += context.getString(R.string.comma) + " ";
            }
        }

        if (response.indexOf(context.getString(R.string.comma)) < 0) {
            response += context.getString(R.string.nobody);
        }
        else {
            response = response.substring(0, response.length() - context.getString(R.string.comma).length() - 1);
        }

        return response;
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
        Log.d("Penguin", msg);
    }

    int getNotificationId(){
        return (GuardService.ALARM_NOTIFICATION_ID + getAddress()).hashCode();
    }

    void setUserNotifiedOfMissing(boolean userNotifiedOfMissing){
        this.userNotifiedOfMissing = userNotifiedOfMissing;
    }

    boolean isUserNotifiedOfMissing() {
        return userNotifiedOfMissing;
    }

    // Returns true iff there is a guardian who sees the penguin and that we have communicated with recently, OR if we see the penguin ourselves.
    public boolean isSeenByAnyone() {
        for (Guardian g : seenBy){
            if (!g.isGuardianMissing()) {
                return true;
            }
        }
        return isSeen();
    }

    private void updateTimestamp() {
        lastSeenTimestamp = System.currentTimeMillis();
        if (seenCallback != null) {
            seenCallback.penguinRediscovered(Penguin.this);
        }
        setUserNotifiedOfMissing(false);
    }
}
