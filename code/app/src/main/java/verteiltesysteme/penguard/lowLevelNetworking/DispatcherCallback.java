package verteiltesysteme.penguard.lowLevelNetworking;

public interface DispatcherCallback {
    void onSuccess();

    void onFailure(int errorCode);
}
