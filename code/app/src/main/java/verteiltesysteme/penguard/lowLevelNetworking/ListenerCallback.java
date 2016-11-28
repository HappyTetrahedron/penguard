package verteiltesysteme.penguard.lowLevelNetworking;

import verteiltesysteme.penguard.protobuf.PenguardProto;

public interface ListenerCallback {
    void onReceive(PenguardProto.PGPMessage parsedMessage);
}
