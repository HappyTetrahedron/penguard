package verteiltesysteme.penguard.guardianservice;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.widget.ListView;

import java.util.ArrayList;

public class GuardianServiceConnection implements ServiceConnection {

    // List of callbacks that get executed once the connection is available.
    private ArrayList<Runnable> serviceConnectedCallbacks = new ArrayList<Runnable>();

    private GuardService service = null;

    /**
     * Adds a new penguin to the connected GuardService iff that penguin isn't already
     * being tracked.
     * @param penguin Penguin to be added
     */
    public void addPenguin(Penguin penguin, TwoPhaseCommitCallback callback) {
        service.addPenguin(penguin, callback);
    }

    public Penguin getPenguinById(String mac){
        return service.getPenguin(mac);
    }

    public void stopAlarm(Penguin penguin){
        service.stopAlarm(penguin);
    }

    /**
     * Registers this guardian at the PLS using the given username. Does nothing if this guardian is already registered.
     * @param username The name to be used.
     * @param callback The callback executed when we get a decision whether the user was registered or not.
     * @return True if registration request was sent, false if this guardian is already registered.
     */
    public boolean register(String username, LoginCallback callback) {
        return service.register(username, callback);
    }

    public boolean reregister(String username, String uuid, LoginCallback callback){
        return service.reregister(username, uuid, callback);
    }

    public void deregister(String username, String uuid) {
        service.deregister(username, uuid);
    }

    public boolean isConnected() {
        return service != null;
    }

    public boolean joinGroup(String groupUN, GroupJoinCallback callback){
        return service.joinGroup(groupUN, callback);
    }

    public String getPenguinName(String mac) {
        return service.getPenguinName(mac);
    }

    public void removePenguin(String mac, TwoPhaseCommitCallback callback) {
        service.removePenguin(mac, callback);
    }

    public String getPenguinSeenByString(String mac) {
        return service.getPenguinSeenByString(mac);
    }

    public boolean isRegistered(){
        return service.isRegistered();
    }

    public void subscribeListViewToPenguinAdapter(ListView listView) {
        service.subscribeListViewToPenguinAdapter(listView);
    }

    public void subscribeListViewToGuardianAdapter(ListView listView){
        service.subscribeListViewToGuardianAdapter(listView);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
       this.service = ((GuardService.PenguinGuardBinder) service).getService();
        for (Runnable r : serviceConnectedCallbacks){
            r.run();
        }
    }

    public void kickGuardian(Guardian guardian, TwoPhaseCommitCallback callback){
        service.kickGuardian(guardian, callback);
    }

    public Guardian getMyself(){
        return service.getMyself();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        this.service = null;
    }

    public void sendGroupTo(String ip, int port){
        Log.e("####", "sendGroupTo: is the service null?" + (service == null));
        service.sendGroupTo(ip, port);
    }

    public void registerServiceConnectedCallback(Runnable serviceConnectedCallback){
        serviceConnectedCallbacks.add(serviceConnectedCallback);
    }

    public void unregisterServiceConnectedCallback(Runnable serviceConnectedCallback){
        serviceConnectedCallbacks.remove(serviceConnectedCallback);
    }
}
