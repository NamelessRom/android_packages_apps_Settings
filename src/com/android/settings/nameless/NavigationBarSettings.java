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
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class NavigationBarSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String CATEGORY_NAVBAR = "navigation_bar";

    private static final String KEY_NAVIGATION_BAR_HEIGHT = "navigation_bar_height";
    private static final String KEY_NAVIGATION_BAR_WIDTH  = "navigation_bar_width";
    private static final String KEY_NAVIGATION_BAR_FORCE_ENABLE  = "navbar_force_enable";
    private static final String KEY_NAVIGATION_BAR_ENABLE_SPEN  = "enable_navbar_spen";

    private ListPreference mNavigationBarHeight;
    private ListPreference mNavigationBarWidth;
    private CheckBoxPreference mNavBarForceEnable;
    private CheckBoxPreference mNavBarEnableSPen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.navigation_bar_settings);

        PreferenceScreen prefScreen = getPreferenceScreen();
        PreferenceCategory category;

        final boolean hasRealNavigationBar = getResources()
                .getBoolean(com.android.internal.R.bool.config_showNavigationBar);
        if (hasRealNavigationBar) { // only disable on devices with REAL navigation bars
            category = (PreferenceCategory) findPreference("navigation_bar_force");
            if (category != null) {
                prefScreen.removePreference(category);
            }
        }

        category = (PreferenceCategory) findPreference(CATEGORY_NAVBAR);
        mNavigationBarHeight = (ListPreference) findPreference(KEY_NAVIGATION_BAR_HEIGHT);
        mNavigationBarHeight.setOnPreferenceChangeListener(this);

        mNavigationBarWidth = (ListPreference) findPreference(KEY_NAVIGATION_BAR_WIDTH);
        if (!Utils.isPhone(getActivity())) {
            category.removePreference(mNavigationBarWidth);
            mNavigationBarWidth = null;
        } else {
            mNavigationBarWidth.setOnPreferenceChangeListener(this);
        }

        updateDimensionValues();

        mNavBarForceEnable = (CheckBoxPreference) findPreference(KEY_NAVIGATION_BAR_FORCE_ENABLE);
        mNavBarForceEnable.setChecked(Settings.System.getInt(getContentResolver(),
                    Settings.System.NAVBAR_FORCE_ENABLE, 0) == 1);
        mNavBarForceEnable.setOnPreferenceChangeListerner(this);

        mNavBarEnableSPen = (CheckBoxPreference) findPreference(KEY_NAVIGATION_BAR_ENABLE_SPEN);
        mNavBarEnableSPen.setChecked(Settings.System.getInt(getContentResolver(),
                    Settings.System.ENABLE_NAVBAR_SPEN, 0) == 1);
        mNavBarEnableSPen.setOnPreferenceChangeListerner(this);
        mNavBarEnableSPen.setEnabled(!mNavBarForceEnable.isChecked());
    }

    public boolean onPreferenceChange(final Preference preference, final Object objValue) {
        if (preference == mNavigationBarWidth) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.NAVIGATION_BAR_WIDTH,
                    Integer.parseInt((String) objValue));
            return true;
        } else if (preference == mNavigationBarHeight) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.NAVIGATION_BAR_HEIGHT,
                    Integer.parseInt((String) objValue));
            return true;
        } else if (preference == mNavBarForceEnable) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.NAVBAR_FORCE_ENABLE, value ? 1 : 0);
            if (value) {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.ENABLE_NAVBAR_SPEN, 0);
            }
            mNavBarEnableSPen.setEnabled(!mNavBarForceEnable.isChecked());
            return true;
        } else if (preference == mNavBarEnableSPen) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.NAVBAR_FORCE_ENABLE, value ? 1 : 0);
            return true;
        }
        return false;
    }

    private void updateDimensionValues() {
        int navigationBarHeight = Settings.System.getInt(getContentResolver(),
                Settings.System.NAVIGATION_BAR_HEIGHT, -2);
        if (navigationBarHeight == -2) {
            navigationBarHeight = (int) (getResources().getDimension(
                    com.android.internal.R.dimen.navigation_bar_height)
                    / getResources().getDisplayMetrics().density);
        }
        mNavigationBarHeight.setValue(String.valueOf(navigationBarHeight));

        if (mNavigationBarWidth == null) {
            return;
        }
        int navigationBarWidth = Settings.System.getInt(getContentResolver(),
                Settings.System.NAVIGATION_BAR_WIDTH, -2);
        if (navigationBarWidth == -2) {
            navigationBarWidth = (int) (getResources().getDimension(
                    com.android.internal.R.dimen.navigation_bar_width)
                    / getResources().getDisplayMetrics().density);
        }
        mNavigationBarWidth.setValue(String.valueOf(navigationBarWidth));
    }

}
