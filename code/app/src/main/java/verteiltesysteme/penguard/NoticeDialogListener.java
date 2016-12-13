package verteiltesysteme.penguard;

import android.support.v4.app.DialogFragment;


interface NoticeDialogListener {
    public void onDialogPositiveClick(DialogFragment dialog);
    public void onDialogNegativeClick(DialogFragment dialog);
}
