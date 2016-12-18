package verteiltesysteme.penguard.Settings;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

import verteiltesysteme.penguard.AppCompatPreferenceActivity;
import verteiltesysteme.penguard.R;
import verteiltesysteme.penguard.guardianservice.GuardService;
import verteiltesysteme.penguard.guardianservice.GuardianServiceConnection;

import static java.util.Arrays.asList;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {

    private OnPreferenceChangeListenerImpl onPreferenceChangeListener = new OnPreferenceChangeListenerImpl();
    protected static ArrayList<String> listOfServerSettings;
    protected static ArrayList<String> listOfPenguinSettings;
    private class OnPreferenceChangeListenerImpl implements Preference.OnPreferenceChangeListener {

        // This method gets called directly after the user has changed a preference. We just always accept the change.
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            preference.setSummary(stringValue);
            return true;
        }
    }

    private GuardianServiceConnection guardianServiceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listOfServerSettings = new ArrayList<>(asList(getString(R.string.pref_key_server_address),
                getString(R.string.pref_key_port),
                getString(R.string.pref_key_username)));
        listOfPenguinSettings = new ArrayList<>(Collections.singletonList(getString(R.string.pref_key_penguin_missing_delay)));

        final PreferenceFragmentImpl preferenceFragment = new PreferenceFragmentImpl();
        preferenceFragment.setSettingsActivity(this);

        Intent intent = new Intent(this, GuardService.class);
        guardianServiceConnection = new GuardianServiceConnection();
        bindService(intent, guardianServiceConnection, Context.BIND_AUTO_CREATE);
        guardianServiceConnection.registerServiceConnectedCallback(new Runnable() {
            @Override
            public void run() {
                preferenceFragment.initializeSettings(guardianServiceConnection.isRegistered());
            }
        });

        getFragmentManager().beginTransaction().replace(android.R.id.content, preferenceFragment).commit();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (guardianServiceConnection != null && guardianServiceConnection.isConnected()) {
            unbindService(guardianServiceConnection);
        }
    }

    protected void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        // Trigger the listener immediately with the preference's
        // current value.
        preference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        onPreferenceChangeListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    protected void toast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void debug(String msg) {
        Log.d("SettingsActivity", msg);
    }
}
