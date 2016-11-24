package verteiltesysteme.penguard.lowLevelNetworking;

public abstract class ListenerCallback {
    public abstract void onReceive(Message parsedMessage);
}
