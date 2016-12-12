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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import verteiltesysteme.penguard.guardianservice.GuardService;
import verteiltesysteme.penguard.guardianservice.GuardianServiceConnection;
import verteiltesysteme.penguard.guardianservice.Penguin;

//here the actual guarding happens in case that we will add calibration later there will be another activity between this one and the PenguinSearchActivity

public class GGuardActivity extends AppCompatActivity {

    private static final int UPDATE_DELAY = 500;

    GuardianServiceConnection serviceConnection = new GuardianServiceConnection();

    Button loginB;
    ListView penguinList;

    Handler handler;
    Runnable updateTask;

    boolean paused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gguard);

        penguinList = (ListView) findViewById(R.id.penguinListView);

        loginB = (Button)findViewById(R.id.loginBtn);

        //bind the service
        Intent intent = new Intent(this, GuardService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        handler = new Handler();

        updateTask = new Runnable() {
            @Override
            public void run() {
                if (serviceConnection != null && serviceConnection.isConnected()) {
                    if (penguinList.getAdapter() == null) {
                        serviceConnection.subscribeListViewToPenguinAdapter(penguinList);
                    }
                    updateLoginB();
                    ((ArrayAdapter<Penguin>) penguinList.getAdapter()).notifyDataSetChanged();
                }
                if (!paused) handler.postDelayed(this, UPDATE_DELAY);
            }
        };

        penguinList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Penguin penguin = (Penguin) parent.getItemAtPosition(position);
                Intent intent = new Intent(GGuardActivity.this, GPenguinDetailActivity.class);
                intent.putExtra(GPenguinDetailActivity.EXTRA_PENGUIN_MAC, penguin.getAddress());
                startActivity(intent);
            }
        });
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
        if (view.equals(findViewById(R.id.addPenguinButton))) { // add new penguin
            Intent intent = new Intent(this, GPenguinSearchActivity.class);
            startActivity(intent);
        }
        if (view.equals(findViewById(R.id.joinGroupButton))) { // join another group
            Intent intent = new Intent(this, GGroupJoinActivity.class);
            startActivity(intent);
        }
        if (view.equals(findViewById(R.id.stopServiceButton))) { // stop guardian service
            unbindAndKillService();
        }
        if (view.equals(loginB)){//go to LoginActivity
            Intent intent = new Intent(this, GLoginActivity.class);
            startActivity(intent);
        }
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

    private void updateLoginB(){
        if (serviceConnection.isRegistered()){
            loginB.setEnabled(false);
        }else {
            loginB.setEnabled(true);
        }
    }

    private void debug(String msg) {
        Log.d("GGuard", msg);
    }
}
