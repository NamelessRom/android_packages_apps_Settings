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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import org.cyanogenmod.hardware.TapToWake;

public class GestureSettings extends SettingsPreferenceFragment {
    private static final String TAG = "GestureSettings";

    private static final String KEY_CATEGORY_GESTURES = "category_gestures";

    private static final String KEY_GESTURES = "device_specific_gesture_settings";
    private static final String KEY_TAP_TO_WAKE = "double_tap_wake_gesture";

    private CheckBoxPreference mTapToWake;

    @Override public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.gesture_settings);

        // update or remove gesture activity
        Utils.updatePreferenceToSpecificActivityFromMetaDataOrRemove(getActivity(),
                getPreferenceScreen(), KEY_GESTURES);

        PreferenceCategory category = (PreferenceCategory) findPreference(KEY_CATEGORY_GESTURES);
        mTapToWake = (CheckBoxPreference) findPreference(KEY_TAP_TO_WAKE);
        if (!isTapToWakeSupported()) {
            category.removePreference(mTapToWake);
            mTapToWake = null;
        }
    }

    @Override public void onResume() {
        super.onResume();

        if (mTapToWake != null) {
            final SharedPreferences prefs =
                    PreferenceManager.getDefaultSharedPreferences(getActivity());
            mTapToWake.setChecked(prefs.getBoolean(KEY_TAP_TO_WAKE, true));
        }
    }

    @Override public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mTapToWake) {
            final SharedPreferences prefs =
                    PreferenceManager.getDefaultSharedPreferences(getActivity());
            prefs.edit().putBoolean(KEY_TAP_TO_WAKE, mTapToWake.isChecked()).commit();
            return TapToWake.setEnabled(mTapToWake.isChecked());
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    /**
     * Restore the properties associated with this preference on boot
     *
     * @param ctx A valid context
     */
    public static void restore(final Context ctx) {
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

    private static boolean isTapToWakeSupported() {
        try {
            return TapToWake.isSupported();
        } catch (NoClassDefFoundError e) {
            // Hardware abstraction framework not installed
            return false;
        }
    }
}
