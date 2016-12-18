package verteiltesysteme.penguard;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import verteiltesysteme.penguard.guardianservice.GuardService;
import verteiltesysteme.penguard.guardianservice.Guardian;
import verteiltesysteme.penguard.guardianservice.TwoPhaseCommitCallback;

public class GGroupOverviewActivity extends StatusToolbarActivity implements NoticeDialogListener {

    ListView listView;

    Guardian selectedGuardian;
    KickGuardianDialogFragment dialog;

    int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ggroup_overview);
        setUpToolbar();

        //bind the service
        Intent intent = new Intent(this, GuardService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        listView = (ListView)findViewById(R.id.groupOverviewListView);
        dialog = new KickGuardianDialogFragment();

        //show a dialog to give the user the option to delete a guardian
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedGuardian = (Guardian)adapterView.getItemAtPosition(i);
                if (counter ==0) {
                    counter++;
                    if (!selectedGuardian.equals(serviceConnection.getMyself())){
                        showDeleteDialog();
                    }else {
                        toast(getString(R.string.removeSelf));
                    }
                }
            }
        });
    }

    @Override
    void updateState() {
        if (serviceConnection != null && serviceConnection.isConnected()) {
            setButtonVisible(4, !serviceConnection.isRegistered());
            // button 4 is the login button
            if (listView.getAdapter() == null) {
                serviceConnection.subscribeListViewToGuardianAdapter(listView);
            }
            ((ArrayAdapter<Guardian>) listView.getAdapter()).notifyDataSetChanged();
        }
    }

    private void deleteGuardian(Guardian guardian){
        TwoPhaseCommitCallback callback = new TwoPhaseCommitCallback() {
            @Override
            public void onCommit(String message) {
                //it worked
                toast(getString(R.string.removeGuardianSuc));
                counter = 0;
                dialog.dismiss();
            }

            @Override
            public void onAbort(String error) {
                //it didn't work
                counter = 0;
                toast(getString(R.string.removeGuardianFail));
                dialog.dismiss();
            }
        };
        if (!guardian.equals(serviceConnection.getMyself())) {
            debug("guardian trying to remove is not yourself");
            serviceConnection.kickGuardian(guardian,callback);
        }else {
            //this state should never be reached
            debug("you are trying to remove yourself");
            toast(getString(R.string.removeSelf));
        }
    }

    @Override
    int getCurrentIconId() {
        return 2;
    }

    @Override
    int getMenuLayoutResource() {
        return R.menu.toolbar;
    }

    @Override
    Toolbar.OnMenuItemClickListener getOnMenuItemClickListener() {
        return new MainToolbarOnMenuItemClickListener(this);
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
    public void onDialogPositiveClick(DialogFragment dialog) {
        //this is for the dialog buttons this is the delete guardian button
        deleteGuardian(selectedGuardian);
        counter =0;
        dialog.dismiss();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        //this is for the dialog buttons... this is the cancel button
        counter=0;
        dialog.dismiss();
    }

    public static class KickGuardianDialogFragment extends DialogFragment {
        Guardian guardian;

        @Override
        @NonNull
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
                            mListener.onDialogNegativeClick(KickGuardianDialogFragment.this);

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

}

