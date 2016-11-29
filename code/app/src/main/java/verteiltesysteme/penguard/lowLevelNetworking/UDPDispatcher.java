package verteiltesysteme.penguard.lowLevelNetworking;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import verteiltesysteme.penguard.protobuf.PenguardProto;

public class UDPDispatcher{
    private ArrayList<DispatcherCallback> callbacks;
    private DatagramSocket socket;

    public static final int ERROR_SENDING_PACKET = 1;
    public static final int ERROR_UNKNOWN_HOST = 2;

    //TODO We might need to send our package over multiple sockets, e.g. wifi and gsm
    public UDPDispatcher(DatagramSocket datagramSocket) {
        socket = datagramSocket;
    }

    public void registerCallback(DispatcherCallback onPostAction) {
        callbacks.add(onPostAction);
    }

    public void unregisterCallback(DispatcherCallback deregisteredCallback) {
        callbacks.remove(deregisteredCallback);
    }

    // Sends a PGPMessage to the given IP and port.
    public void sendPacket(PenguardProto.PGPMessage message, String ip, int port){
        for(DispatcherCallback callback : callbacks) {
            new Thread(new NetworkingTask(message, ip, port, callback)).start();
        }
    }

    // internal class that can be used to send a single packet
    class NetworkingTask implements Runnable {

        PenguardProto.PGPMessage message;
        String ip;
        int port;
        DispatcherCallback callback;

        NetworkingTask(PenguardProto.PGPMessage message, String ip, int port, DispatcherCallback callback) {
            this.message = message;
            this.ip = ip;
            this.port = port;
            this.callback = callback;
        }

        @Override
        public void run() {
            byte[] outData = message.toByteArray();
            try {
                InetAddress inetAddr = InetAddress.getByName(ip);
                DatagramPacket outPacket = new DatagramPacket(outData, outData.length, inetAddr, port);
                socket.send(outPacket);
            } catch (UnknownHostException e) {
                callback.onFailure(ERROR_UNKNOWN_HOST);
            } catch (java.io.IOException e) {
                callback.onFailure(ERROR_SENDING_PACKET);
            }
            callback.onSuccess();
        }
    }

    private void debug(String msg){
        Log.d("UDPDispatcher", msg);
    }
}
