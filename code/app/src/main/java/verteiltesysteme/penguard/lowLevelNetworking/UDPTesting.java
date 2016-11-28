package verteiltesysteme.penguard.lowLevelNetworking;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

import verteiltesysteme.penguard.protobuf.PenguardProto;

public class UDPTesting extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the socket used for dispatching and listening.
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(65535);
        } catch (SocketException e) {
            /* Can occur for various reasons, such as a missing permission, or "Address already in use",
             * i.e. if two sockets are open at the same time on the same port.
             */
            e.printStackTrace();
        }

        DispatcherCallback dispatchAction = new DispatcherCallback() {
            @Override
            public void onSuccess() {
                debug("Dispatched a message");
            }

            @Override
            public void onFailure() {
                debug("Failed to dispatch a message");
            }
        };

        // Create dispatcher. Dispatcher will send to ip 10.0.2.14
        UDPDispatcher dispatcher = new UDPDispatcher(socket);
        dispatcher.registerCallback(dispatchAction);


        ListenerCallback listenAction = new ListenerCallback() {
            @Override
            public void onReceive(PenguardProto.PGPMessage message) {
                debug("Received message: \"" + message.toString() + "\"");
            }
        };

        UDPListener listener = new UDPListener(socket);
        listener.registerCallback(listenAction);

        // Start listening on incoming packages. Every time a package is received, execute the listenerAction.
        listener.start();

        // Dispatch some test package.
        PenguardProto.PGPMessage message = PenguardProto.PGPMessage.newBuilder()
                .setName("Anneliese")
                .setType(PenguardProto.PGPMessage.Type.SG_ACK)
                .setAck(PenguardProto.Ack.newBuilder()
                        .setUuid("beef")
                        .setName("Berta")
                        .setIp("1.2.3.4")
                        .setPort(5500)
                        .build())
                .build();
        try {
            dispatcher.execute(new PGPPacket("10.0.2.15", message));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    private void debug(String message){
        Log.d("UDPTesting", message);
    }

}
