package verteiltesysteme.penguard.lowLevelNetworking;

import android.provider.ContactsContract;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import verteiltesysteme.penguard.protobuf.PenguardProto;

import static android.R.attr.port;

public class UDPListener extends Thread {
    private ListenerCallback onReceiveAction;
    private DatagramSocket socket = null;

    public UDPListener(DatagramSocket datagramSocket){
        socket = datagramSocket;
    }

    public void registerCallback(ListenerCallback onReceiveAction){
        this.onReceiveAction = onReceiveAction;
    }

    @Override
    public void run(){
        while(true){
            try{
                try {
                    byte[] inData = new byte[64];
                    DatagramPacket in = new DatagramPacket(inData, inData.length);
                    socket.receive(in);
                    inData = in.getData();
                    onReceiveAction.onReceive(parseMessage(inData));
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

                Thread.sleep(50);
            }
            catch(java.lang.InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    private PenguardProto.PGPMessage parseMessage(byte[] data){
        PenguardProto.PGPMessage message = null;
        try {
            message = PenguardProto.PGPMessage.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return message;
    }
}
