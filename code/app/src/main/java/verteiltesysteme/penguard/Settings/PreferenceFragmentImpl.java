package verteiltesysteme.penguard.Settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;

import java.util.ArrayList;

import verteiltesysteme.penguard.R;

import static java.util.Arrays.asList;
import static verteiltesysteme.penguard.Settings.OnPreferenceChangeListenerImpl.listOfServerSettings;

public class PreferenceFragmentImpl extends PreferenceFragment {

    /* List of all serversetting-related keys. Whenever a new server setting is added, it should be over this list.
     * This is for extensibility: Using this list, we can just disallow changing the server settings if the user is logged in.
     */
    private static final ArrayList<String> listOfServerSettings = new ArrayList<>(asList("server_address", "port", "username"));

    private SettingsActivity settingsActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_headers);
        initializeSettings();
    }

    protected void setSettingsActivity(SettingsActivity settingsActivity) {
        this.settingsActivity = settingsActivity;
    }

    private void initializeSettings(){
        // We add the server settings in one bunch for extensibility. All server settings are defined in the static final ArrayList listOfServerSettings.
        for (String s : listOfServerSettings) {
            debug(s);
            settingsActivity.bindPreferenceSummaryToValue(findPreference(s));
        }
    }

    private void debug(String msg) {
        Log.d("PreferenceFragmentImpl", msg);
    }
}
