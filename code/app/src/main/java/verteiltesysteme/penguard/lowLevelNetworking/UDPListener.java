package verteiltesysteme.penguard.lowLevelNetworking;

import android.support.annotation.Nullable;
import android.util.Log;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;

import verteiltesysteme.penguard.protobuf.PenguardProto;


public class UDPListener extends Thread {
    private ArrayList<ListenerCallback> callbacks;
    private DatagramSocket socket = null;

    public UDPListener(DatagramSocket datagramSocket){
        callbacks = new ArrayList<>();
        socket = datagramSocket;
    }

    public void registerCallback(ListenerCallback onReceiveAction){
        callbacks.add(onReceiveAction);
    }

    public void unregisterCallback(ListenerCallback deregisteredCallback) {
        callbacks.remove(deregisteredCallback);
    }

    @Override
    public void run(){
        byte[] inData = new byte[1024];
        DatagramPacket in = new DatagramPacket(inData, inData.length);
        while(!interrupted() && !socket.isClosed()){
            try {
                socket.receive(in);
                inData = in.getData();
                PenguardProto.PGPMessage message = parseMessage(inData);
                if (message != null) { //null messages that couldn't be parsed are ignored
                    for(ListenerCallback callback : callbacks) {
                        callback.onReceive(message, in.getAddress(), in.getPort());
                    }
                }
            }
            catch (IOException e) {
                // fail silently
                debug("IOException when receiving packet: " + e.getMessage());
            }
        }
    }

    @Nullable
    private PenguardProto.PGPMessage parseMessage(byte[] data){
        PenguardProto.PGPMessage message;

        ByteArrayInputStream in = new ByteArrayInputStream(data);

        try {
            message = PenguardProto.PGPMessage.parseDelimitedFrom(in);
        } catch (InvalidProtocolBufferException e) {
            debug("Invalid protobuf: " + e.getMessage());
            return null;
        } catch (IOException e) {
            debug("IOException: " + e.getMessage());
            return null;
        }
        return message;
    }

    private void debug(String msg) {
        Log.d("UDPListener", msg);
    }
}
