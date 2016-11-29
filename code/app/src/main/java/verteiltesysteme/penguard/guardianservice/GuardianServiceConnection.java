package verteiltesysteme.penguard.guardianservice;

import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

public class GuardianServiceConnection implements ServiceConnection {

    private GuardService service = null;

    public void addPenguin(BluetoothDevice device) {
        service.addPenguin(device);
    }

    public boolean isConnected() {
        return service != null;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
       this.service = ((GuardService.PenguinGuardBinder) service).getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        this.service = null;
    }
}
