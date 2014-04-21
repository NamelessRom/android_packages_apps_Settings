package com.android.settings.nameless.secret;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.provider.Settings;

import com.android.settings.R;

/**
 * Created by alex on 22.04.14.
 */
public class SettingsPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.nameless_settings_headers);

        final Activity activity = getActivity();

        setupPreferences(activity);
    }

    private void setupPreferences(final Activity activity) {
        boolean disableMobileDataSwitch = Settings.Global.getInt(activity.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }
}
