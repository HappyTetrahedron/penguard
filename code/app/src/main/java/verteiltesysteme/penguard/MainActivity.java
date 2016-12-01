package verteiltesysteme.penguard;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import verteiltesysteme.penguard.guardianservice.GuardService;
import verteiltesysteme.penguard.lowLevelNetworking.UDPTesting;

public class MainActivity extends AppCompatActivity {

    Button b1, b2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        b1 = (Button)findViewById(R.id.button); //UDP will later be called the i'm a guard
        b2 = (Button)findViewById(R.id.button2); //bluetooth //will later be called i'm a penguin

        /* Not sure if I'm supposed to but, put I put my UDPTesting activity here, so that when I click the
         * button I can test whether my UDP implementation works. If anyone needs the button for something else,
         * feel free to just remove it. --Nils
         */
        b1.setOnClickListener(new View.OnClickListener() { @Override
        public void onClick(View view) {
            Intent intent = new Intent(view.getContext(), UDPTesting.class);
            startActivity(intent);

        }
        });

        b2.setOnClickListener(new View.OnClickListener() { @Override
            public void onClick(View view) {
                Intent serviceIntent = new Intent(view.getContext(), GuardService.class);
                startService(serviceIntent);
                Intent intent = new Intent(view.getContext(), GLoginActivity.class);
                startActivity(intent);

            }
        });

        //TODO check whether GuardService is running. If so, transition directly to guard activity. See issue #21
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
        Log.d("Penguard_main", msg);
    }
}
