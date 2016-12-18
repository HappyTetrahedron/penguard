package verteiltesysteme.penguard;

import android.support.v4.app.DialogFragment;


interface NoticeDialogListener {
    void onDialogPositiveClick(DialogFragment dialog);
    void onDialogNegativeClick(DialogFragment dialog);
}
