package verteiltesysteme.penguard.lowLevelNetworking;

/* This class provides a Callback for the UDPDispatcher. It's not strictly necessary for this class to implement Runnable, but has the advantage
 * that the class can easily be threaded for intensive computations (unlikely to ever be necessary) and that the class itself can use the run() method
 * to do some precomputation if necessary.
 */
public interface DispatcherCallback {
    void onSuccess();

    void onFailure(int errorCode);
}
