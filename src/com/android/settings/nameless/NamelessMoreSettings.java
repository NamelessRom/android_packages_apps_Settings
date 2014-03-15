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
import android.preference.Preference;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class NamelessMoreSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

private static final String SREC_ENABLE_TOUCHES = "srec_enable_touches";
private static final String SREC_ENABLE_MIC = "srec_enable_mic";

private CheckBoxPreference mSrecEnableTouches;
private CheckBoxPreference mSrecEnableMic;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.nameless_more_settings);

        mSrecEnableTouches = (CheckBoxPreference) findPreference(SREC_ENABLE_TOUCHES);
        mSrecEnableTouches.setChecked((Settings.System.getInt(resolver,
                Settings.System.SREC_ENABLE_TOUCHES, 0) == 1));
        mSrecEnableMic = (CheckBoxPreference) findPreference(SREC_ENABLE_MIC);
        mSrecEnableMic.setChecked((Settings.System.getInt(resolver,
                Settings.System.SREC_ENABLE_MIC, 0) == 1));

    }


   @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if  (preference == mSrecEnableTouches) {
            boolean checked = ((CheckBoxPreference)preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.SREC_ENABLE_TOUCHES, checked ? 1:0);
            return true;
        } else if  (preference == mSrecEnableMic) {
            boolean checked = ((CheckBoxPreference)preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.SREC_ENABLE_MIC, checked ? 1:0);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        return false;
    }

}
