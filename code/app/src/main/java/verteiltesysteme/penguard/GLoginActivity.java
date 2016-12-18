package verteiltesysteme.penguard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import verteiltesysteme.penguard.guardianservice.GuardService;
import verteiltesysteme.penguard.guardianservice.LoginCallback;


//this activity is used for login in the guard. It is called by the main activity. It receives an empty intent

public class GLoginActivity extends PenguardActivity {

    String lastUUID;
    String lastUN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glogin);

        final Button joinB = (Button)findViewById(R.id.button3);
        final EditText usernameET = (EditText)findViewById(R.id.editText);
        final Button skipB = (Button)findViewById(R.id.skipB);

        final Intent intent = new Intent(this, GuardService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        //set the textview
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        lastUN = sharedPref.getString(getString(R.string.pref_key_username), getString(R.string.pref_default_username));
        lastUUID = sharedPref.getString(getString(R.string.pref_key_uuid), getString(R.string.pref_default_uuid));
        debug("last uuid " +lastUUID);

        usernameET.setText(lastUN);


        //the onclick listener is only set like this for the purpose of easier implementing the bluetooth stuff without having to worry about networking
        joinB.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View view) {
                if (usernameET.getText().toString().equals("")) {
                    return;
                }

               // Create a callback for the registration process
                LoginCallback loginCallback = new LoginCallback() {
                    @Override
                    public void registrationSuccess(String uuid) {
                        // We can only manipulate the loading circle and button from the UI thread
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.loadingCircle).setVisibility(View.GONE);
                                joinB.setEnabled(true);
                            }
                        });

                        //update the username in the settings
                        debug("updating the username in the seetings: "+usernameET.getText().toString());
                        sharedPref.edit()
                                .putString(getString(R.string.pref_key_username), usernameET.getText().toString())
                                .putString(getString(R.string.pref_key_uuid), uuid)
                                .apply();

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
                                toast(error);
                                joinB.setEnabled(true);
                            }
                        });
                    }
                };

                debug("last username: " + lastUN + ". Current un: " + usernameET.getText() + ". Equal: " + usernameET.getText().toString());
                if (lastUUID.equals("") || !lastUN.equals(usernameET.getText().toString())) { // Register a new user
                    debug("Registering as new user");
                    // Registration happens in GuardService. We pass a callback that will be executed once the server replies.
                    if (serviceConnection.register(usernameET.getText().toString(), loginCallback)) { // the registration process started successfully
                        // disable join button to prevent spamming it
                        joinB.setEnabled(false);
                        // Display loading circle.
                        findViewById(R.id.loadingCircle).setVisibility(View.VISIBLE);

                        if (!lastUUID.equals("")) { //We have registered previously but changed username
                            serviceConnection.deregister(lastUN, lastUUID);
                        }

                    } else { // registration process did not start
                        debug("Registering as existing user");
                        toast(getString(R.string.alreadyRegistered));
                    }

                }
                else { // re-registering an existing user
                    // Registration happens in GuardService. We pass a callback that will be executed once the server replies.
                    if (serviceConnection.reregister(usernameET.getText().toString(), lastUUID, loginCallback)) { // the registration process started successfully
                        // disable join button to prevent spamming it
                        joinB.setEnabled(false);
                        // Display loading circle.
                        findViewById(R.id.loadingCircle).setVisibility(View.VISIBLE);
                    } else { // registration process did not start
                        toast(getString(R.string.alreadyRegistered));
                    }
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
        if (serviceConnection != null && serviceConnection.isConnected()) {
            unbindService(serviceConnection);
        }
    }
}
