package verteiltesysteme.penguard;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


import verteiltesysteme.penguard.guardianservice.GuardService;
import verteiltesysteme.penguard.guardianservice.Guardian;
import verteiltesysteme.penguard.guardianservice.GuardianServiceConnection;
import verteiltesysteme.penguard.guardianservice.MergeRequestArrayAdapter;
import verteiltesysteme.penguard.protobuf.PenguardProto;

public class GGroupMergeRequestsActivity extends AppCompatActivity {

    GuardianServiceConnection serviceConnection = new GuardianServiceConnection();
    private ArrayList<PenguardProto.PGPMessage> pendingRequests = new ArrayList<PenguardProto.PGPMessage>();
    final static String EXTRA_IP = "RequestIP";
    final static String EXTRA_PORT = "RequestedPort";
    final static String EXTRA_NAME = "RequestedName";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ggroup_merge_requests);

        //bind the service
        Intent bindintent = new Intent(this, GuardService.class);
        bindService(bindintent, serviceConnection, Context.BIND_AUTO_CREATE);
        Intent intent = getIntent();
        String ip = intent.getStringExtra(EXTRA_IP);
        String name = intent.getStringExtra(EXTRA_NAME);
        int port = intent.getIntExtra(EXTRA_PORT, 6789);
        PenguardProto.MergeReq merge = PenguardProto.MergeReq.newBuilder()
                .setIp(ip)
                .setName(name)
                .setPort(port)
                .build();

        PenguardProto.PGPMessage mergerequest = PenguardProto.PGPMessage.newBuilder()
                .setMergeReq(merge)
                .setType(PenguardProto.PGPMessage.Type.SG_MERGE_REQ)
                .build();

        pendingRequests.add(mergerequest);

        ListView mergerequestlist = (ListView) findViewById(R.id.mergeRequestList);

        //used my own ArrayAdapter according to this: http://stackoverflow.com/questions/2265661/how-to-use-arrayadaptermyclass
        //so i could just display the name instead of the whole message
        final MergeRequestArrayAdapter mergeRequestAdapter = new MergeRequestArrayAdapter(getApplicationContext(), pendingRequests);
        mergeRequestAdapter.notifyDataSetChanged();
        mergerequestlist.setAdapter(mergeRequestAdapter);

        // register onClickListener to handle click events on each item
        mergerequestlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // argument position gives the index of item which is clicked
            public void onItemClick(AdapterView<?> arg0, View v, final int position, long arg3) {
                final PenguardProto.PGPMessage selectedmessage = pendingRequests.get(position);
                debug("clicked on message: " + selectedmessage);
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (!isFinishing()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(GGroupMergeRequestsActivity.this);
                            builder.setMessage(getText(R.string.dialog_text_merge_request) + selectedmessage.getMergeReq().getName() + "?");
                            builder.setTitle(R.string.dialog_title_merge_request);

                            builder.setPositiveButton(R.string.dialog_ok_merge_request, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    debug("is service connection null? " + (serviceConnection == null));
                                    String ip = selectedmessage.getMergeReq().getIp();
                                    int port = selectedmessage.getMergeReq().getPort();
                                    debug("sending message with ip: " + ip + " and port: " + port);
                                    pendingRequests.remove(position);
                                    toast("Initialised merging groups");
                                    mergeRequestAdapter.notifyDataSetChanged();
                                    toast(getString(R.string.sendingTo) + ip + ":" + port);
                                    serviceConnection.sendGroupTo(ip, port);
                                }
                            });

                            builder.setNegativeButton(R.string.dialog_cancel_merge_request, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
//                AlertDialog dialog = builder.create();
                            builder.show();
                        }
                    }
                });


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

    public void back(View view){
        Intent intent = new Intent(this, GGuardActivity.class);
        startActivity(intent);
    }

    private void debug(String msg) {
        Log.d("MergeRequestActivity", msg);
    }

    private void toast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}

