package verteiltesysteme.penguard;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import verteiltesysteme.penguard.Settings.SettingsActivity;
import verteiltesysteme.penguard.guardianservice.GuardService;
import verteiltesysteme.penguard.guardianservice.GuardianServiceConnection;

public abstract class PenguardActivity extends AppCompatActivity {

    protected GuardianServiceConnection serviceConnection = new GuardianServiceConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    protected void toast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    protected void debug(String msg) {
        Log.d(this.getClass().getName(), msg);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(isMyServiceRunning(GuardService.class)) {
            getMenuInflater().inflate(R.menu.menu_endservice, menu);
        }
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

    protected void unbindAndKillService(){
        Intent backToMainIntent = new Intent(this, MainActivity.class);
        // clear the backstack when transitioning to main activity
        backToMainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        Intent stopServiceIntent = new Intent(this, GuardService.class);

        if (serviceConnection != null && serviceConnection.isConnected()) {
            unbindService(serviceConnection);
        }
        serviceConnection = null;
        stopService(stopServiceIntent);
        startActivity(backToMainIntent);
    }
}
