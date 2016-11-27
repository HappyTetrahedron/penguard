package verteiltesysteme.penguard.lowLevelNetworking;

import android.os.AsyncTask;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import verteiltesysteme.penguard.protobuf.PenguardProto;

public class UDPDispatcher extends AsyncTask<PenguardProto.Message, Void, Boolean> {
    private int port;
    private String ip;
    private DispatcherCallback onPostAction;
    private DatagramSocket socket;

    public UDPDispatcher(String ip, int port) {
        this.port = port;
        this.ip = ip;
        try{
            socket = new DatagramSocket(port);
        }
        catch(java.net.SocketException e){
            e.printStackTrace();
        }
    }

    public void registerCallback(DispatcherCallback onPostAction) {
        this.onPostAction = onPostAction;
    }

    @Override
    public Boolean doInBackground(PenguardProto.Message... params){
        for(PenguardProto.Message message : params){
            try {
                byte[] outData = message.toByteArray();
                InetAddress receiverIP = InetAddress.getByName(ip);
                DatagramPacket outPacket = new DatagramPacket(outData, outData.length, receiverIP, port);
                socket.send(outPacket);
                socket.close();
            }
            catch(java.io.IOException e){
                return false;
            }
        }
        return true;

    }

    @Override
    protected void onPostExecute(Boolean success){
        onPostAction.setSuccess(success);
        onPostAction.run();
    }
}
