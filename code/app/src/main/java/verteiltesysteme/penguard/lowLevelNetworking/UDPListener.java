package verteiltesysteme.penguard.lowLevelNetworking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDPListener extends Thread {
    private ListenerCallback listener;
    private int port;

    public UDPListener(int port, ListenerCallback listener){
        this.listener = listener;
        this.port = port;
    }

    @Override
    public void run(){
        byte[] inData = new byte[64];
        DatagramSocket socket;
        try {
            socket = new DatagramSocket(port);
            DatagramPacket in = new DatagramPacket(inData, inData.length);
            socket.receive(in);
            String message = new String(in.getData(), 0, in.getLength());
            listener.setParsedMessage(parseMessage(message));
            listener.start();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Do some low-level parsing. For now simply returns a message object.
    private Message parseMessage(String string){
        return new Message(string);
    }
}
