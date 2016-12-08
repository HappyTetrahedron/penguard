package verteiltesysteme.penguard;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.security.Guard;

import verteiltesysteme.penguard.guardianservice.GuardService;
import verteiltesysteme.penguard.lowLevelNetworking.UDPTesting;

public class MainActivity extends AppCompatActivity {

    Button b1, b2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Do not display MainActivity if GuardService is on
        if (isMyServiceRunning(GuardService.class)) {
            Intent intent = new Intent(this, GGuardActivity.class);
            // add flags to clear this MainActivity from the stack
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        }

        setContentView(R.layout.activity_main);

        b1 = (Button)findViewById(R.id.button); //i'm a guard
        b2 = (Button)findViewById(R.id.button2); //i'm a penguin

        /* Not sure if I'm supposed to but, put I put my UDPTesting activity here, so that when I click the
         * button I can test whether my UDP implementation works. If anyone needs the button for something else,
         * feel free to just remove it. --Nils
         */
        b2.setOnClickListener(new View.OnClickListener() { @Override
        public void onClick(View view) {
            /*Intent intent = new Intent(view.getContext(), GGroupJoinActivity.class); //TODO i changed this for the purpose of doing #17 since we do not have the server yet and the intent flow is sort of weird too
            startActivity(intent);*/
            toast("Being a penguin is not implemented yet...sorry");

        }
        });

        b1.setOnClickListener(new View.OnClickListener() { @Override
            public void onClick(View view) {
                Intent serviceIntent = new Intent(view.getContext(), GuardService.class);
                startService(serviceIntent);
                Intent intent = new Intent(view.getContext(), GLoginActivity.class);
                startActivity(intent);

            }
        });

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void toast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    private void debug(String msg) {
        Log.d("Penguard_main", msg);
    }
}
