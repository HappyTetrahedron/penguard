package verteiltesysteme.penguard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import verteiltesysteme.penguard.guardianservice.GuardService;
import verteiltesysteme.penguard.guardianservice.GuardianServiceConnection;
import verteiltesysteme.penguard.guardianservice.Penguin;

import static verteiltesysteme.penguard.R.string.peng;
import static verteiltesysteme.penguard.R.string.penguard_active;
import static verteiltesysteme.penguard.R.string.penguin;

public class GCalibrationActivity extends AppCompatActivity {

    public static String CALIBRATION_INTENT_IDENTIFIER = "calibration_result";
    private ToggleButton calibrationToggle;
    private int[] calibratedValues = new int[2];
    private Penguin penguin;
    private GuardianServiceConnection serviceConnection;

    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_gcalibration);
        serviceConnection = new GuardianServiceConnection();
        serviceConnection.registerServiceConnectedCallback(new Runnable() {
            @Override
            public void run() {
                readPenguinFromMac();
            }
        });
        Intent intent = new Intent(this, GuardService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        calibrationToggle = (ToggleButton) findViewById(R.id.calibrationToggle);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unbindService(serviceConnection);
    }

    public void onClick(View view) {
        if (view == calibrationToggle && serviceConnection.isConnected()) {
            /* Check if service is connected to this class and if it is, get our penguin.
             * It would be much nicer to have a callback that gets executed as soon as the serviceConnection
             * is available. I'm currently too lazy for that though.
             */

            if (calibrationToggle.isChecked()) {
                // Start calibrating.
                calibratedValues[0] = penguin.getRssi();
                debug("Penguin has low value: " + calibratedValues[0]);
            }
            else {
                // Stop calibrating.
                calibratedValues[1] = penguin.getRssi();
                debug("Penguin has high value: " + calibratedValues[1]);
                Intent result = new Intent();
                result.putExtra(CALIBRATION_INTENT_IDENTIFIER, calibratedValues);
                setResult(RESULT_OK, result);
            }
        }
    }

    private void readPenguinFromMac(){
        String penguardMac = getIntent().getStringExtra(GPenguinDetailActivity.EXTRA_PENGUIN_MAC);
        penguin = serviceConnection.getPenguinById(penguardMac);
    }

    private void toast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void debug(String msg) {
        Log.d("GCalibrationActivity", msg);
    }

}
