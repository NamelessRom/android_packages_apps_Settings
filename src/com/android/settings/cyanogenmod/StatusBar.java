/*
 * Copyright (C) 2012 The CyanogenMod Project
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
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class StatusBar extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String STATUS_BAR_AM_PM_STYLE = "status_bar_am_pm_style";
    private static final String STATUS_BAR_CLOCK_STYLE = "statusbar_clock_style";
    private static final String STATUS_BAR_CLOCK_WEEKDAY = "statusbar_clock_weekday";
    private static final String STATUS_BAR_BATTERY = "status_bar_battery";
    private static final String STATUS_BAR_BATTERY_SHOW_PERCENT
            = "status_bar_battery_show_percent";

    private static final String STATUS_BAR_SIGNAL = "status_bar_signal";
    private static final String STATUS_BAR_NETWORK_ACTIVITY = "status_bar_network_activity";
    private static final String STATUS_BAR_TRAFFIC = "status_bar_traffic";

    private static final String STATUS_BAR_STYLE_HIDDEN = "4";
    private static final String STATUS_BAR_STYLE_TEXT = "6";

    private ListPreference mStatusBarAmPm;
    private ListPreference mStatusBarClockStyle;
    private ListPreference mStatusBarWeekday;
    private ListPreference mStatusBarBattery;
    private CheckBoxPreference mStatusBarBatteryShowPercent;

    private ListPreference mStatusBarCmSignal;
    private CheckBoxPreference mStatusBarNetworkActivity;
    private CheckBoxPreference mStatusBarTraffic;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.status_bar);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mStatusBarBattery = (ListPreference) prefSet.findPreference(STATUS_BAR_BATTERY);
        mStatusBarBatteryShowPercent =
                (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_BATTERY_SHOW_PERCENT);
        mStatusBarCmSignal = (ListPreference) prefSet.findPreference(STATUS_BAR_SIGNAL);

        mStatusBarAmPm = (ListPreference) prefSet.findPreference(STATUS_BAR_AM_PM_STYLE);
        int statusBarAmPm = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_AM_PM_STYLE, 2);

        mStatusBarAmPm.setValue(String.valueOf(statusBarAmPm));
        mStatusBarAmPm.setSummary(mStatusBarAmPm.getEntry());
        mStatusBarAmPm.setOnPreferenceChangeListener(this);

        mStatusBarClockStyle = (ListPreference) prefSet.findPreference(STATUS_BAR_CLOCK_STYLE);
        int StatusBarClockStyle = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_CLOCK_STYLE, 2);

        mStatusBarClockStyle.setValue(String.valueOf(StatusBarClockStyle));
        mStatusBarClockStyle.setSummary(mStatusBarAmPm.getEntry());
        mStatusBarClockStyle.setOnPreferenceChangeListener(this);

        mStatusBarWeekday = (ListPreference) prefSet.findPreference(STATUS_BAR_CLOCK_WEEKDAY);
        int StatusBarWeekday = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_CLOCK_WEEKDAY, 2);

        mStatusBarWeekday.setValue(String.valueOf(StatusBarWeekday));
        mStatusBarWeekday.setSummary(mStatusBarWeekday.getEntry());
        mStatusBarWeekday.setOnPreferenceChangeListener(this);

        CheckBoxPreference statusBarBrightnessControl = (CheckBoxPreference)
                prefSet.findPreference(Settings.System.STATUS_BAR_BRIGHTNESS_CONTROL);

        try {
            if (Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS_MODE)
                    == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                statusBarBrightnessControl.setEnabled(false);
                statusBarBrightnessControl.setSummary(R.string.status_bar_toggle_info);
            }
        } catch (SettingNotFoundException e) {
            // Do nothing
        }

        int batteryStyle = Settings.System.getInt(resolver, Settings.System.STATUS_BAR_BATTERY, 0);
        mStatusBarBattery.setValue(String.valueOf(batteryStyle));
        mStatusBarBattery.setSummary(mStatusBarBattery.getEntry());
        mStatusBarBattery.setOnPreferenceChangeListener(this);

        int signalStyle = Settings.System.getInt(resolver, Settings.System.STATUS_BAR_SIGNAL_TEXT, 0);
        mStatusBarCmSignal.setValue(String.valueOf(signalStyle));
        mStatusBarCmSignal.setSummary(mStatusBarCmSignal.getEntry());
        mStatusBarCmSignal.setOnPreferenceChangeListener(this);

        if (Utils.isWifiOnly(getActivity())) {
            prefSet.removePreference(mStatusBarCmSignal);
        }

        mStatusBarNetworkActivity = (CheckBoxPreference)
                prefSet.findPreference(STATUS_BAR_NETWORK_ACTIVITY);
        mStatusBarNetworkActivity.setChecked(Settings.System.getInt(resolver,
            Settings.System.STATUS_BAR_NETWORK_ACTIVITY, 0) == 1);
        mStatusBarNetworkActivity.setOnPreferenceChangeListener(this);

        mStatusBarTraffic = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_TRAFFIC);
        int intState = Settings.System.getInt(resolver, Settings.System.STATUS_BAR_TRAFFIC, 0);
        intState = setStatusBarTrafficSummary(intState);
        mStatusBarTraffic.setChecked(intState > 0);
        mStatusBarTraffic.setOnPreferenceChangeListener(this);

        if (Utils.isTablet(getActivity())) {
            prefSet.removePreference(statusBarBrightnessControl);
        }

        enableStatusBarBatteryDependents(mStatusBarBattery.getValue());

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mStatusBarBattery) {
            int batteryStyle = Integer.valueOf((String) newValue);
            int index = mStatusBarBattery.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver, Settings.System.STATUS_BAR_BATTERY, batteryStyle);
            mStatusBarBattery.setSummary(mStatusBarBattery.getEntries()[index]);
            enableStatusBarBatteryDependents((String)newValue);
            return true;
        } else if (preference == mStatusBarCmSignal) {
            int signalStyle = Integer.valueOf((String) newValue);
            int index = mStatusBarCmSignal.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver, Settings.System.STATUS_BAR_SIGNAL_TEXT, signalStyle);
            mStatusBarCmSignal.setSummary(mStatusBarCmSignal.getEntries()[index]);
            return true;
        } else if (mStatusBarAmPm != null && preference == mStatusBarAmPm) {
            int statusBarAmPm = Integer.valueOf((String) newValue);
            int index = mStatusBarAmPm.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver, Settings.System.STATUS_BAR_AM_PM_STYLE, statusBarAmPm);
            mStatusBarAmPm.setSummary(mStatusBarAmPm.getEntries()[index]);
            return true;
        } else if (mStatusBarClockStyle != null && preference == mStatusBarClockStyle) {
            int StatusBarClockStyle = Integer.valueOf((String) newValue);
            int index = mStatusBarClockStyle.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver, Settings.System.STATUS_BAR_CLOCK_STYLE, StatusBarClockStyle);
            mStatusBarClockStyle.setSummary(mStatusBarClockStyle.getEntries()[index]);
            return true;
        } else if (mStatusBarWeekday != null && preference == mStatusBarWeekday) {
            int StatusBarWeekday = Integer.valueOf((String) newValue);
            int index = mStatusBarWeekday.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver, Settings.System.STATUS_BAR_CLOCK_WEEKDAY, StatusBarWeekday);
            mStatusBarWeekday.setSummary(mStatusBarWeekday.getEntries()[index]);
            return true;
        } else if (preference == mStatusBarNetworkActivity) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.STATUS_BAR_NETWORK_ACTIVITY, value ? 1 : 0);
            return true;
        } else if (preference == mStatusBarTraffic) {
            // Increment the state and then update the label
            int intState = Settings.System.getInt(resolver, Settings.System.STATUS_BAR_TRAFFIC, 0);
            intState++;
            intState = setStatusBarTrafficSummary(intState);
            Settings.System.putInt(resolver, Settings.System.STATUS_BAR_TRAFFIC, intState);
            return intState <= 1;
        }

        return false;
    }

    private void enableStatusBarBatteryDependents(String value) {
        boolean enabled = !value.equals(STATUS_BAR_STYLE_TEXT)
                && !value.equals(STATUS_BAR_STYLE_HIDDEN);
        mStatusBarBatteryShowPercent.setEnabled(enabled);
    }

    private int setStatusBarTrafficSummary(int intState) {
        // These states must match com.android.systemui.statusbar.policy.Traffic
        if (intState == 1) {
            mStatusBarTraffic.setSummary(R.string.show_network_speed_bits);
        } else if (intState == 2) {
            mStatusBarTraffic.setSummary(R.string.show_network_speed_bytes);
        } else {
            mStatusBarTraffic.setSummary(R.string.show_network_speed_summary);
            return 0;
        }
        return intState;
    }
}
