package verteiltesysteme.penguard;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import verteiltesysteme.penguard.guardianservice.GuardService;
import verteiltesysteme.penguard.guardianservice.GuardianServiceConnection;

//here the actual guarding happens in case that we will add calibration later there will be another activity between this one and the PenguinSearchActivity

public class GGuardActivity extends AppCompatActivity {

    GuardianServiceConnection serviceConnection = new GuardianServiceConnection();

    TextView rssiTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gguard);

        rssiTextView = (TextView)findViewById(R.id.rssiTV);

        //bind the service
        Intent intent = new Intent(this, GuardService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceConnection != null && serviceConnection.isConnected()) {
            unbindService(serviceConnection);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onClick(View view) {
        if (view.equals(findViewById(R.id.addPenguinButton))) { // add new penguin
            Intent intent = new Intent(this, GPenguinSearchActivity.class);
            startActivity(intent);
        }
        if (view.equals(findViewById(R.id.joinGroupButton))) { // join another group
            Intent intent = new Intent(this, GGroupJoinActivity.class);
            startActivity(intent);
        }
        if (view.equals(findViewById(R.id.stopServiceButton))) { // stop guardian service
            Intent backToMainIntent = new Intent(this, MainActivity.class);
            // clear the backstack when transitioning to main activity
            backToMainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            Intent stopServiceIntent = new Intent(this, GuardService.class);

            unbindService(serviceConnection);
            serviceConnection = null;
            stopService(stopServiceIntent);
            startActivity(backToMainIntent);
        }
    }

    private void debug(String msg) {
        Log.d("GGuard", msg);
    }
}
