package verteiltesysteme.penguard;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;


import verteiltesysteme.penguard.guardianservice.GuardService;
import verteiltesysteme.penguard.guardianservice.Guardian;
import verteiltesysteme.penguard.guardianservice.GuardianServiceConnection;
import verteiltesysteme.penguard.guardianservice.MergeRequestArrayAdapter;
import verteiltesysteme.penguard.protobuf.PenguardProto;

public class GGroupMergeRequestsActivity extends AppCompatActivity {

    GuardianServiceConnection serviceConnection = new GuardianServiceConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ggroup_merge_requests);
        Context context = getApplicationContext();
        SharedPreferences sharedMergeRequests = context.getSharedPreferences(
                getString(R.string.group_merge_request_list_file), Context.MODE_PRIVATE);
        String pendingMergeRequests = sharedMergeRequests.getString(getString(R.string.group_merge_request_list), "");
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<PenguardProto.PGPMessage>>(){}.getType();
        final ArrayList<PenguardProto.PGPMessage> mergerequests = gson.fromJson(pendingMergeRequests, type);

        ListView mergerequestlist = (ListView) findViewById(R.id.mergeRequestList);

        //used my own ArrayAdapter according to this: http://stackoverflow.com/questions/2265661/how-to-use-arrayadaptermyclass
        //so i could just display the name instead of the whole message
        MergeRequestArrayAdapter mergeRequestAdapter = new MergeRequestArrayAdapter(getApplicationContext(), mergerequests);
        mergerequestlist.setAdapter(mergeRequestAdapter);

        // register onClickListener to handle click events on each item
        mergerequestlist.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            // argument position gives the index of item which is clicked
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3)
            {
                final PenguardProto.PGPMessage selectedmessage = mergerequests.get(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(getApplication());
                builder.setMessage(getText(R.string.dialog_text_merge_request) + selectedmessage.getName() + "?")
                        .setTitle(R.string.dialog_title_merge_request);
                final AlertDialog dialog = builder.create();

                builder.setPositiveButton(R.string.dialog_ok_merge_request, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        serviceConnection.sendGroupTo(
                                selectedmessage.getMergeReq().getIp(),
                                selectedmessage.getMergeReq().getPort());
                    }
                });

                builder.setNegativeButton(R.string.dialog_cancel_merge_request, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                    }
                });

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        getMenuInflater().inflate(R.menu.menu_howto, menu);
        getMenuInflater().inflate(R.menu.menu_endservice, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_howto:
                Intent intent1 = new Intent(this, HowToActivity.class);
                startActivity(intent1);
                return true;
            case R.id.menu_endService:
                unbindAndKillService();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void unbindAndKillService(){
        Intent backToMainIntent = new Intent(this, MainActivity.class);
        // clear the backstack when transitioning to main activity
        backToMainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        Intent stopServiceIntent = new Intent(this, GuardService.class);

        unbindService(serviceConnection);
        serviceConnection = null;
        stopService(stopServiceIntent);
        startActivity(backToMainIntent);
    }
}
