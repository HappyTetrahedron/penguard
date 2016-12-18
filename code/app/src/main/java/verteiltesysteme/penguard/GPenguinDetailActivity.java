package verteiltesysteme.penguard;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Arrays;

import verteiltesysteme.penguard.guardianservice.GuardService;
import verteiltesysteme.penguard.guardianservice.GuardianServiceConnection;
import verteiltesysteme.penguard.guardianservice.Penguin;
import verteiltesysteme.penguard.guardianservice.TwoPhaseCommitCallback;

public class GPenguinDetailActivity extends StatusActivity {

    private String penguinMac;

    public static final String EXTRA_PENGUIN_MAC = "penguin_mac";
    private static final int PENGUIN_CALIBRATION_REQUEST = 1;

    private TextView penguinName;
    private TextView penguinInfo;
    private Button calibrateButton;
    private Button removeButton;
    private Penguin penguin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpenguin_detail);

        serviceConnection = new GuardianServiceConnection();
        serviceConnection.registerServiceConnectedCallback(new Runnable() {
            @Override
            public void run() {
                penguin = serviceConnection.getPenguinById(penguinMac);
                if (penguin != null) {
                    serviceConnection.stopAlarm(penguin);
                }
                else {// penguin was removed in the meantime. nothing left to do here.
                    finish();
                }
            }
        });
        Intent intent = new Intent(this, GuardService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        Intent callingIntent = getIntent();
        penguinMac = callingIntent.getStringExtra(EXTRA_PENGUIN_MAC);

        penguinName = (TextView) findViewById(R.id.penguinNameTV);
        penguinInfo = (TextView) findViewById(R.id.penguinInfoTV);
        calibrateButton = (Button) findViewById(R.id.calibrationBtn);
        removeButton = (Button) findViewById(R.id.removePenguinBtn);
    }

    @Override
    void updateState() {
        if (serviceConnection != null && serviceConnection.isConnected()) {
            if (penguin != null) {
                penguinName.setText(penguin.getName());
                penguinInfo.setText(serviceConnection.getPenguinSeenByString(penguinMac));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceConnection != null && serviceConnection.isConnected()) {
            unbindService(serviceConnection);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (requestCode == PENGUIN_CALIBRATION_REQUEST){
            if (resultCode == RESULT_OK) {
                int[] calibrationResults = result.getIntArrayExtra(GCalibrationActivity.CALIBRATION_INTENT_IDENTIFIER);
                penguin.setCalibratedValues(calibrationResults);
                debug("Got calibration result: " + Arrays.toString(calibrationResults));
            }
        }
    }

    public void onClick(View view) {
        if (view.equals(removeButton)) {
            TwoPhaseCommitCallback callback = new TwoPhaseCommitCallback() {
                @Override
                public void onCommit(String message) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toast(getString(R.string.penguinRemoveSucceeded));
                        }
                    });
                }
                @Override
                public void onAbort(String error) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toast(getString(R.string.penguinRemoveFailed));
                        }
                    });
                }
            };
            serviceConnection.removePenguin(penguinMac, callback);
            finish();
        }
        else if(view.equals(calibrateButton)) {
            Intent intent = new Intent(this, GCalibrationActivity.class);
            intent.putExtra(EXTRA_PENGUIN_MAC, penguinMac);
            startActivityForResult(intent, PENGUIN_CALIBRATION_REQUEST);
        }
    }
}
