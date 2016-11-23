package verteiltesysteme.penguard.lowLevelNetworking;

public abstract class DispatcherCallback extends Thread {
    private boolean success;

    // Used by the UDPDispatcher to set success status.
    // It seems kinda dangerous to me to have it that way, since if people were not aware that this has to be done, they could miss it.
    // Might come up with something better later
    public void setSuccess(boolean s){
        success = s;
    }

    public abstract void onSuccess();

    public abstract void onFailure();

    @Override
    public void run(){
        if(success){
            onSuccess();
        }
        else {
            onFailure();
        }
    }

}
