package verteiltesysteme.penguard.lowLevelNetworking;

import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

import verteiltesysteme.penguard.protobuf.PenguardProto;

import static android.R.attr.port;

public class UDPListener extends Thread {
    private ListenerCallback onReceiveAction;
    private DatagramSocket socket = null;

    public UDPListener(DatagramSocket datagramSocket){
        socket = datagramSocket;
    }

    // TODO we should allow for multiple callbacks to be registered, and for them to deregister as well.
    public void registerCallback(ListenerCallback onReceiveAction){
        this.onReceiveAction = onReceiveAction;
    }

    @Override
    public void run(){
        byte[] inData = new byte[64];
        DatagramPacket in = new DatagramPacket(inData, inData.length);
        while(!interrupted() && !socket.isClosed()){
            try {
                socket.receive(in);
                inData = in.getData();
                PenguardProto.PGPMessage message = parseMessage(inData);
                if (message != null) { //null messages that couldn't be parsed are ignored
                    onReceiveAction.onReceive(message);
                }
            }
            catch (IOException e) {
                // fail silently
                debug("IOException when receiving packet: " + e.getMessage());
            }
        }
    }

    private int getStartOfTrailingZeros(byte[] data){
        int i;
        for(i=data.length; i > 0; i--) {
            if (data[i-1] != 0) break;
        }
        return i;
    }

    @Nullable
    private PenguardProto.PGPMessage parseMessage(byte[] data){
        PenguardProto.PGPMessage message;
        // Find length in order to truncate trailing zeros
        int length = getStartOfTrailingZeros(data);

        // create truncated array
        byte[] truncated = Arrays.copyOfRange(data, 0, length);
        //TODO truncating might be bad if the message actually HAS a 0 at the end. Not sure how to deal with that.
        try {
            message = PenguardProto.PGPMessage.parseFrom(truncated);
        } catch (InvalidProtocolBufferException e) {
            // fail silently
            return null;
        }
        return message;
    }

    private void debug(String msg) {
        Log.d("UDPListener", msg);
    }
}
