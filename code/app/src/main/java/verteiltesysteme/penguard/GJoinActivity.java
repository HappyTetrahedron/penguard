package verteiltesysteme.penguard;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

//here the guard can choose if he wants to simply guard a penguin alone or join an already existing group

public class GJoinActivity extends AppCompatActivity {

    Button joinG, scanP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gjoin);

        //get the UI elements
        joinG = (Button)findViewById(R.id.joinGroupButton);
        scanP =(Button)findViewById(R.id.scanPButton);

        //TODO get the uuid from the intent and send it on

        //onclick listener for the two buttons
        //TODO add the uuid to the intent this is left away here for the time being

        joinG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), GGroupJoinActivity.class);
                startActivity(intent);
            }
        });

        scanP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), GPenguinSearchActivity.class);
                startActivity(intent);
            }
        });
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
        Log.d("GJoin", msg);
    }
}
