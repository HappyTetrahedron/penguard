package verteiltesysteme.penguard.lowLevelNetworking;

/* This class provides a Callback for the UDPDispatcher. It's not strictly necessary for this class to implement Runnable, but has the advantage
 * that the class can easily be threaded for intensive computations (unlikely to ever be necessary) and that the class itself can use the run() method
 * to do some precomputation if necessary.
 */
public abstract class DispatcherCallback {
    private boolean success;

    // Used by the UDPDispatcher to set success status.
    // It seems kinda dangerous to me to have it that way, since if people were not aware that this has to be done, they could miss it.
    // Might come up with something better later
    public void setSuccess(boolean s){
        success = s;
    }

    public abstract void onSuccess();

    public abstract void onFailure();

    public void run(){
        if(success){
            onSuccess();
        }
        else {
            onFailure();
        }
    }

}
