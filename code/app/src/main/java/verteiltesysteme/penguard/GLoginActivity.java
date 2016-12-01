package verteiltesysteme.penguard;

import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
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

        Intent intent = new Intent(this, GuardService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);


        //the onclick listener is only set like this for the purpose of easier implementing the bluetooth stuff without having to worry about networking
        joinB.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View view) {
                joinB.setEnabled(false);

                // Display loading circle.
                findViewById(R.id.loadingCircle).setVisibility(View.VISIBLE);

                // Create a callback for the registration process
                GLoginCallback loginCallback = new GLoginCallback() {
                    @Override
                    public void registrationSuccess() {
                        findViewById(R.id.loadingCircle).setVisibility(View.GONE);
                        Intent intent = new Intent(view.getContext(), GPenguinSearchActivity.class);
                        startActivity(intent);
                        joinB.setEnabled(true);
                    }

                    @Override
                    public void registrationFailure() {
                        findViewById(R.id.loadingCircle).setVisibility(View.GONE);
                        displayToast("Contacting server failed.");
                        joinB.setEnabled(true);
                    }
                };

                // Registration happens in GuardService. We pass a callback that will be executed once the server replies.
                serviceConnection.register(usernameET.getText().toString(), loginCallback);
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

    private void displayToast(String text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
