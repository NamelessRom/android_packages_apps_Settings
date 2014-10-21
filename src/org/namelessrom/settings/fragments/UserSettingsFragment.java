package org.namelessrom.settings.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.android.settings.R;

/**
 * Created by alex on 21.10.14.
 */
public class UserSettingsFragment extends PreferenceFragment {

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.nameless_user_settings);
    }

}
