/*
 * Copyright (C) 2014 - 2015 The NamelessRom project
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
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManagerGlobal;

import com.android.internal.util.cm.ScreenType;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NavigationBarSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {
    private static final String TAG = "NavigationBarSettings";

    private static final String DISABLE_NAV_KEYS = "disable_nav_keys";
    private static final String KEY_NAVIGATION_BAR_LEFT = "navigation_bar_left";
    private static final String KEY_NAVIGATION_RECENTS_LONG_PRESS = "navigation_recents_long_press";

    private static final String CATEGORY_NAVBAR = "navigation_bar_category";

    private PreferenceCategory mNavigationPreferencesCat;

    private SwitchPreference mDisableNavigationKeys;
    private ListPreference mNavigationRecentsLongPressAction;

    private Handler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.navigation_bar_settings);

        mHandler = new Handler();

        final PreferenceScreen prefScreen = getPreferenceScreen();

        // Force Navigation bar related options
        mDisableNavigationKeys = (SwitchPreference) findPreference(DISABLE_NAV_KEYS);

        mNavigationPreferencesCat = (PreferenceCategory) findPreference(CATEGORY_NAVBAR);

        // Navigation bar recents long press activity needs custom setup
        mNavigationRecentsLongPressAction =
                initRecentsLongPressAction(KEY_NAVIGATION_RECENTS_LONG_PRESS);

        HashMap<String, String> prefsToRemove = (HashMap<String, String>)
                getPreferencesToRemove(this, getActivity());
        for (String key : prefsToRemove.keySet()) {
            String category = prefsToRemove.get(key);
            Preference preference = findPreference(key);
            if (category != null) {
                // Parent is a category
                PreferenceCategory preferenceCategory =
                        (PreferenceCategory) findPreference(category);
                if (preferenceCategory != null) {
                    // Preference category might have already been removed
                    preferenceCategory.removePreference(preference);
                }
            } else {
                // Either parent is preference screen, or remove whole category
                removePreference(key);
            }
        }

        if (mNavigationPreferencesCat.getPreferenceCount() == 0) {
            // Hide navigation bar category
            prefScreen.removePreference(mNavigationPreferencesCat);
        }

    }

    private static Map<String, String> getPreferencesToRemove(NavigationBarSettings settings,
            Context context) {
        HashMap<String, String> result = new HashMap<>();

        // Only visible on devices that does not have a navigation bar already,
        // and don't even try unless the existing keys can be disabled
        boolean needsNavigationBar = false;
        try {
            needsNavigationBar = WindowManagerGlobal.getWindowManagerService().needsNavigationBar();
        } catch (RemoteException ignored) { }

        if (needsNavigationBar) {
            result.put(DISABLE_NAV_KEYS, null);
        } else {
            // Remove keys that can be provided by the navbar
            if (settings != null) {
                settings.updateDisableNavkeysOption();
                settings.mNavigationPreferencesCat.setEnabled(
                        settings.mDisableNavigationKeys.isChecked());
            }
        }

        if (!ScreenType.isPhone(context)) {
            result.put(KEY_NAVIGATION_BAR_LEFT, CATEGORY_NAVBAR);
        }

        return result;
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

        // Dyanamically generate the list array,
        // query PackageManager for all Activites that are registered for ACTION_RECENTS_LONG_PRESS
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
                // Use pm.getApplicationInfo for the label,
                // we cannot rely on ResolveInfo that comes back from queryIntentActivities.
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

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mNavigationRecentsLongPressAction) {
            // RecentsLongPressAction is handled differently because it intentionally uses
            // Settings.System
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

    private void updateDisableNavkeysOption() {
        boolean enabled = Settings.Secure.getInt(getActivity().getContentResolver(),
                Settings.Secure.DEV_FORCE_SHOW_NAVBAR, 0) != 0;

        mDisableNavigationKeys.setChecked(enabled);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mDisableNavigationKeys) {
            mDisableNavigationKeys.setEnabled(false);
            mNavigationPreferencesCat.setEnabled(false);
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.DEV_FORCE_SHOW_NAVBAR,
                    mDisableNavigationKeys.isChecked() ? 1 : 0);
            updateDisableNavkeysOption();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDisableNavigationKeys.setEnabled(true);
                    mNavigationPreferencesCat.setEnabled(mDisableNavigationKeys.isChecked());
                }
            }, 1000);
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result = new ArrayList<>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.navigation_bar_settings;
                    result.add(sir);

                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    ArrayList<String> result = new ArrayList<>();

                    Map<String, String> items = getPreferencesToRemove(null, context);
                    for (String key : items.keySet()) {
                        result.add(key);
                    }
                    return result;
                }
            };

}

