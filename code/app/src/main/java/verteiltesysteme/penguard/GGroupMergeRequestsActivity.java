package verteiltesysteme.penguard;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;


import verteiltesysteme.penguard.guardianservice.GuardService;
import verteiltesysteme.penguard.guardianservice.MergeRequestArrayAdapter;
import verteiltesysteme.penguard.protobuf.PenguardProto;

public class GGroupMergeRequestsActivity extends PenguardActivity {

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
                                    String ip = selectedmessage.getMergeReq().getIp();
                                    int port = selectedmessage.getMergeReq().getPort();
                                    debug("sending message with ip: " + ip + " and port: " + port);
                                    pendingRequests.remove(position);
                                    toast("Initialised merging groups");
                                    mergeRequestAdapter.notifyDataSetChanged();
                                    serviceConnection.sendGroupTo(ip, port);
                                    dialogInterface.dismiss();
                                    Intent intent = new Intent(getApplicationContext(), GGuardActivity.class);
                                    startActivity(intent);
                                    GGroupMergeRequestsActivity.this.finish();
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

}

