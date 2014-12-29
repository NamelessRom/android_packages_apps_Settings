/*
 * Copyright (C) 2014 The NamelessRom project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.nameless;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.cyanogenmod.SystemSettingSwitchPreference;

public class NavigationBarSettings extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String CATEGORY_NAV_BAR_SIMULATE = "navigation_bar_simulate";

    private static final String KEY_FORCE_ENABLE_NAVBAR = "navbar_force_enable";
    private static final String KEY_HARDWARE_KEYS_DISABLE = "hardware_keys_disable";

    private static final String PREF_BUTTON_BACKLIGHT = "pref_navbar_button_backlight";

    private SystemSettingSwitchPreference mForceEnableNavbar;
    private SystemSettingSwitchPreference mHardwareKeysDisable;

    private Handler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.navigation_bar_settings);

        final boolean hasRealNavigationBar = getResources()
                .getBoolean(com.android.internal.R.bool.config_showNavigationBar);

        // only disable on devices with REAL navigation bars
        if (!hasRealNavigationBar) {
            mForceEnableNavbar =
                    (SystemSettingSwitchPreference) findPreference(KEY_FORCE_ENABLE_NAVBAR);
            mForceEneableNavbar.setOnPreferenceChangeListener(this);
            mHardwareKeysDisable =
                    (SystemSettingSwitchPreference) findPreference(KEY_HARDWARE_KEYS_DISABLE);
            mHardwareKeysDisable.setOnPreferenceChangeListener(this);
        } else {
            final Preference pref = findPreference(CATEGORY_NAV_BAR_SIMULATE);
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }
        }

        mHandler = new Handler();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (mForceEnableNavbar == preference) {
            mForceEnableNavbar.setEnabled(false);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mForceEnableNavbar.setEnabled(true);
                }
            }, 1000);
            return true;
        } else if (mHardwareKeysDisable == preference) {
            final boolean enabled = (Boolean) newValue;
            final SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(getActivity());
            final int defaultBrightness = getResources().getInteger(
                    com.android.internal.R.integer.config_buttonBrightnessSettingDefault);

            final SharedPreferences.Editor editor = prefs.edit();

            if (enabled) {
                final int currentBrightness = Settings.System.getInt(getContentResolver(),
                        Settings.System.BUTTON_BRIGHTNESS, defaultBrightness);
                if (!prefs.contains(PREF_BUTTON_BACKLIGHT)) {
                    editor.putInt(PREF_BUTTON_BACKLIGHT, currentBrightness);
                }
                Settings.System.putInt(getContentResolver(), Settings.System.BUTTON_BRIGHTNESS, 0);
            } else {
                final int oldBright = prefs.getInt(PREF_BUTTON_BACKLIGHT, -1);
                if (oldBright != -1) {
                    Settings.System.putInt(getContentResolver(),
                            Settings.System.BUTTON_BRIGHTNESS, oldBright);
                    editor.remove(PREF_BUTTON_BACKLIGHT);
                }
            }
            editor.commit();
            return true;
        }

        return false;
    }
}
