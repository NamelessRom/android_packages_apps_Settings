/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.nameless;

import android.os.Bundle;
import android.preference.Preference;
import android.provider.Settings;
import android.text.TextUtils;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;

public class AppCircleBar extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {

    private static final String PREF_INCLUDE_APP_CIRCLE_BAR_KEY = "app_circle_bar_included_apps";

    private AppMultiSelectListPreference mIncludedAppCircleBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.app_circlebar);

        mIncludedAppCircleBar =
                (AppMultiSelectListPreference) findPreference(PREF_INCLUDE_APP_CIRCLE_BAR_KEY);
        mIncludedAppCircleBar.setValues(getIncludedApps());
        mIncludedAppCircleBar.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mIncludedAppCircleBar) {
            storeIncludedApps((Set<String>) objValue);
            return true;
        }

        return false;
    }

    private Set<String> getIncludedApps() {
        final String included = Settings.System.getString(getContentResolver(),
                Settings.System.WHITELIST_APP_CIRCLE_BAR);
        if (TextUtils.isEmpty(included)) {
            return null;
        }
        return new HashSet<>(Arrays.asList(included.split("\\|")));
    }

    private void storeIncludedApps(Set<String> values) {
        final StringBuilder builder = new StringBuilder();
        String delimiter = "";
        for (final String value : values) {
            builder.append(delimiter);
            builder.append(value);
            delimiter = "|";
        }
        Settings.System.putString(getContentResolver(), Settings.System.WHITELIST_APP_CIRCLE_BAR,
                builder.toString());
    }

}
