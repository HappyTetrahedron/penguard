package verteiltesysteme.penguard.lowLevelNetworking;

import android.os.AsyncTask;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import verteiltesysteme.penguard.protobuf.PenguardProto;

public class UDPDispatcher extends AsyncTask<PGPPacket, Void, Boolean> {
    private DispatcherCallback onPostAction;
    private DatagramSocket socket;

    public UDPDispatcher(DatagramSocket datagramSocket) {
        socket = datagramSocket;
    }

    public void registerCallback(DispatcherCallback onPostAction) {
        this.onPostAction = onPostAction;
    }

    @Override
    public Boolean doInBackground(PGPPacket... params){
        for(PGPPacket packet : params){
            try {
                PenguardProto.PGPMessage message = packet.message;
                byte[] outData = message.toByteArray();
                InetAddress receiverIP = packet.ipAddr;
                // TODO: Probably get rid of PGPPacket actually. Port and Ip can be read from PGPMessage.
                DatagramPacket outPacket = new DatagramPacket(outData, outData.length, receiverIP, socket.getPort());
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
