package verteiltesysteme.penguard;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.security.Guard;

import verteiltesysteme.penguard.guardianservice.GuardService;
import verteiltesysteme.penguard.guardianservice.GuardianServiceConnection;

//this activity is used for login in the guard. It is called by the main activity. It recieves an empty intent

public class GLoginActivity extends AppCompatActivity {

    Button joinB;
    EditText usernameET;

    GuardianServiceConnection serviceConnection = new GuardianServiceConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glogin);

        //TODO bind to sevoidrvice
        Intent intent = new Intent(this, GuardService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        //get the UI elements
        joinB = (Button)findViewById(R.id.button3);
        usernameET = (EditText)findViewById(R.id.editText);

        //the onclick listener is only set like this for the purpose of easier implementing the bluetooth stuff without having to worry about networking
        //TODO the code below has to be adapted to contact the server etc once the PGP is estblished
        joinB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serviceConnection.register(usernameET.getText().toString());
                // TODO display a loading circle until the registration is complete. Only then, transition to the activity. Maybe use a callback.
                Intent intent = new Intent(view.getContext(), GJoinActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
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

    private void debug(String msg) {
        Log.d("GLogin", msg);
    }
}
