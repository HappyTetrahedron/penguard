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
//TODO as per the newest specification this activity is unneeded; make sure it isn't used anywhere and then remove it. See Issue #18

public class GJoinActivity extends AppCompatActivity {

    Button joinG, scanP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gjoin);

        //get the UI elements
        joinG = (Button)findViewById(R.id.joinGroupButton);
        scanP =(Button)findViewById(R.id.scanPButton);

        //onclick listener for the two buttons

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
