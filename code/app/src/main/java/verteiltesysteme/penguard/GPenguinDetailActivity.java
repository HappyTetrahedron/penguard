package verteiltesysteme.penguard;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

import verteiltesysteme.penguard.Settings.SettingsActivity;

import verteiltesysteme.penguard.guardianservice.GuardService;
import verteiltesysteme.penguard.guardianservice.GuardianServiceConnection;
import verteiltesysteme.penguard.guardianservice.Penguin;
import verteiltesysteme.penguard.guardianservice.TwoPhaseCommitCallback;

public class GPenguinDetailActivity extends AppCompatActivity {

    private GuardianServiceConnection serviceConnection;
    private String penguinMac;

    public static final String EXTRA_PENGUIN_MAC = "penguin_mac";
    private static final int PENGUIN_CALIBRATION_REQUEST = 1;

    private TextView penguinName;
    private TextView penguinInfo;
    private Button calibrateButton;
    private Button removeButton;
    private Penguin penguin;

    private Handler handler;
    private Runnable updateTask;

    private boolean paused = true;

    private final int UPDATE_DELAY = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpenguin_detail);

        serviceConnection = new GuardianServiceConnection();
        serviceConnection.registerServiceConnectedCallback(new Runnable() {
            @Override
            public void run() {
                readPenguinFromMac();
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

        handler = new Handler();

        updateTask = new Runnable() {
            @Override
            public void run() {
                if (serviceConnection != null && serviceConnection.isConnected()) {
                    penguinName.setText(penguin.getName());
                    penguinInfo.setText(serviceConnection.getPenguinSeenByString(penguinMac));
                }
                if (!paused) handler.postDelayed(this, UPDATE_DELAY);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        paused = false;
        handler.post(updateTask);
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        getMenuInflater().inflate(R.menu.menu_howto, menu);
        getMenuInflater().inflate(R.menu.menu_endservice, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_howto:
                Intent intent1 = new Intent(this, HowToActivity.class);
                startActivity(intent1);
                return true;
            case R.id.menu_endService:
                unbindAndKillService();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onClick(View view) {
        if (view.equals(removeButton)) {
            TwoPhaseCommitCallback callback = new TwoPhaseCommitCallback() {
                @Override
                public void onCommit(String message) {
                    debug(message);
                }

                @Override
                public void onAbort(String error) {
                    debug(error);
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

    private void readPenguinFromMac(){
        penguin = serviceConnection.getPenguinById(penguinMac);
    }

    private void unbindAndKillService(){
        Intent backToMainIntent = new Intent(this, MainActivity.class);
        // clear the backstack when transitioning to main activity
        backToMainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        Intent stopServiceIntent = new Intent(this, GuardService.class);

        unbindService(serviceConnection);
        serviceConnection = null;
        stopService(stopServiceIntent);
        startActivity(backToMainIntent);
    }

    private void debug(String msg) {
        Log.d("GPenguinDetail", msg);
    }
}
