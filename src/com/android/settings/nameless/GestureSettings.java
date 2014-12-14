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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import org.cyanogenmod.hardware.TapToWake;

public class GestureSettings extends SettingsPreferenceFragment {
    private static final String TAG = "GestureSettings";

    private static final String CATEGORY_GESTURES = "category_gestures";

    private static final String KEY_GESTURES = "device_specific_gesture_settings";
    private static final String KEY_TAP_TO_WAKE = "double_tap_wake_gesture";

    private SwitchPreference mTapToWake;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.gesture_settings);

        // update or remove gesture activity
        Utils.updatePreferenceToSpecificActivityFromMetaDataOrRemove(getActivity(),
                getPreferenceScreen(), KEY_GESTURES);

        final PreferenceCategory category = (PreferenceCategory) findPreference(CATEGORY_GESTURES);

        mTapToWake = (SwitchPreference) findPreference(KEY_TAP_TO_WAKE);
        if (!isTapToWakeSupported()) {
            category.removePreference(mTapToWake);
            mTapToWake = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mTapToWake != null) {
            mTapToWake.setChecked(TapToWake.isEnabled());
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mTapToWake) {
            final SharedPreferences prefs =
                    PreferenceManager.getDefaultSharedPreferences(getActivity());
            prefs.edit().putBoolean(KEY_TAP_TO_WAKE, mTapToWake.isChecked()).apply();
            return TapToWake.setEnabled(mTapToWake.isChecked());
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private static boolean isTapToWakeSupported() {
        try {
            return TapToWake.isSupported();
        } catch (NoClassDefFoundError e) {
            // Hardware abstraction framework not installed
            return false;
        }
    }

    public static void restore(Context ctx) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        if (isTapToWakeSupported()) {
            final boolean enabled = prefs.getBoolean(KEY_TAP_TO_WAKE, TapToWake.isEnabled());

            if (!TapToWake.setEnabled(enabled)) {
                Log.e(TAG, "Failed to restore tap-to-wake settings.");
            } else {
                Log.d(TAG, "Tap-to-wake settings restored.");
            }
        }
    }
}
