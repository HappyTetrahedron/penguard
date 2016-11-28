package verteiltesysteme.penguard.guardianservice;

import android.util.Log;

import java.util.Vector;

class BluetoothThread extends Thread {

    private final Vector<Penguin> penguins;
    private GuardService service;

    BluetoothThread(final Vector<Penguin> penguins, GuardService service) {
        this.penguins = penguins;
        this.service = service;
    }

    public void run() {
        while (!isInterrupted()) {
            for (Penguin p : penguins) {
                debug("Initiating RSSI scan for penguin " + p.getName());
                if (p.getGatt() == null) {
                    p.setGatt(p.getDevice().connectGatt(service, true, p.bluetoothGattCallback));
                }
                p.getGatt().readRemoteRssi();
            }

            try {
                Thread.sleep(5 * 1000); //TODO tweak this value
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void debug(String msg) {
        Log.d("BluetoothThread", msg);
    }
}
