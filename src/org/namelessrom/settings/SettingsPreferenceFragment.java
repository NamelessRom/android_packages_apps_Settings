package org.namelessrom.settings;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;

/**
 * Created by alex on 21.10.14.
 */
public class SettingsPreferenceFragment extends PreferenceFragment {

    @Override public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        final Intent intent = preference.getIntent();
        if (intent != null) {
            startActivitySafely(intent);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    protected void startActivitySafely(final Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException exc) {
            Log.e("SettingsFragment", "Activity not found.", exc);
        }
    }

}
