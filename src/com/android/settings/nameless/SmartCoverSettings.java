/*
 * <!--
 *    Copyright (C) 2014 The NamelessRom Project
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * -->
 */
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
