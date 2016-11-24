package verteiltesysteme.penguard.lowLevelNetworking;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class UDPTesting extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        UDPDispatcher dispatcher = new UDPDispatcher("Ping from dispatcher", "10.0.2.15", 65535);
        dispatcher.registerCallback(dispatchAction);
        ListenerCallback listenAction = new ListenerCallback() {
            @Override
            public void onReceive(Message message) {
                debug("Received message: \"" + message.getContent() + "\"");
            }
        };
        UDPListener listener = new UDPListener(65535);
        listener.registerCallback(listenAction);

        listener.start();
        dispatcher.execute(new Message("Ping from dispatcher"));

    }

    private void debug(String message){
        Log.d("UDPTesting", message);
    }

}
