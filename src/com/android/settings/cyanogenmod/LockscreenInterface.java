/*
 * Copyright (C) 2012 The CyanogenMod Project
 * Modifications Copyright (C) 2013 The NamelessROM Project
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

import android.app.ActivityManager;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.android.internal.widget.LockPatternUtils;
import com.android.settings.ChooseLockSettingsHelper;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class LockscreenInterface extends SettingsPreferenceFragment {
    private static final String TAG = "LockscreenInterface";

    private static final String KEY_ENABLE_WIDGETS = "keyguard_enable_widgets";
    private static final String KEY_LOCK_CLOCK = "lock_clock";
    private static final String LOCKSCREEN_WIDGETS_CATEGORY = "lockscreen_widgets_category";

    // Omni Additions
    private static final String BATTERY_AROUND_LOCKSCREEN_RING = "battery_around_lockscreen_ring";

    private static final String KEY_ENABLE_CAMERA = "keyguard_enable_camera";

    private CheckBoxPreference mEnableKeyguardWidgets;
    private CheckBoxPreference mEnableCameraWidget;

    private ChooseLockSettingsHelper mChooseLockSettingsHelper;
    private DevicePolicyManager mDPM;
    private boolean mIsPrimary;

    // Omni Additions
    private CheckBoxPreference mLockRingBattery;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mChooseLockSettingsHelper = new ChooseLockSettingsHelper(getActivity());
        mDPM = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);

        addPreferencesFromResource(R.xml.lockscreen_interface_settings);
        PreferenceCategory widgetsCategory = (PreferenceCategory) findPreference(LOCKSCREEN_WIDGETS_CATEGORY);

        // Only add if device has LockClock installed
        if (!isPackageInstalled("com.cyanogenmod.lockclock")) {
            widgetsCategory.removePreference(findPreference(KEY_LOCK_CLOCK));
        }

        // Determine which user is logged in
        mIsPrimary = UserHandle.myUserId() == UserHandle.USER_OWNER;
        if (mIsPrimary) {
            // Its the primary user, show all the settings
            if (!Utils.isPhone(getActivity())) {
                if (widgetsCategory != null) {
                    widgetsCategory.removePreference(
                            findPreference(Settings.System.LOCKSCREEN_MAXIMIZE_WIDGETS));
                }
            }

        } else {
            // Secondary user is logged in, remove all primary user specific preferences
        }

        // This applies to all users
        // Enable or disable keyguard widget checkbox based on DPM state
        mEnableKeyguardWidgets = (CheckBoxPreference) findPreference(KEY_ENABLE_WIDGETS);
        if (mEnableKeyguardWidgets != null) {
            if (ActivityManager.isLowRamDeviceStatic()) {
                    /*|| mLockPatternUtils.isLockScreenDisabled()) {*/
                // Widgets take a lot of RAM, so disable them on low-memory devices
                if (widgetsCategory != null) {
                    widgetsCategory.removePreference(findPreference(KEY_ENABLE_WIDGETS));
                    mEnableKeyguardWidgets = null;
                }
            } else {
                final boolean disabled = (0 != (mDPM.getKeyguardDisabledFeatures(null)
                        & DevicePolicyManager.KEYGUARD_DISABLE_WIDGETS_ALL));
                if (disabled) {
                    mEnableKeyguardWidgets.setSummary(
                            R.string.security_enable_widgets_disabled_summary);
                }
                mEnableKeyguardWidgets.setEnabled(!disabled);
            }
        }

        mEnableCameraWidget = (CheckBoxPreference) findPreference(KEY_ENABLE_CAMERA);
        // Enable or disable camera widget based on device and policy
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA) ||
                Camera.getNumberOfCameras() == 0) {
            widgetsCategory.removePreference(mEnableCameraWidget);
            mEnableCameraWidget = null;
        } else {
            checkDisabledByPolicy(mEnableCameraWidget,
                    DevicePolicyManager.KEYGUARD_DISABLE_SECURE_CAMERA);
        }

        // Add the additional Omni settings
        mLockRingBattery = (CheckBoxPreference) findPreference(
            BATTERY_AROUND_LOCKSCREEN_RING);
        if (mLockRingBattery != null) {
            mLockRingBattery.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.BATTERY_AROUND_LOCKSCREEN_RING, 0) == 1);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        final LockPatternUtils lockPatternUtils = mChooseLockSettingsHelper.utils();

        // Update custom widgets and camera
        if (mEnableKeyguardWidgets != null) {
            mEnableKeyguardWidgets.setChecked(lockPatternUtils.getWidgetsEnabled());
        }

        if (mEnableCameraWidget != null) {
            mEnableCameraWidget.setChecked(lockPatternUtils.getCameraEnabled());
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        final String key = preference.getKey();

        final LockPatternUtils lockPatternUtils = mChooseLockSettingsHelper.utils();
        if (KEY_ENABLE_WIDGETS.equals(key)) {
            lockPatternUtils.setWidgetsEnabled(mEnableKeyguardWidgets.isChecked());
        } else if (preference == mLockRingBattery) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.BATTERY_AROUND_LOCKSCREEN_RING, mLockRingBattery.isChecked() ? 1 : 0);
            return true;
        } else if (KEY_ENABLE_CAMERA.equals(key)) {
            mLockUtils.setCameraEnabled(mEnableCameraWidget.isChecked());
            return true;
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public static class DeviceAdminLockscreenReceiver extends DeviceAdminReceiver {}

}
