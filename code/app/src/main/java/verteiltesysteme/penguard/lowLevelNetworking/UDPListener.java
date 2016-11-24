package verteiltesysteme.penguard.lowLevelNetworking;

import android.provider.ContactsContract;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import static android.R.id.message;

public class UDPListener extends Thread {
    private ListenerCallback onReceiveAction;
    private int port;
    private DatagramSocket socket = null;

    public UDPListener(int port){
        this.port = port;
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
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

    // Do some low-level parsing.
    private Message parseMessage(byte[] data){
        //TODO: Protobuf parsing
        return null;
    }
}
