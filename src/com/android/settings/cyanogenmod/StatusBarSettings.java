/*
 * Copyright (C) 2014-2015 The CyanogenMod Project
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
package com.android.settings.cyanogenmod;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.widget.SeekBarPreference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class StatusBarSettings extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener, Indexable {

    private static final String TAG = "StatusBar";

    private static final String STATUS_BAR_CLOCK_STYLE = "status_bar_clock";
    private static final String STATUS_BAR_AM_PM = "status_bar_am_pm";
    private static final String STATUS_BAR_DATE = "status_bar_date";
    private static final String STATUS_BAR_DATE_STYLE = "status_bar_date_style";
    private static final String STATUS_BAR_DATE_FORMAT = "status_bar_date_format";
    private static final String STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style";
    private static final String STATUS_BAR_SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";
    private static final String NETWORK_TRAFFIC_STATE = "network_traffic_state";
    private static final String NETWORK_TRAFFIC_UNIT = "network_traffic_unit";
    private static final String NETWORK_TRAFFIC_PERIOD = "network_traffic_period";
    private static final String NETWORK_TRAFFIC_AUTOHIDE = "network_traffic_autohide";
    private static final String NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD = "network_traffic_autohide_threshold";

    private static final int STATUS_BAR_BATTERY_STYLE_HIDDEN = 4;
    private static final int STATUS_BAR_BATTERY_STYLE_TEXT = 6;
    private int mNetTrafficVal;
    private int MASK_UP;
    private int MASK_DOWN;
    private int MASK_UNIT;
    private int MASK_PERIOD;

    public static final int CLOCK_DATE_STYLE_LOWERCASE = 1;
    public static final int CLOCK_DATE_STYLE_UPPERCASE = 2;
    private static final int CUSTOM_CLOCK_DATE_FORMAT_INDEX = 18;

    private ListPreference mStatusBarClock;
    private ListPreference mStatusBarAmPm;
    private ListPreference mStatusBarDate;
    private ListPreference mStatusBarDateStyle;
    private ListPreference mStatusBarDateFormat;
    private ListPreference mStatusBarBattery;
    private ListPreference mStatusBarBatteryShowPercent;
    private ListPreference mNetTrafficState;
    private ListPreference mNetTrafficUnit;
    private ListPreference mNetTrafficPeriod;
    private SystemSettingSwitchPreference mNetTrafficAutohide;
    private SeekBarPreference mNetTrafficAutohideThreshold;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.status_bar_settings);

        ContentResolver resolver = getActivity().getContentResolver();

        loadResources();

        mStatusBarClock = (ListPreference) findPreference(STATUS_BAR_CLOCK_STYLE);
        mStatusBarAmPm = (ListPreference) findPreference(STATUS_BAR_AM_PM);
        mStatusBarDate = (ListPreference) findPreference(STATUS_BAR_DATE);
        mStatusBarDateStyle = (ListPreference) findPreference(STATUS_BAR_DATE_STYLE);
        mStatusBarDateFormat = (ListPreference) findPreference(STATUS_BAR_DATE_FORMAT);

        mStatusBarBattery = (ListPreference) findPreference(STATUS_BAR_BATTERY_STYLE);
        mStatusBarBatteryShowPercent =
                (ListPreference) findPreference(STATUS_BAR_SHOW_BATTERY_PERCENT);

        int clockStyle = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_CLOCK, 1);
        mStatusBarClock.setValue(String.valueOf(clockStyle));
        mStatusBarClock.setSummary(mStatusBarClock.getEntry());
        mStatusBarClock.setOnPreferenceChangeListener(this);

        if (DateFormat.is24HourFormat(getActivity())) {
            mStatusBarAmPm.setEnabled(false);
            mStatusBarAmPm.setSummary(R.string.status_bar_am_pm_info);
        } else {
            int statusBarAmPm = Settings.System.getInt(resolver,
                    Settings.System.STATUS_BAR_AM_PM, 2);
            mStatusBarAmPm.setValue(String.valueOf(statusBarAmPm));
            mStatusBarAmPm.setSummary(mStatusBarAmPm.getEntry());
            mStatusBarAmPm.setOnPreferenceChangeListener(this);
        }

        int showDate = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_DATE, 0);
        mStatusBarDate.setValue(String.valueOf(showDate));
        mStatusBarDate.setSummary(mStatusBarDate.getEntry());
        mStatusBarDate.setOnPreferenceChangeListener(this);

        int dateStyle = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_DATE_STYLE, 0);
        mStatusBarDateStyle.setValue(String.valueOf(dateStyle));
        mStatusBarDateStyle.setSummary(mStatusBarDateStyle.getEntry());
        mStatusBarDateStyle.setOnPreferenceChangeListener(this);

        String dateFormat = Settings.System.getString(resolver,
                Settings.System.STATUS_BAR_DATE_FORMAT);
        if (TextUtils.isEmpty(dateFormat)) {
            mStatusBarDateFormat.setValue("EEE");
        } else {
            mStatusBarDateFormat.setValue(dateFormat);
        }
        parseClockDateFormats();
        mStatusBarDateFormat.setSummary(mStatusBarDateFormat.getEntry());
        mStatusBarDateFormat.setOnPreferenceChangeListener(this);

        parseClockDateFormats();

        int batteryStyle = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_BATTERY_STYLE, 0);
        mStatusBarBattery.setValue(String.valueOf(batteryStyle));
        mStatusBarBattery.setSummary(mStatusBarBattery.getEntry());
        mStatusBarBattery.setOnPreferenceChangeListener(this);

        int batteryShowPercent = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, 0);
        mStatusBarBatteryShowPercent.setValue(String.valueOf(batteryShowPercent));
        mStatusBarBatteryShowPercent.setSummary(mStatusBarBatteryShowPercent.getEntry());
        enableStatusBarBatteryDependents(batteryStyle);
        mStatusBarBatteryShowPercent.setOnPreferenceChangeListener(this);

        mNetTrafficState = (ListPreference) findPreference(NETWORK_TRAFFIC_STATE);
        mNetTrafficUnit = (ListPreference) findPreference(NETWORK_TRAFFIC_UNIT);
        mNetTrafficPeriod = (ListPreference) findPreference(NETWORK_TRAFFIC_PERIOD);
        mNetTrafficAutohide = (SystemSettingSwitchPreference) findPreference(NETWORK_TRAFFIC_AUTOHIDE);
        mNetTrafficAutohideThreshold = (SeekBarPreference) findPreference(NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD);

        mNetTrafficVal = Settings.System.getInt(resolver, Settings.System.NETWORK_TRAFFIC_STATE, 0);
        int intIndex = mNetTrafficVal & (MASK_UP + MASK_DOWN);
        intIndex = mNetTrafficState.findIndexOfValue(String.valueOf(intIndex));
        updateNetworkTrafficState(intIndex);

        mNetTrafficState.setValueIndex(intIndex >= 0 ? intIndex : 0);
        mNetTrafficState.setSummary(mNetTrafficState.getEntry());
        mNetTrafficState.setOnPreferenceChangeListener(this);

        mNetTrafficUnit.setValueIndex(getBit(mNetTrafficVal, MASK_UNIT) ? 1 : 0);
        mNetTrafficUnit.setSummary(mNetTrafficUnit.getEntry());
        mNetTrafficUnit.setOnPreferenceChangeListener(this);

        intIndex = (mNetTrafficVal & MASK_PERIOD) >>> 16;
        intIndex = mNetTrafficPeriod.findIndexOfValue(String.valueOf(intIndex));
        mNetTrafficPeriod.setValueIndex(intIndex >= 0 ? intIndex : 1);
        mNetTrafficPeriod.setSummary(mNetTrafficPeriod.getEntry());
        mNetTrafficPeriod.setOnPreferenceChangeListener(this);

        int netTrafficAutohideThreshold = Settings.System.getInt(resolver,
                Settings.System.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD, 10);
        mNetTrafficAutohideThreshold.setValue(netTrafficAutohideThreshold / 1);
        mNetTrafficAutohideThreshold.setOnPreferenceChangeListener(this);

        enableStatusBarClockDependents();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Adjust clock position for RTL if necessary
        Configuration config = getResources().getConfiguration();
        if (config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                mStatusBarClock.setEntries(getActivity().getResources().getStringArray(
                        R.array.status_bar_clock_style_entries_rtl));
                mStatusBarClock.setSummary(mStatusBarClock.getEntry());
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mStatusBarClock) {
            int clockStyle = Integer.parseInt((String) newValue);
            int index = mStatusBarClock.findIndexOfValue((String) newValue);
            Settings.System.putInt(
                    resolver, STATUS_BAR_CLOCK_STYLE, clockStyle);
            mStatusBarClock.setSummary(mStatusBarClock.getEntries()[index]);
            enableStatusBarClockDependents();
            return true;
        } else if (preference == mStatusBarAmPm) {
            int statusBarAmPm = Integer.valueOf((String) newValue);
            int index = mStatusBarAmPm.findIndexOfValue((String) newValue);
            Settings.System.putInt(
                    resolver, STATUS_BAR_AM_PM, statusBarAmPm);
            mStatusBarAmPm.setSummary(mStatusBarAmPm.getEntries()[index]);
            return true;
        } else if (preference == mStatusBarDate) {
            int statusBarDate = Integer.valueOf((String) newValue);
            int index = mStatusBarDate.findIndexOfValue((String) newValue);
            Settings.System.putInt(
                    resolver, STATUS_BAR_DATE, statusBarDate);
            mStatusBarDate.setSummary(mStatusBarDate.getEntries()[index]);
            enableStatusBarClockDependents();
            return true;
        } else if (preference == mStatusBarDateStyle) {
            int statusBarDateStyle = Integer.parseInt((String) newValue);
            int index = mStatusBarDateStyle.findIndexOfValue((String) newValue);
            Settings.System.putInt(
                    resolver, STATUS_BAR_DATE_STYLE, statusBarDateStyle);
            mStatusBarDateStyle.setSummary(mStatusBarDateStyle.getEntries()[index]);
            return true;
        } else if (preference ==  mStatusBarDateFormat) {
            String value = String.valueOf(newValue);
            int index = mStatusBarDateFormat.findIndexOfValue(value);
            if (index == CUSTOM_CLOCK_DATE_FORMAT_INDEX) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle(R.string.status_bar_date_string_edittext_title);
                alert.setMessage(R.string.status_bar_date_string_edittext_summary);

                final EditText input = new EditText(getActivity());
                String oldText = Settings.System.getString(resolver,
                        Settings.System.STATUS_BAR_DATE_FORMAT);
                if (oldText != null) {
                    input.setText(oldText);
                }
                alert.setView(input);

                alert.setPositiveButton(R.string.menu_save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int whichButton) {
                        String value = input.getText() != null ? input.getText().toString() : null;
                        if (!TextUtils.isEmpty(value)) {
                            Settings.System.putString(resolver,
                                    Settings.System.STATUS_BAR_DATE_FORMAT, value);
                        }
                    }
                });

                alert.setNegativeButton(R.string.menu_cancel, null);
                alert.show();
            } else if (index != -1) {
                Settings.System.putString(
                        resolver, Settings.System.STATUS_BAR_DATE_FORMAT, value);
                mStatusBarDateFormat.setSummary(mStatusBarDateFormat.getEntries()[index]);
            }
            return true;
        } else if (preference == mStatusBarBattery) {
            int batteryStyle = Integer.valueOf((String) newValue);
            int index = mStatusBarBattery.findIndexOfValue((String) newValue);
            Settings.System.putInt(
                    resolver, Settings.System.STATUS_BAR_BATTERY_STYLE, batteryStyle);
            mStatusBarBattery.setSummary(mStatusBarBattery.getEntries()[index]);
            enableStatusBarBatteryDependents(batteryStyle);
            return true;
        } else if (preference == mStatusBarBatteryShowPercent) {
            int batteryShowPercent = Integer.valueOf((String) newValue);
            int index = mStatusBarBatteryShowPercent.findIndexOfValue((String) newValue);
            Settings.System.putInt(
                    resolver, Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, batteryShowPercent);
            mStatusBarBatteryShowPercent.setSummary(
                    mStatusBarBatteryShowPercent.getEntries()[index]);
            return true;
        } else if (preference == mNetTrafficState) {
            int intState = Integer.valueOf((String) newValue);
            mNetTrafficVal = setBit(mNetTrafficVal, MASK_UP, getBit(intState, MASK_UP));
            mNetTrafficVal = setBit(mNetTrafficVal, MASK_DOWN, getBit(intState, MASK_DOWN));
            Settings.System.putInt(resolver,
                    Settings.System.NETWORK_TRAFFIC_STATE, mNetTrafficVal);
            int index = mNetTrafficState.findIndexOfValue((String) newValue);
            mNetTrafficState.setSummary(mNetTrafficState.getEntries()[index]);
            updateNetworkTrafficState(index);
            return true;
        } else if (preference == mNetTrafficUnit) {
            mNetTrafficVal = setBit(mNetTrafficVal, MASK_UNIT, ((String) newValue).equals("1"));
            Settings.System.putInt(resolver,
                    Settings.System.NETWORK_TRAFFIC_STATE, mNetTrafficVal);
            int index = mNetTrafficUnit.findIndexOfValue((String) newValue);
            mNetTrafficUnit.setSummary(mNetTrafficUnit.getEntries()[index]);
            return true;
        } else if (preference == mNetTrafficPeriod) {
            int intState = Integer.valueOf((String) newValue);
            mNetTrafficVal = setBit(mNetTrafficVal, MASK_PERIOD, false) + (intState << 16);
            Settings.System.putInt(resolver,
                    Settings.System.NETWORK_TRAFFIC_STATE, mNetTrafficVal);
            int index = mNetTrafficPeriod.findIndexOfValue((String) newValue);
            mNetTrafficPeriod.setSummary(mNetTrafficPeriod.getEntries()[index]);
            return true;
        } else if (preference == mNetTrafficAutohideThreshold) {
            int threshold = (Integer) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD, threshold * 1);
            return true;
        }
        return false;
    }

    private void enableStatusBarBatteryDependents(int batteryIconStyle) {
        if (batteryIconStyle == STATUS_BAR_BATTERY_STYLE_HIDDEN ||
                batteryIconStyle == STATUS_BAR_BATTERY_STYLE_TEXT) {
            mStatusBarBatteryShowPercent.setEnabled(false);
        } else {
            mStatusBarBatteryShowPercent.setEnabled(true);
        }
    }

    private void enableStatusBarClockDependents() {
        final ContentResolver resolver = getActivity().getContentResolver();
        final int clock = Settings.System.getInt(resolver, Settings.System.STATUS_BAR_CLOCK, 1);
        if (clock == 0) {
            mStatusBarDate.setEnabled(false);
            mStatusBarDateStyle.setEnabled(false);
            mStatusBarDateFormat.setEnabled(false);
            return;
        }

        mStatusBarDate.setEnabled(true);

        final int date = Settings.System.getInt(resolver, Settings.System.STATUS_BAR_DATE, 0);
        mStatusBarDateStyle.setEnabled(date != 0);
        mStatusBarDateFormat.setEnabled(date != 0);
    }

    private void parseClockDateFormats() {
        // Parse and repopulate mClockDateFormats's entries based on current date.
        String[] dateEntries = getResources().getStringArray(
                R.array.status_bar_date_format_entries_values);
        CharSequence parsedDateEntries[] = new String[dateEntries.length];
        Date now = new Date();

        int lastEntry = dateEntries.length - 1;
        int dateFormat = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.STATUS_BAR_DATE_STYLE, 0);
        for (int i = 0; i < dateEntries.length; i++) {
            if (i == lastEntry) {
                parsedDateEntries[i] = dateEntries[i];
                continue;
            }

            String newDate;
            CharSequence dateString = DateFormat.format(dateEntries[i], now);
            if (dateFormat == CLOCK_DATE_STYLE_LOWERCASE) {
                newDate = dateString.toString().toLowerCase();
            } else if (dateFormat == CLOCK_DATE_STYLE_UPPERCASE) {
                newDate = dateString.toString().toUpperCase();
            } else {
                newDate = dateString.toString();
            }
            parsedDateEntries[i] = newDate;
        }
        mStatusBarDateFormat.setEntries(parsedDateEntries);
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                                                                            boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.status_bar_settings;
                    result.add(sir);

                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    ArrayList<String> result = new ArrayList<String>();
                    return result;
                }
            };

    private boolean getBit(int intNumber, int intMask) {
        return (intNumber & intMask) == intMask;
    }

    private int setBit(int intNumber, int intMask, boolean blnState) {
        if (blnState) {
            return (intNumber | intMask);
        }
        return (intNumber & ~intMask);
    }

    private void loadResources() {
        Resources resources = getActivity().getResources();
        MASK_UP = resources.getInteger(R.integer.maskUp);
        MASK_DOWN = resources.getInteger(R.integer.maskDown);
        MASK_UNIT = resources.getInteger(R.integer.maskUnit);
        MASK_PERIOD = resources.getInteger(R.integer.maskPeriod);
    }

    private void updateNetworkTrafficState(int mIndex) {
        if (mIndex <= 0) {
            mNetTrafficUnit.setEnabled(false);
            mNetTrafficPeriod.setEnabled(false);
            mNetTrafficAutohide.setEnabled(false);
            mNetTrafficAutohideThreshold.setEnabled(false);
        } else {
            mNetTrafficUnit.setEnabled(true);
            mNetTrafficPeriod.setEnabled(true);
            mNetTrafficAutohide.setEnabled(true);
            mNetTrafficAutohideThreshold.setEnabled(true);
        }
    }
}
