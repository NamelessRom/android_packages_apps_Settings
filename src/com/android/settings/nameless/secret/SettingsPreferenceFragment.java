package com.android.settings.nameless.secret;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.android.settings.R;

/**
 * Created by alex on 22.04.14.
 */
public class SettingsPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.nameless_settings_headers);
    }
}
