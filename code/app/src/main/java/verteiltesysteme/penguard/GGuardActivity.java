package verteiltesysteme.penguard;

import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

//here the actual guarding happens in case that we will add calibration later there will be another activity between this one and the PenguinSearchActivity

public class GGuardActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gguard);
    }

    private void debug(String msg) {
        Log.d("GGuard", msg);
    }
}
