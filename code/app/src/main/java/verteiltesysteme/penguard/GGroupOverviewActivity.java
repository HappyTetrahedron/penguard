package verteiltesysteme.penguard;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import verteiltesysteme.penguard.Settings.SettingsActivity;
import verteiltesysteme.penguard.guardianservice.GuardService;
import verteiltesysteme.penguard.guardianservice.Guardian;
import verteiltesysteme.penguard.guardianservice.GuardianServiceConnection;
import verteiltesysteme.penguard.guardianservice.Penguin;
import verteiltesysteme.penguard.guardianservice.TwoPhaseCommitCallback;

public class GGroupOverviewActivity extends AppCompatActivity  implements NoticeDialogListener{

    ListView listView;
    GuardianServiceConnection serviceConnection = new GuardianServiceConnection();

    Guardian selectedGuardian;
    KickGuardianDialogFragment dialog;

    Handler handler;
    Runnable updateTask;

    boolean paused = false;
    private static final int UPDATE_DELAY = 500;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ggroup_overview);

        //bind the service
        Intent intent = new Intent(this, GuardService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        listView = (ListView)findViewById(R.id.groupOverviewListView);
        dialog = new KickGuardianDialogFragment();

        handler = new Handler();

        //show in the listview every guardian and when he was last seen via the timestamp
        updateTask = new Runnable() {
            @Override
            public void run() {
                if (serviceConnection != null && serviceConnection.isConnected()) {
                    if (listView.getAdapter() == null) {
                        serviceConnection.subscribeListViewToGuardianAdapter(listView);
                    }
                    ((ArrayAdapter<Guardian>) listView.getAdapter()).notifyDataSetChanged();
                }
                if (!paused) handler.postDelayed(this, UPDATE_DELAY);
            }
        };

        //show a dialog to give the user the option to delete a guardian
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedGuardian = (Guardian)adapterView.getItemAtPosition(i);
                showDeleteDialog();
            }
        });
    }

    private void deleteGuardian(Guardian guardian){
        TwoPhaseCommitCallback callback = new TwoPhaseCommitCallback() {
            @Override
            public void onCommit(String message) {
                //it worked
                toast(getString(R.string.removeGuardianSuc));
                dialog.dismiss();
                //TODO update listview
            }

            @Override
            public void onAbort(String error) {
                //it didn't work
                toast(getString(R.string.removeGuardianFail));
            }
        };
        serviceConnection.kickGuardian(guardian,callback);
    }

    private void showDeleteDialog(){
        dialog.show(getSupportFragmentManager(), "NoticeDialogFragment");
    }

    @Override
    protected void onResume() {
        super.onResume();
        paused = false;
        handler.post(updateTask);
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
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

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        //this is for the dialog buttons this is the delete guardian button
        deleteGuardian(selectedGuardian);

    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        //this is for the dialog buttons... this is the cancel button
        dialog.dismiss();
    }

    public static class KickGuardianDialogFragment extends DialogFragment {
        Guardian guardian;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.deleteQuestion)
                    .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Kick the guardian out
                            mListener.onDialogPositiveClick(KickGuardianDialogFragment.this);

                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                            mListener.onDialogPositiveClick(KickGuardianDialogFragment.this);

                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }

        // Use this instance of the interface to deliver action events
        NoticeDialogListener mListener;

        // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            // Verify that the host activity implements the callback interface
            try {
                // Instantiate the NoticeDialogListener so we can send events to the host
                mListener = (NoticeDialogListener) activity;
            } catch (ClassCastException e) {
                // The activity doesn't implement the interface, throw exception
                throw new ClassCastException(activity.toString()
                        + " must implement NoticeDialogListener");
            }
        }

    }
    private void toast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    private void debug(String msg) {
        Log.d("GGroupOverview", msg);
    }


}

