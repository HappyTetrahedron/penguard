package verteiltesysteme.penguard.Settings;


import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import verteiltesysteme.penguard.AppCompatPreferenceActivity;

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

    private OnPreferenceChangeListenerImpl onPreferenceChangeListener;

    /* This class is the definition of spaghetti code, but I'm taking no responsibility here since I wasn't the one who
     * introduced like three static anonymous classes.
     * (also this is not meant as harsh as it sounds. Refactoring this was just painfully entertaining)
     * If we have enough time we can consider tidying up this class a bit - for example, settings must now be added from the PreferenceFragmentImpl.
     * -Nils
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onPreferenceChangeListener = new OnPreferenceChangeListenerImpl(this);
        PreferenceFragmentImpl preferenceFragment = new PreferenceFragmentImpl();
        preferenceFragment.setSettingsActivity(this);
        getFragmentManager().beginTransaction().replace(android.R.id.content, preferenceFragment).commit();
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

    protected boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    protected void toast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void debug(String msg) {
        Log.d("SettingsActivity", msg);
    }
}
