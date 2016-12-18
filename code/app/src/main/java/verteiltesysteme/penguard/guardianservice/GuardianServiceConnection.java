package verteiltesysteme.penguard.guardianservice;

import android.content.ComponentName;
import android.content.Context;
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
        service.interfaceAddPenguin(penguin, callback);
    }

    /**
     * Get a reference to a penguin by specifying its Mac
     * @param mac Mac address of the penguin
     * @return The corresponding penguin, or null if it doesn't exist
     */
    public Penguin getPenguinById(String mac){
        return service.interfaceGetPenguin(mac);
    }

    /**
     * Stop any ongoing alarm associated with the given penguin
     * @param penguin Penguin for which to stop alarms
     */
    public void stopAlarm(Penguin penguin){
        service.interfaceStopAlarm(penguin);
    }

    /**
     * Registers this guardian at the PLS using the given username. Does nothing if this guardian is already registered.
     * @param username The name to be used.
     * @param callback The callback executed when we get a decision whether the user was registered or not.
     * @return True if registration request was sent, false if this guardian is already registered.
     */
    public boolean register(String username, LoginCallback callback) {
        return service.interfaceRegister(username, callback);
    }

    /**
     * Registers this guardian at the PLS using the given username and uuid. Does nothing if this guardian is already registered.
     * @param username The name to be used.
     * @param uuid The uuid associated with that name
     * @param callback The callback executed when we get a decision whether the user was registered or not.
     * @return True if registration request was sent, false if this guardian is already registered.
     */
    public boolean reregister(String username, String uuid, LoginCallback callback){
        return service.interfaceReregister(username, uuid, callback);
    }

    /**
     * Unregisters this guardian at the PLS.
     * @param username The name to be used.
     * @param uuid The uuid associated with that name
     */
    public void deregister(String username, String uuid) {
        service.interfaceDeregister(username, uuid);
    }

    /**
     * Used to determine whether this service connection was successfully connected to a service.
     * @return true if connected, fals otherwise
     */
    public boolean isConnected() {
        return service != null;
    }

    public boolean joinGroup(String groupUN, GroupJoinCallback callback){
        return service.interfaceJoinGroup(groupUN, callback);
    }

    /**
     * Initiates removal of a given penguin from the service's list of guarded penguins.
     * @param mac Mac address of the penguin to be removed
     * @param callback A callback that is executed on successful/failed removal
     */
    public void removePenguin(String mac, TwoPhaseCommitCallback callback) {
        service.interfaceRemovePenguin(mac, callback);
    }

    /**
     * Returns a string that indicates which guardians are currently seeing a penguin.
     * @param mac Mac address of the penguin
     * @return String of the form "Seen by x, y"
     */
    public String getPenguinSeenByString(String mac) {
        return service.interfaceGetPenguinSeenByString(mac);
    }

    /**
     * Used to determine whether the guardian is registered with a PLS
     * @return true if registered, false otherwise
     */
    public boolean isRegistered(){
        return service.interfaceIsRegistered();
    }

    /**
     * Subscribes a listview to the service's PenguinAdapter, so that the list can display a list of all penguins.
     * @param listView The listview to subscribe.
     */
    public void subscribeListViewToPenguinAdapter(ListView listView) {
        service.interfaceSubscribeListViewToPenguinAdapter(listView);
    }

    /**
     * Subscribes a listview to the service's GuardianAdapter, so that the list can display a list of all guardians.
     * @param listView The listview to subscribe.
     */
    public void subscribeListViewToGuardianAdapter(ListView listView){
        service.interfaceSubscribeListViewToGuardianAdapter(listView);
    }

    /**
     * Initiate kicking a guardian from the group
     * @param guardian Guardian to be kicked
     * @param callback Callback to be called on success or failure.
     */
    public void kickGuardian(Guardian guardian, TwoPhaseCommitCallback callback){
        service.interfaceKickGuardian(guardian, callback);
    }

    /**
     * Return a reference to the guardian the service is representing
     * @return The guardian that is us
     */
    public Guardian getMyself(){
        return service.interfaceGetMyself();
    }

    /**
     * Sends our group information to a given IP and port, in order to merge groups with another guardian
     * @param ip The ip to send the infos to
     * @param port The port to send the infos to
     */
    public void sendGroupTo(String ip, int port){
        Log.e("####", "sendGroupTo: is the service null?" + (service == null));
        service.sendGroupTo(ip, port);
    }

    /**
     * Get the default PenguinSeenCallback from the service.
     * @return A reference to the default PenguinSeenCallback
     */
    public PenguinSeenCallback getPenguinSeenCallback(){
        return service.interfaceGetPenguinSeenCallback();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        this.service = ((GuardService.PenguinGuardBinder) service).getService();
        for (Runnable r : serviceConnectedCallbacks){
            r.run();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        this.service = null;
        debug("SERVICE DISCONNECTED");
    }

    public void registerServiceConnectedCallback(Runnable serviceConnectedCallback){
        serviceConnectedCallbacks.add(serviceConnectedCallback);
    }

    public void unregisterServiceConnectedCallback(Runnable serviceConnectedCallback){
        serviceConnectedCallbacks.remove(serviceConnectedCallback);
    }

    private void debug(String msg) {
        Log.d("GuardianServiceConnecti", msg);
    }

    public Context getContext(){
        return service;
    }

}
