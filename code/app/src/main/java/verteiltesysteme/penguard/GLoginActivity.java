package verteiltesysteme.penguard;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.net.DatagramSocket;
import java.net.SocketException;

import verteiltesysteme.penguard.lowLevelNetworking.DispatcherCallback;
import verteiltesysteme.penguard.lowLevelNetworking.ListenerCallback;
import verteiltesysteme.penguard.lowLevelNetworking.UDPDispatcher;
import verteiltesysteme.penguard.lowLevelNetworking.UDPListener;
import verteiltesysteme.penguard.protobuf.PenguardProto;

//this activity is used for login in the guard. It is called by the main activity. It recieves an empty intent

public class GLoginActivity extends AppCompatActivity {

    static final String SERVER_IP = "1.1.1.1";
    static final int SERVER_PORT = 65535;
    static final int CLIENT_PORT = 65535;
    static DatagramSocket socket = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glogin);

        final Button joinB = (Button)findViewById(R.id.button3);
        final EditText usernameET = (EditText)findViewById(R.id.editText);
        //TODO: Add spinner
        //final Spinner usernameSpinner = (Spinner)findViewById(R.id.spinner);

        try {
            socket = new DatagramSocket(CLIENT_PORT);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        final UDPListener listener = new UDPListener(socket);
        final UDPDispatcher dispatcher = new UDPDispatcher(socket);

        ListenerCallback listenerAction = new ListenerCallback() {
            @Override
            public void onReceive(PenguardProto.PGPMessage parsedMessage) {
                PenguardProto.PGPMessage.Type answerType = parsedMessage.getType();
                switch(answerType){
                    case SG_ACK:
                        registerSuccessful(usernameET.getText().toString());
                        break;
                    case SG_ERR:
                        registerUnsuccessful(usernameET.getText().toString(), parsedMessage.getError());
                        break;
                    default:
                        // Do nothing, we caught a package that was not meant for this activity.
                        break;
                }
            }
        };

        DispatcherCallback dispatcherAction = new DispatcherCallback() {
            @Override
            public void onSuccess() {
                //TODO: Probably do nothing in here. If I feel *very* motivated, I'll implement an hourglass animation/popup or something.
            }

            @Override
            public void onFailure(int errorCode) {
                displayToast("Contacting server failed");
            }
        };

        listener.registerCallback(listenerAction);
        dispatcher.registerCallback(dispatcherAction);


        //the onclick listener is only set like this for the purpose of easier implementing the bluetooth stuff without having to worry about networking
        //TODO the code below has to be adapted to contact the server etc once the PGP is established
        joinB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PenguardProto.PGPMessage registerMessage = PenguardProto.PGPMessage.newBuilder()
                        .setType(PenguardProto.PGPMessage.Type.GS_REGISTER)
                        .setName(usernameET.getText().toString())
                        .build();
                dispatcher.sendPacket(registerMessage, SERVER_IP, SERVER_PORT);
            }
        });

    }

    @Override
    public void onDestroy(){

    }

    private void displayToast(String text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT);
    }

    private void registerSuccessful(String username){
        // TODO: Server replied 'success', now we need to switch activity. We'll also need to add the username to our spinner list thingy and add the UUID to the settings.
        Intent intent = new Intent(this, GJoinActivity.class).putExtra("username", username);
        startActivity(intent);
    }

    private void registerUnsuccessful(String username, PenguardProto.Error error){
        // TODO: Server replied 'failure', now we need to display a Toast or similar.
        Toast.makeText(this, "Username already in use", Toast.LENGTH_SHORT);
    }

}
