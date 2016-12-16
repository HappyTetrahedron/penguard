package verteiltesysteme.penguard;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import verteiltesysteme.penguard.guardianservice.GroupJoinCallback;
import verteiltesysteme.penguard.guardianservice.GuardService;

public class GGroupJoinActivity extends PenguardActivity {

    Button btn;
    TextView textView;
    EditText editText;

    GroupJoinCallback joinCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ggroup_join);

        btn = (Button)findViewById(R.id.groupJoinBT);
        textView = (TextView) findViewById(R.id.groupJoinTV);
        editText = (EditText)findViewById(R.id.groupJoinET);

        Intent intent = new Intent(this, GuardService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                groupJoin();
            }
        });

        joinCallback = new GroupJoinCallback() {
            @Override
            public void joinSuccessful() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toast(getString(R.string.joinSuc));
                    }
                });
                GGroupJoinActivity.this.finish();
            }

            @Override
            public void joinAccepted() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toast(getString(R.string.joinAcept));
                    }
                });
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
        if (serviceConnection != null && serviceConnection.isConnected()) {
            unbindService(serviceConnection);
        }
    }

    private void groupJoin(){
        //grey out the button and notify the user that a join is underway
        btn.setEnabled(false);
        toast(getString(R.string.group_join_wait));

        //get the typed in username in the editText
        String groupUN = String.valueOf(editText.getText());

        //call the function in the serviceConnector
        if (! serviceConnection.joinGroup(groupUN, joinCallback)){
            //something went wrong, so notify user and re-enable the button
            joinCallback.joinFailure(getString(R.string.toast_merge_failed_progress));
            btn.setEnabled(true);
        }
    }
}
