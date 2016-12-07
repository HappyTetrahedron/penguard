package verteiltesysteme.penguard.guardianservice;

import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import verteiltesysteme.penguard.GLoginCallback;

public class GuardianServiceConnection implements ServiceConnection {

    private GuardService service = null;

    /**
     * Adds a new penguin to the connected GuardService iff that penguin isn't already
     * being tracked.
     * @param penguin Penguin to be added
     */
    public void addPenguin(Penguin penguin) {
        service.addPenguin(penguin);
    }

    /**
     * Registers this guardian at the PLS using the given username. Does nothing if this guardian is already registered.
     * @param username The name to be used.
     * @param callback The callback executed when we get a decision whether the user was registered or not.
     * @return True if registration request was sent, false if this guardian is already registered.
     */
    public boolean register(String username, GLoginCallback callback) {
        return service.register(username, callback);
    }

    public boolean isConnected() {
        return service != null;
    }

    public boolean joinGroup(String groupUN){
        return service.joinGroup(groupUN);
    }

    public boolean isRegistered(){
        return service.isRegistered();
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
