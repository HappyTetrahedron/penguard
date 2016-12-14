package verteiltesysteme.penguard.Settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import verteiltesysteme.penguard.R;
import verteiltesysteme.penguard.guardianservice.GuardService;

public class PreferenceFragmentImpl extends PreferenceFragment {

    private SettingsActivity settingsActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_headers);
    }

    protected void setSettingsActivity(SettingsActivity settingsActivity) {
        this.settingsActivity = settingsActivity;
    }

    protected void initializeSettings(boolean isUserRegistered){
        // Notify the user with a toast if he is logged in and therefore can't change server settings.
        if (isUserRegistered){
            settingsActivity.toast(getString(R.string.please_logout_to_change));
        }

        // We add the server-related settings in one bunch for extensibility.
        for (String s : SettingsActivity.listOfServerSettings) {
            debug(s);
            Preference preference = findPreference(s);
            settingsActivity.bindPreferenceSummaryToValue(preference);
            if (isUserRegistered){
                preference.setEnabled(false);
            }
        }
    }

    private void debug(String msg) {
        Log.d("PreferenceFragmentImpl", msg);
    }
}
