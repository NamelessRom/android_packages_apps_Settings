package org.namelessrom.settings.fragments;

import android.os.Bundle;

import com.android.settings.R;

import org.namelessrom.settings.SettingsPreferenceFragment;

/**
 * Created by alex on 21.10.14.
 */
public class DeviceSettingsFragment extends SettingsPreferenceFragment {

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.nameless_device_settings);
    }

}
