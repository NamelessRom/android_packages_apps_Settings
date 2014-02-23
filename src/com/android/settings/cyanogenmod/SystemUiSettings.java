/*
 * Copyright (C) 2012 The CyanogenMod project
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

package com.android.settings.cyanogenmod;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.view.ViewConfiguration;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class SystemUiSettings extends SettingsPreferenceFragment  implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "SystemSettings";

    private static final String KEY_EXPANDED_DESKTOP = "expanded_desktop";
    private static final String KEY_EXPANDED_DESKTOP_NO_NAVBAR = "expanded_desktop_no_navbar";
    private static final String CATEGORY_NAVBAR = "navigation_bar";
    private static final String KEY_SCREEN_GESTURE_SETTINGS = "touch_screen_gesture_settings";

    private static final String KEY_NAVIGATION_BAR_HEIGHT = "navigation_bar_height";
    private static final String KEY_NAVIGATION_BAR_WIDTH = "navigation_bar_width";

    private ListPreference mExpandedDesktopPref;
    private CheckBoxPreference mExpandedDesktopNoNavbarPref;

    ListPreference mNavigationBarHeight;
    ListPreference mNavigationBarWidth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.system_ui_settings);
        PreferenceScreen prefScreen = getPreferenceScreen();
        final ContentResolver resolver = getActivity().getContentResolver();

        // Expanded desktop
        mExpandedDesktopPref = (ListPreference) findPreference(KEY_EXPANDED_DESKTOP);
        mExpandedDesktopNoNavbarPref =
                (CheckBoxPreference) findPreference(KEY_EXPANDED_DESKTOP_NO_NAVBAR);

        Utils.updatePreferenceToSpecificActivityFromMetaDataOrRemove(getActivity(),
                getPreferenceScreen(), KEY_SCREEN_GESTURE_SETTINGS);

        int expandedDesktopValue = Settings.System.getInt(resolver,
                Settings.System.EXPANDED_DESKTOP_STYLE, 0);

        final boolean hasRealNavigationBar = getResources()
                .getBoolean(com.android.internal.R.bool.config_showNavigationBar);
        if (hasRealNavigationBar) { // only disable on devices with REAL navigation bars
            final Preference pref = findPreference("navbar_force_enable");
            if (pref != null) {
                prefScreen.removePreference(pref);
            }
        }

        // Allows us to support devices, which have the navigation bar force enabled.
        final boolean hasNavBar = !ViewConfiguration.get(getActivity()).hasPermanentMenuKey();

        if (hasNavBar) {
            mExpandedDesktopPref.setOnPreferenceChangeListener(this);
            mExpandedDesktopPref.setValue(String.valueOf(expandedDesktopValue));
            updateExpandedDesktop(expandedDesktopValue);
            prefScreen.removePreference(mExpandedDesktopNoNavbarPref);

            mNavigationBarHeight = (ListPreference) findPreference(KEY_NAVIGATION_BAR_HEIGHT);
            String navbarHeight = Settings.System.getString(resolver,
                    Settings.System.NAVIGATION_BAR_HEIGHT);
            navbarHeight = mapChosenPixelstoDp(navbarHeight);
            if (navbarHeight != null) {
                mNavigationBarHeight.setValue(navbarHeight);
            } else {
                mNavigationBarHeight.setValue("48");
            }
            mNavigationBarHeight.setOnPreferenceChangeListener(this);

            mNavigationBarWidth = (ListPreference) findPreference(KEY_NAVIGATION_BAR_WIDTH);
            String navbarWidth = Settings.System.getString(resolver,
                    Settings.System.NAVIGATION_BAR_WIDTH);
            navbarWidth = mapChosenPixelstoDp(navbarWidth);
            if (navbarWidth != null) {
                mNavigationBarWidth.setValue(navbarWidth);
            } else {
                mNavigationBarWidth.setValue("42");
            }
            mNavigationBarWidth.setOnPreferenceChangeListener(this);
        } else {
            // Hide no-op "Status bar visible" expanded desktop mode
            mExpandedDesktopNoNavbarPref.setOnPreferenceChangeListener(this);
            mExpandedDesktopNoNavbarPref.setChecked(expandedDesktopValue > 0);
            prefScreen.removePreference(mExpandedDesktopPref);
            // Hide navigation bar category
            prefScreen.removePreference(findPreference(CATEGORY_NAVBAR));
        }

    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mExpandedDesktopPref) {
            int expandedDesktopValue = Integer.valueOf((String) objValue);
            updateExpandedDesktop(expandedDesktopValue);
            return true;
        } else if (preference == mExpandedDesktopNoNavbarPref) {
            boolean value = (Boolean) objValue;
            updateExpandedDesktop(value ? 2 : 0);
            return true;
        } else if (preference == mNavigationBarWidth) {
            String newVal = (String) objValue;
            int dp = Integer.parseInt(newVal);
            int width = mapChosenDpToPixels(dp);
            Settings.System.putInt(getContentResolver(), Settings.System.NAVIGATION_BAR_WIDTH,
                    width);
            return true;
        } else if (preference == mNavigationBarHeight) {
            String newVal = (String) objValue;
            int dp = Integer.parseInt(newVal);
            int height = mapChosenDpToPixels(dp);
            Settings.System.putInt(getContentResolver(), Settings.System.NAVIGATION_BAR_HEIGHT,
                    height);
            return true;
        }
        return false;
    }

    private void updateExpandedDesktop(int value) {
        ContentResolver cr = getContentResolver();
        Resources res = getResources();
        int summary = -1;

        Settings.System.putInt(cr, Settings.System.EXPANDED_DESKTOP_STYLE, value);

        if (value == 0) {
            // Expanded desktop deactivated
            Settings.System.putInt(cr, Settings.System.POWER_MENU_EXPANDED_DESKTOP_ENABLED, 0);
            Settings.System.putInt(cr, Settings.System.EXPANDED_DESKTOP_STATE, 0);
            summary = R.string.expanded_desktop_disabled;
        } else if (value == 1) {
            Settings.System.putInt(cr, Settings.System.POWER_MENU_EXPANDED_DESKTOP_ENABLED, 1);
            summary = R.string.expanded_desktop_status_bar;
        } else if (value == 2) {
            Settings.System.putInt(cr, Settings.System.POWER_MENU_EXPANDED_DESKTOP_ENABLED, 1);
            summary = R.string.expanded_desktop_no_status_bar;
        }

        if (mExpandedDesktopPref != null && summary != -1) {
            mExpandedDesktopPref.setSummary(res.getString(summary));
        }
    }

    public int mapChosenDpToPixels(int dp) {
        switch (dp) {
            case 48:
                return getResources().getDimensionPixelSize(R.dimen.navigation_bar_48);
            case 44:
                return getResources().getDimensionPixelSize(R.dimen.navigation_bar_44);
            case 42:
                return getResources().getDimensionPixelSize(R.dimen.navigation_bar_42);
            case 40:
                return getResources().getDimensionPixelSize(R.dimen.navigation_bar_40);
            case 36:
                return getResources().getDimensionPixelSize(R.dimen.navigation_bar_36);
            case 30:
                return getResources().getDimensionPixelSize(R.dimen.navigation_bar_30);
            case 24:
                return getResources().getDimensionPixelSize(R.dimen.navigation_bar_24);
            case 0:
                return 0;
        }
        return -1;
    }

    public String mapChosenPixelstoDp(String px) {
        if (px.equals("96")) {
            return "48";
        } else if (px.equals("88")) {
            return "44";
        } else if (px.equals("84")) {
            return "42";
        } else if (px.equals("80")) {
            return "40";
        } else if (px.equals("72")) {
            return "36";
        } else if (px.equals("60")) {
            return "30";
        } else if (px.equals("48")) {
            return "24";
        } else if (px.equals("0")) {
            return "0";
        }
        return null;
    }

}
