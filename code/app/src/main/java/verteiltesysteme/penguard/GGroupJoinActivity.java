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
import android.widget.TextView;
import android.widget.Toast;

import verteiltesysteme.penguard.guardianservice.GuardService;
import verteiltesysteme.penguard.guardianservice.GuardianServiceConnection;

public class GGroupJoinActivity extends AppCompatActivity {

    Button btn;
    TextView textView;
    EditText editText;

    GuardianServiceConnection guardianServiceConnection = new GuardianServiceConnection();

    GGroupJoinCallback joinCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ggroup_join);

        btn = (Button)findViewById(R.id.groupJoinBT);
        textView = (TextView) findViewById(R.id.groupJoinTV);
        editText = (EditText)findViewById(R.id.groupJoinET);

        Intent intent = new Intent(this, GuardService.class);
        bindService(intent, guardianServiceConnection, Context.BIND_AUTO_CREATE);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                groupJoin();
            }
        });

        joinCallback = new GGroupJoinCallback() {
            @Override
            public void joinSuccessful() {
                toast("Join successful.");
                GGroupJoinActivity.this.finish();
            }

            @Override
            public void joinAccepted() {
                toast("Your Join request was accepted. Updating group...");
            }

            @Override
            public void joinFailure(String error) {
                toast(error);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btn.setEnabled(true); //reenable to button
                    }
                });
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(guardianServiceConnection);
    }

    private void groupJoin(){
        //grey out the button and notify the user that a join is underway
        btn.setEnabled(false);
        toast(getString(R.string.group_join_wait));

        //get the typed in username in the editText
        String groupUN = String.valueOf(editText.getText());

        //call the function in the serviceConnector
        if (guardianServiceConnection.joinGroup(groupUN, joinCallback)){
            //join started successful
        } else {
            //something went wrong
            toast("Unable to join group");
            btn.setEnabled(true); //reenable to button
        }
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

    private void toast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void debug(String msg) {
        Log.d("GGroupJoin", msg);
    }
}
