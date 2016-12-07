package verteiltesysteme.penguard;

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

import verteiltesysteme.penguard.guardianservice.GuardianServiceConnection;

//TODO use this activity to initiate group join by binding to service and calling respective method in there, see issue #17

public class GGroupJoinActivity extends AppCompatActivity {

    Button btn;
    EditText editText;

    GuardianServiceConnection guardianServiceConnection = new GuardianServiceConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ggroup_join);

        btn = (Button)findViewById(R.id.groupJoinBT);
        editText = (EditText)findViewById(R.id.groupJoinET);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                groupJoin();
            }
        });
    }

    private void groupJoin(){
        //grey out the button and notify the user that a join is underway
        btn.setEnabled(false);
        toast(getString(R.string.group_join_wait));

        //get the typed in username in the editText
        String groupUN = String.valueOf(editText.getText());

        //call the function in the serviceConnector
        if (guardianServiceConnection.joinGroup(groupUN)){
            //join started successful
        }else {
            //something went wrong
            toast("Unable to join group"); //TODO maybe make a case distinction
            btn.setEnabled(true); //reenable to button
        }

        //TODO the entire group merge needs to happen and then we need to be able to transition to the next activity

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
