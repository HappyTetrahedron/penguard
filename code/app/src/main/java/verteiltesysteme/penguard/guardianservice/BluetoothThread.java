package verteiltesysteme.penguard.guardianservice;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

class BluetoothThread extends Thread{

    private boolean running = true;
    private boolean paused = false;

    private final static int SCAN_INTERVAL_SECONDS = 5;
    private final PenguinList penguins;
    private GuardService service;
    private BluetoothManager manager;
    private BluetoothBroadcastReceiver bluetoothBroadcastReceiver;

    BluetoothThread(final PenguinList penguins, GuardService service) {
        this.penguins = penguins;
        this.service = service;
        this.manager = (BluetoothManager) service.getSystemService(Context.BLUETOOTH_SERVICE);
        this.bluetoothBroadcastReceiver = new BluetoothBroadcastReceiver();
    }

    public void run() {
        while (running && !isInterrupted()) {
            if (!paused) {
                for (Penguin p : penguins) {
                    if (paused) break;

                    if (!p.isInitialized()) p.initialize(manager);

                    if (p.getGatt() == null) {
                        p.setGatt(p.getDevice().connectGatt(service, true, p.bluetoothGattCallback));
                    }
                    else if (manager.getConnectionState(p.getDevice(), BluetoothProfile.GATT) != BluetoothAdapter.STATE_CONNECTED) {
                        debug("Initiating connect for penguin " + p.getName());
                        p.getGatt().connect();
                    }
                    else {
                        debug("Initiating RSSI scan for penguin " + p.getName());
                        p.readRssi();
                    }
                }
            }

            try {
                Thread.sleep(SCAN_INTERVAL_SECONDS * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    BroadcastReceiver getBroadcastReceiver() {
        return bluetoothBroadcastReceiver;
    }

    class BluetoothBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                    == BluetoothAdapter.STATE_OFF) {
                debug("That idiot turned off bluetooth");
                paused = true;
                for (Penguin p : penguins) {
                    p.setGatt(null);
                }
            } else if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                    == BluetoothAdapter.STATE_ON) {
                debug("Bluetooth turned back on");
                paused = false;
            }
        }
    }

    synchronized void stopScanning() {
        this.running = false;
    }

    private void debug(String msg) {
        Log.d("BluetoothThread", msg);
    }
}
