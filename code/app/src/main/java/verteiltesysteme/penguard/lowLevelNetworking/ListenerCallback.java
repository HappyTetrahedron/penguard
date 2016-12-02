package verteiltesysteme.penguard.lowLevelNetworking;

import java.net.InetAddress;

import verteiltesysteme.penguard.protobuf.PenguardProto;

public interface ListenerCallback {

    void onReceive(PenguardProto.PGPMessage parsedMessage, InetAddress address, int port);
}
