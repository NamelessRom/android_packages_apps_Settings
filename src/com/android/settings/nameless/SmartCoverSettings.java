package com.android.settings.nameless;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class SmartCoverSettings extends SettingsPreferenceFragment {

    private static final String ACTION_NAMELESS_PROVIDER =
            "org.namelessrom.providers.activities.Preferences";

    private static final Intent INTENT_NAMELESS_PROVIDER = new Intent(ACTION_NAMELESS_PROVIDER);

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.smart_cover_settings);

        if (!intentExists(INTENT_NAMELESS_PROVIDER)) {
            getPreferenceScreen().removePreference(findPreference("nameless_provider"));
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getKey().equals("nameless_provider")) {
            startActivity(INTENT_NAMELESS_PROVIDER);
            return true;
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private boolean intentExists(final Intent intent) {
        return getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY).size() > 0;
    }
}
