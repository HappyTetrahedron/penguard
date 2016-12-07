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
import android.widget.Toast;

import verteiltesysteme.penguard.guardianservice.GuardService;
import verteiltesysteme.penguard.guardianservice.GuardianServiceConnection;

//this activity is used for login in the guard. It is called by the main activity. It receives an empty intent

public class GLoginActivity extends AppCompatActivity {

    GuardianServiceConnection serviceConnection = new GuardianServiceConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glogin);

        final Button joinB = (Button)findViewById(R.id.button3);
        final EditText usernameET = (EditText)findViewById(R.id.editText);
        final Button skipB = (Button)findViewById(R.id.skipB);

        final Intent intent = new Intent(this, GuardService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);


        //the onclick listener is only set like this for the purpose of easier implementing the bluetooth stuff without having to worry about networking
        joinB.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View view) {

               // Create a callback for the registration process
                GLoginCallback loginCallback = new GLoginCallback() {
                    @Override
                    public void registrationSuccess() {
                        // We can only manipulate the loading circle and button from the UI thread
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.loadingCircle).setVisibility(View.GONE);
                                joinB.setEnabled(true);
                            }
                        });

                        // Start next activity
                        Intent intent = new Intent(view.getContext(), GGuardActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                   }

                    @Override
                    public void registrationFailure(final String error) {
                        // We can only manipulate the loading circle and button from the UI thread
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.loadingCircle).setVisibility(View.GONE);
                                displayToast(error);
                                joinB.setEnabled(true);
                            }
                        });
                    }
                };

                // Registration happens in GuardService. We pass a callback that will be executed once the server replies.
                if(serviceConnection.register(usernameET.getText().toString(), loginCallback)) { // the registration process started successfully
                    // disable join button to prevent spamming it
                    joinB.setEnabled(false);
                    // Display loading circle.
                    findViewById(R.id.loadingCircle).setVisibility(View.VISIBLE);
                }
                else { // registration process did not start
                    displayToast("You are already registered");
                }
            }
        });

        skipB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentSkip = new Intent(view.getContext(), GGuardActivity.class);
                startActivity(intentSkip);
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

    private void displayToast(String text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private void debug(String msg) {
        Log.d("GLoginActivity", msg);
    }
}
