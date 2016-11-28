package verteiltesysteme.penguard.lowLevelNetworking;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import verteiltesysteme.penguard.protobuf.PenguardProto;

public class PGPPacket {
    InetAddress ipAddr;
    PenguardProto.PGPMessage message;

    public PGPPacket(String ip, PenguardProto.PGPMessage message) throws UnknownHostException {
        ipAddr = InetAddress.getByName(ip);
        this.message = message;
    }
}

