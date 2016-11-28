package verteiltesysteme.penguard.lowLevelNetworking;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.net.DatagramSocket;
import java.net.SocketException;

import verteiltesysteme.penguard.R;
import verteiltesysteme.penguard.protobuf.PenguardProto;

public class UDPTesting extends AppCompatActivity {

    DatagramSocket socket;
    PenguardProto.PGPMessage message;
    UDPDispatcher dispatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_udptest);

        ((Button) findViewById(R.id.testbutton)).setText("TEST");

        // Create the socket used for dispatching and listening.
        socket = null;
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
            public void onFailure(int error) {
                debug("Failed to dispatch a message: " + error);
            }
        };

        // Create dispatcher.
        dispatcher = new UDPDispatcher(socket);
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
        message = PenguardProto.PGPMessage.newBuilder()
                .setName("Anneliese")
                .setType(PenguardProto.PGPMessage.Type.SG_ACK)
                .setAck(PenguardProto.Ack.newBuilder()
                        .setUuid("beef")
                        .setName("Berta")
                        .setIp("1.2.3.4")
                        .setPort(5500)
                        .build())
                .build();
        dispatcher.sendPacket(message, "127.0.0.1", 65535);

    }

    public void onClick(View view) {
        dispatcher.sendPacket(message, "127.0.0.1", 65535);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.close();
    }

    private void debug(String message){
        Log.d("UDPTesting", message);
    }

}
