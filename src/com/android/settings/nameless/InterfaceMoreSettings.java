/*
 * Copyright (C) 2013-2014 The NamelessROM Project
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses
 */

package com.android.settings.nameless;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class InterfaceMoreSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "InterfaceMoreSettings";

    private static final String RECENT_MENU_CLEAR_ALL_LOCATION = "clear_recents_button_location";

    private ListPreference mRecentClearAllPosition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.nameless_interface_more_settings);

        mRecentClearAllPosition = (ListPreference) findPreference(RECENT_MENU_CLEAR_ALL_LOCATION);
        final String recentClearAllPosition = Settings.System.getString(getContentResolver(),
                Settings.System.CLEAR_RECENTS_BUTTON_LOCATION);
        if (recentClearAllPosition != null) {
            mRecentClearAllPosition.setValue(recentClearAllPosition);
        }
        mRecentClearAllPosition.setOnPreferenceChangeListener(this);

    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mRecentClearAllPosition) {
            String value = (String) objValue;
            Settings.System.putString(getContentResolver(),
                    Settings.System.CLEAR_RECENTS_BUTTON_LOCATION, value);
            return true;
        }
        return false;
    }

}
