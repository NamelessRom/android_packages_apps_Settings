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

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.cyanogenmod.SystemSettingSwitchPreference;

import java.util.List;

public class NavigationBarSettings extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "NavigationBarSettings";

    private static final String CATEGORY_NAV_BAR = "navigation_bar";
    private static final String CATEGORY_NAV_BAR_SIMULATE = "navigation_bar_simulate";

    private static final String KEY_FORCE_ENABLE_NAVBAR = "navbar_force_enable";
    private static final String KEY_HARDWARE_KEYS_DISABLE = "hardware_keys_disable";
    private static final String KEY_NAVIGATION_RECENTS_LONG_PRESS = "navigation_recents_long_press";

    private static final String PREF_BUTTON_BACKLIGHT = "pref_navbar_button_backlight";

    private SystemSettingSwitchPreference mForceEnableNavbar;
    private SystemSettingSwitchPreference mHardwareKeysDisable;
    private ListPreference mNavigationRecentsLongPressAction;

    private PreferenceCategory mNavigationBarCategory;

    private Handler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.navigation_bar_settings);

        mNavigationBarCategory = (PreferenceCategory) findPreference(CATEGORY_NAV_BAR);

        // Navigation bar recents long press activity needs custom setup
        mNavigationRecentsLongPressAction =
                initRecentsLongPressAction(KEY_NAVIGATION_RECENTS_LONG_PRESS);

        final boolean hasRealNavigationBar = getResources()
                .getBoolean(com.android.internal.R.bool.config_showNavigationBar);

        // only disable on devices with REAL navigation bars
        if (!hasRealNavigationBar) {
            mForceEnableNavbar =
                    (SystemSettingSwitchPreference) findPreference(KEY_FORCE_ENABLE_NAVBAR);
            mForceEnableNavbar.setOnPreferenceChangeListener(this);
            mHardwareKeysDisable =
                    (SystemSettingSwitchPreference) findPreference(KEY_HARDWARE_KEYS_DISABLE);
            mHardwareKeysDisable.setOnPreferenceChangeListener(this);

            enableNavigationBarCategory(mForceEnableNavbar.isChecked());
        } else {
            final Preference pref = findPreference(CATEGORY_NAV_BAR_SIMULATE);
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }
        }

        mHandler = new Handler();
    }

    private void enableNavigationBarCategory(final boolean shouldEnable) {
        if (mNavigationBarCategory == null) return;

        final List<Preference> preferences = mNavigationBarCategory.getPreferenceList();
        for (final Preference preference : preferences) {
            preference.setEnabled(shouldEnable);
        }

        // disable navigation recents long press again if not available
        if (!shouldEnable) {
            mNavigationRecentsLongPressAction.setEnabled(false);
        } else {
            PackageManager pm = getPackageManager();
            Intent intent = new Intent(Intent.ACTION_RECENTS_LONG_PRESS);
            List<ResolveInfo> recentsActivities = pm.queryIntentActivities(intent,
                    PackageManager.MATCH_DEFAULT_ONLY);
            mNavigationRecentsLongPressAction.setEnabled(recentsActivities.size() != 0);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (mForceEnableNavbar == preference) {
            mForceEnableNavbar.setEnabled(false);
            enableNavigationBarCategory(false);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mForceEnableNavbar.setEnabled(true);
                    enableNavigationBarCategory(mForceEnableNavbar.isChecked());
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
                final int currentBrightness = Settings.Secure.getInt(getContentResolver(),
                        Settings.Secure.BUTTON_BRIGHTNESS, defaultBrightness);
                if (!prefs.contains(PREF_BUTTON_BACKLIGHT)) {
                    editor.putInt(PREF_BUTTON_BACKLIGHT, currentBrightness);
                }
                Settings.Secure.putInt(getContentResolver(), Settings.Secure.BUTTON_BRIGHTNESS, 0);
            } else {
                final int oldBright = prefs.getInt(PREF_BUTTON_BACKLIGHT, -1);
                if (oldBright != -1) {
                    Settings.Secure.putInt(getContentResolver(),
                            Settings.Secure.BUTTON_BRIGHTNESS, oldBright);
                    editor.remove(PREF_BUTTON_BACKLIGHT);
                }
            }
            editor.commit();
            return true;
        } else if (preference == mNavigationRecentsLongPressAction) {
            // RecentsLongPressAction is handled differently because it intentionally uses
            // Settings.Secure
            String putString = (String) newValue;
            int index = mNavigationRecentsLongPressAction.findIndexOfValue(putString);
            CharSequence summary = mNavigationRecentsLongPressAction.getEntries()[index];

            // Update the summary
            mNavigationRecentsLongPressAction.setSummary(summary);
            if (putString.length() == 0) {
                putString = null;
            }
            Settings.Secure.putString(getContentResolver(),
                    Settings.Secure.RECENTS_LONG_PRESS_ACTIVITY, putString);
            return true;
        }

        return false;
    }

    private ListPreference initRecentsLongPressAction(String key) {
        ListPreference list = (ListPreference) getPreferenceScreen().findPreference(key);
        list.setOnPreferenceChangeListener(this);

        // Read the componentName from Settings.Secure, this is the user's prefered setting
        String componentString = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.RECENTS_LONG_PRESS_ACTIVITY);
        ComponentName targetComponent = null;
        if (componentString == null) {
            list.setSummary(getString(R.string.hardware_keys_action_last_app));
        } else {
            targetComponent = ComponentName.unflattenFromString(componentString);
        }

        // Dyanamically generate the list array, query PackageManager for all Activites that are
        // registered for ACTION_RECENTS_LONG_PRESS
        PackageManager pm = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_RECENTS_LONG_PRESS);
        List<ResolveInfo> recentsActivities = pm.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        if (recentsActivities.size() == 0) {
            // No entries available, disable
            list.setSummary(getString(R.string.hardware_keys_action_last_app));
            Settings.Secure.putString(getContentResolver(),
                    Settings.Secure.RECENTS_LONG_PRESS_ACTIVITY, null);
            list.setEnabled(false);
            return list;
        }

        CharSequence[] entries = new CharSequence[recentsActivities.size() + 1];
        CharSequence[] values = new CharSequence[recentsActivities.size() + 1];
        // First entry is always default last app
        entries[0] = getString(R.string.hardware_keys_action_last_app);
        values[0] = "";
        list.setValue(values[0].toString());
        int i = 1;
        for (ResolveInfo info : recentsActivities) {
            try {
                // Use pm.getApplicationInfo for the label, we cannot rely on ResolveInfo that
                // comes back from queryIntentActivities.
                entries[i] = pm.getApplicationInfo(info.activityInfo.packageName, 0).loadLabel(pm);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Error package not found: " + info.activityInfo.packageName, e);
                // Fallback to package name
                entries[i] = info.activityInfo.packageName;
            }

            // Set the value to the ComponentName that will handle this intent
            ComponentName entryComponent = new ComponentName(info.activityInfo.packageName,
                    info.activityInfo.name);
            values[i] = entryComponent.flattenToString();
            if (targetComponent != null) {
                if (entryComponent.equals(targetComponent)) {
                    // Update the selected value and the preference summary
                    list.setSummary(entries[i]);
                    list.setValue(values[i].toString());
                }
            }
            i++;
        }
        list.setEntries(entries);
        list.setEntryValues(values);
        return list;
    }
}
