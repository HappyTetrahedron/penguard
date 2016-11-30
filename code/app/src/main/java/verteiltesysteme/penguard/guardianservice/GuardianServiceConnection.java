package verteiltesysteme.penguard.guardianservice;

import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

public class GuardianServiceConnection implements ServiceConnection {

    private GuardService service = null;

    /**
     * Adds a new penguin to the connected GuardService iff that penguin isn't already
     * being tracked.
     * @param penguin Penguin to be added
     */
    public void addPenguin(Penguin penguin) {
        service.addPenguin(penguin); //
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
