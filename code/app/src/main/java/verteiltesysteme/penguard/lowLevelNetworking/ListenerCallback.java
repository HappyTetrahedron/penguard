package verteiltesysteme.penguard.lowLevelNetworking;

import verteiltesysteme.penguard.protobuf.PenguardProto;

public abstract class ListenerCallback {
    public abstract void onReceive(PenguardProto.PGPMessage parsedMessage);
}
