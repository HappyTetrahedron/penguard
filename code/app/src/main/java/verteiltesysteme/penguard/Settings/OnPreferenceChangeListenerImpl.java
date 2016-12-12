package verteiltesysteme.penguard.Settings;

import android.preference.Preference;
import android.util.Log;

import java.util.ArrayList;

import verteiltesysteme.penguard.R;
import verteiltesysteme.penguard.guardianservice.GuardService;

import static java.util.Arrays.asList;

public class OnPreferenceChangeListenerImpl implements Preference.OnPreferenceChangeListener {

    SettingsActivity settingsActivity;

    /* List of all server setting keys. Whenever a new server setting is added, it should be over this list.
     * This is for extensibility: Using this list, we can just disallow changing the server settings if the user is logged in.
     */
    protected static final ArrayList<String> listOfServerSettings = new ArrayList<>(asList("server_address", "port", "username"));

    protected OnPreferenceChangeListenerImpl(SettingsActivity activity){
        this.settingsActivity = activity;
    }

    // This method gets called directly after the user has changed a preference. We can use this to accept or reject that change.
    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();
        preference.setSummary(stringValue);

        // Only update the setting if it's NOT a server setting. Server settings are described in listOfServerSettings.
        if (settingsActivity.isMyServiceRunning(GuardService.class)){
            for (String s : listOfServerSettings ) {
                if (preference.getKey().equals(s)) {
                    settingsActivity.toast(settingsActivity.getString(R.string.please_logout_to_change));
                    return false;
                }
            }
        }

        return true;
    }

    private void debug(String msg) {
        Log.d("OnPreferenceChangeListe", msg);
    }
}
