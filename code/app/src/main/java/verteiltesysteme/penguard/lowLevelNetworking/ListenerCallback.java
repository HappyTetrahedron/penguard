package verteiltesysteme.penguard.lowLevelNetworking;

public abstract class ListenerCallback extends Thread {
    private Message parsedMessage;

    protected ListenerCallback() {
        super();
    }

    // Used by the UDPListener to set the message that was parsed.
    // It seems kinda dangerous to me to have it that way, since if people were not aware that this has to be done, they could miss it.
    // Might come up with something better later
    protected void setParsedMessage(Message parsedMessage){
        this.parsedMessage = parsedMessage;
    }

    protected Message getParsedMessage() {
        return parsedMessage;
    }

    public abstract void onReceive();

    @Override
    public void run() {
        onReceive();
    }
}
