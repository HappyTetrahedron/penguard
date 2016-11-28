package verteiltesysteme.penguard;

import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;

//here the actual guarding happens in case that we will add calibration later there will be another activity between this one and the PenguinSearchActivity

public class GGuardActivity extends AppCompatActivity {

    static final String EXTRA_DEVICE = "bt_device"; //aka the penguin

    GuardService service;
    boolean isBound = false;

    TextView rssiTextView;
    BluetoothDevice device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gguard);

        rssiTextView = (TextView)findViewById(R.id.rssiTV);

        //bind the service
        Intent intent = new Intent(this, GuardService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        //see if the service is actually bound
        if (isBound){
            debug("service bound");
        }else {
            debug("fuck this shit");
        }

        //unpack the intent
        device = getIntent().getParcelableExtra(EXTRA_DEVICE);
        //service.addPenguin(device); //this adds the device as a penguin to the service

        //enable communication between service and activity i.e. show the rssi of the penguins
        //service.setTV(rssiTextView);

        //let the service do it's work... there needs to be a way to do this better
        //service.doWork();

    }

    private void debug(String msg) {
        Log.d("GGuard", msg);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            GuardService.PenguinGuardBinder binder = (GuardService.PenguinGuardBinder)iBinder;
            service = binder.getService();
            isBound = true;
            debug("derp in the bound thingy");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBound = false;

        }
    };
}
