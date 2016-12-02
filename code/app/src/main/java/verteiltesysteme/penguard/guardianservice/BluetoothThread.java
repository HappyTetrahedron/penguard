package verteiltesysteme.penguard.guardianservice;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.util.Vector;

class BluetoothThread extends Thread {

    private final static int SCAN_INTERVAL_SECONDS = 5;
    private final Vector<Penguin> penguins;
    private GuardService service;
    private BluetoothManager manager;

    BluetoothThread(final Vector<Penguin> penguins, GuardService service) {
        this.penguins = penguins;
        this.service = service;
        this.manager = (BluetoothManager) service.getSystemService(Context.BLUETOOTH_SERVICE);
    }

    public void run() {
        while (!isInterrupted()) {

            //TODO this crashes hard if the user randomly disables bluetooth. See issue #22
            for (Penguin p : penguins) {

                if (!p.isInitialized()) p.initialize();

                if (p.getGatt() == null) {
                    p.setGatt(p.getDevice().connectGatt(service, true, p.bluetoothGattCallback));
                }
                else if (manager.getConnectionState(p.getDevice(), BluetoothProfile.GATT) != BluetoothAdapter.STATE_CONNECTED) {
                    debug("Initiating connect for penguin " + p.getName());
                    p.getGatt().connect();
                }
                else {
                    debug("Initiating RSSI scan for penguin " + p.getName());
                    p.getGatt().readRemoteRssi();
                }
            }

            try {
                Thread.sleep(SCAN_INTERVAL_SECONDS * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void debug(String msg) {
        Log.d("BluetoothThread", msg);
    }
}
