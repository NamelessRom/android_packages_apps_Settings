/*
 * Copyright (C) 2012-2014 The CyanogenMod Project
 * Modifications Copyright (C) 2013-2014 The NamelessROM Project
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

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.MediaStore;
import android.view.Display;
import android.view.Window;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.android.internal.util.nameless.NamelessUtils;
import com.android.internal.util.nameless.constants.FlashLightConstants;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.cyanogenmod.SeekBarPreference;
import com.android.settings.notificationlight.ColorPickerView;
import com.android.settings.ChooseLockSettingsHelper;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import java.io.File;
import java.io.IOException;

public class LockscreenInterface extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String LOCKSCREEN_GENERAL_CATEGORY = "lockscreen_general_category";
    private static final String LOCKSCREEN_WIDGETS_CATEGORY = "lockscreen_widgets_category";
    private static final String LOCKSCREEN_BACKGROUND_CATEGORY = "lockscreen_background_category";
    private static final String KEY_BATTERY_STATUS = "lockscreen_battery_status";
    private static final String KEY_LOCKSCREEN_BUTTONS = "lockscreen_buttons";
    private static final String KEY_ENABLE_WIDGETS = "keyguard_enable_widgets";
    private static final String KEY_LOCK_CLOCK = "lock_clock";
    private static final String PREF_LOCKSCREEN_USE_CAROUSEL = "lockscreen_use_widget_container_carousel";
    private static final String LOCKSCREEN_BACKGROUND_STYLE = "lockscreen_background_style";
    private static final String LOCKSCREEN_WALLPAPER_ALPHA = "lockscreen_wallpaper_alpha";

    private static final int REQUEST_PICK_WALLPAPER = 201;
    private static final int COLOR_FILL = 0;
    private static final int CUSTOM_IMAGE = 1;
    private static final int DEFAULT = 2;

    // Omni Additions
    private static final String BATTERY_AROUND_LOCKSCREEN_RING = "battery_around_lockscreen_ring";

    private static final String KEY_ENABLE_CAMERA = "keyguard_enable_camera";
    private static final String KEY_SEE_TRHOUGH = "see_through";
    private static final String KEY_WIDGETS_CATAGORY = "widgets_catagory";

    // Nameless Additions
    private static final String KEY_LOCKSCREEN_TORCH = "lockscreen_glowpad_torch";

    private PreferenceCategory mWidgetsCatagory;

    private ListPreference mBatteryStatus;
    private CheckBoxPreference mEnableKeyguardWidgets;
    private CheckBoxPreference mEnableCameraWidget;
    private CheckBoxPreference mSeeThrough;
    private CheckBoxPreference mLockscreenUseCarousel;
    private ListPreference mLockBackground;
    private SeekBarPreference mWallpaperAlpha;

    private ChooseLockSettingsHelper mChooseLockSettingsHelper;
    private LockPatternUtils mLockUtils;
    private DevicePolicyManager mDPM;

    private File wallpaperImage;
    private File wallpaperTemporary;
    private PreferenceCategory backgroundCategory;

    // Omni Additions
    private CheckBoxPreference mLockRingBattery;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.lockscreen_interface_settings);

        mChooseLockSettingsHelper = new ChooseLockSettingsHelper(getActivity());
        mLockUtils = mChooseLockSettingsHelper.utils();
        mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

        mWidgetsCatagory = (PreferenceCategory) findPreference(KEY_WIDGETS_CATAGORY);
        mLockscreenUseCarousel = (CheckBoxPreference) findPreference(PREF_LOCKSCREEN_USE_CAROUSEL);
        if (!showCarousel()) {
            mWidgetsCatagory.removePreference(mLockscreenUseCarousel);
        } else {
            mLockscreenUseCarousel.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_USE_WIDGET_CONTAINER_CAROUSEL, 0) == 1);
        }

        // Find categories
        PreferenceCategory generalCategory = (PreferenceCategory)
                findPreference(LOCKSCREEN_GENERAL_CATEGORY);
        PreferenceCategory widgetsCategory = (PreferenceCategory)
                findPreference(LOCKSCREEN_WIDGETS_CATEGORY);
        backgroundCategory = (PreferenceCategory) findPreference(LOCKSCREEN_BACKGROUND_CATEGORY);

        // Find preferences
        mEnableKeyguardWidgets = (CheckBoxPreference) findPreference(KEY_ENABLE_WIDGETS);

        // lockscreen see through
        mSeeThrough = (CheckBoxPreference) findPreference(KEY_SEE_TRHOUGH);

        mBatteryStatus = (ListPreference) findPreference(KEY_BATTERY_STATUS);
        if (mBatteryStatus != null) {
            mBatteryStatus.setOnPreferenceChangeListener(this);
        }

        // Remove lockscreen button actions if device doesn't have hardware keys
        if (!hasButtons()) {
            generalCategory.removePreference(findPreference(KEY_LOCKSCREEN_BUTTONS));
        }

        // Enable or disable lockscreen widgets based on policy
        checkDisabledByPolicy(mEnableKeyguardWidgets,
                DevicePolicyManager.KEYGUARD_DISABLE_WIDGETS_ALL);

        // Remove cLock settings item if not installed
        if (!isPackageInstalled("com.cyanogenmod.lockclock")) {
            widgetsCategory.removePreference(findPreference(KEY_LOCK_CLOCK));
        }

        // Remove maximize widgets on tablets
        if (!Utils.isPhone(getActivity())) {
            widgetsCategory.removePreference(
                    findPreference(Settings.System.LOCKSCREEN_MAXIMIZE_WIDGETS));
        }

        mEnableCameraWidget = (CheckBoxPreference) findPreference(KEY_ENABLE_CAMERA);
        // Enable or disable camera widget based on device and policy
        if (Camera.getNumberOfCameras() == 0) {
            widgetsCategory.removePreference(mEnableCameraWidget);
            mEnableCameraWidget = null;
        } else if (mLockUtils.isSecure()) {
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

        // Glowpad Torch
        if (!NamelessUtils.isPackageInstalled(getActivity(), FlashLightConstants.APP_PACKAGE_NAME)) {
            generalCategory.removePreference(findPreference(KEY_LOCKSCREEN_TORCH));
        }

        mLockBackground = (ListPreference) findPreference(LOCKSCREEN_BACKGROUND_STYLE);
        mLockBackground.setOnPreferenceChangeListener(this);
        mLockBackground.setValue(Integer.toString(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_BACKGROUND_STYLE, 2)));
        mLockBackground.setSummary(mLockBackground.getEntry());

        mWallpaperAlpha = (SeekBarPreference) findPreference(LOCKSCREEN_WALLPAPER_ALPHA);
        mWallpaperAlpha.setOnPreferenceChangeListener(this);
        float mWallpaperAlphaTransparency = 1.0f;
        try {
            mWallpaperAlphaTransparency = Settings.System.getFloat(getContentResolver(),
                    Settings.System.LOCKSCREEN_WALLPAPER_ALPHA);
        } catch (Exception e) {
            mWallpaperAlphaTransparency = 1.0f;
            Settings.System.putFloat(getContentResolver(),
                    Settings.System.LOCKSCREEN_WALLPAPER_ALPHA, 1.0f);
        }
        mWallpaperAlpha.setInitValue((int) (mWallpaperAlphaTransparency * 100));

        wallpaperImage = new File(getActivity().getFilesDir() + "/lockwallpaper");
        wallpaperTemporary = new File(getActivity().getCacheDir() + "/lockwallpaper.tmp");
        updateVisiblePreferences();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Update custom widgets and camera
        if (mEnableKeyguardWidgets != null) {
            mEnableKeyguardWidgets.setChecked(mLockUtils.getWidgetsEnabled());
        }

        if (mEnableCameraWidget != null) {
            mEnableCameraWidget.setChecked(mLockUtils.getCameraEnabled());
        }

        // Update battery status
        if (mBatteryStatus != null) {
            ContentResolver cr = getActivity().getContentResolver();
            int batteryStatus = Settings.System.getInt(cr,
                    Settings.System.LOCKSCREEN_BATTERY_VISIBILITY, 0);
            mBatteryStatus.setValueIndex(batteryStatus);
            mBatteryStatus.setSummary(mBatteryStatus.getEntries()[batteryStatus]);
        }
        updateVisiblePreferences();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        final String key = preference.getKey();

        if (KEY_ENABLE_WIDGETS.equals(key)) {
            mLockUtils.setWidgetsEnabled(mEnableKeyguardWidgets.isChecked());
        } else if (preference == mLockRingBattery) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.BATTERY_AROUND_LOCKSCREEN_RING, mLockRingBattery.isChecked() ? 1 : 0);
            return true;
        } else if (KEY_ENABLE_CAMERA.equals(key)) {
            mLockUtils.setCameraEnabled(mEnableCameraWidget.isChecked());
            return true;
        } else if (preference == mSeeThrough) {
            Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_SEE_THROUGH,
                    mSeeThrough.isChecked() ? 1 : 0);
        } else if (preference == mLockscreenUseCarousel) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_USE_WIDGET_CONTAINER_CAROUSEL,
                    mLockscreenUseCarousel.isChecked() ? 1 : 0);
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver cr = getActivity().getContentResolver();

        if (preference == mBatteryStatus) {
            int value = Integer.valueOf((String) objValue);
            int index = mBatteryStatus.findIndexOfValue((String) objValue);
            Settings.System.putInt(cr, Settings.System.LOCKSCREEN_BATTERY_VISIBILITY, value);
            mBatteryStatus.setSummary(mBatteryStatus.getEntries()[index]);
            return true;
        } else if (preference == mLockBackground) {
            int index = mLockBackground.findIndexOfValue(objValue.toString());
            preference.setSummary(mLockBackground.getEntries()[index]);
            handleBackgroundSelection(index);
            return true;
        } else if (preference == mWallpaperAlpha) {
            float value = Float.parseFloat((String) objValue);
            Settings.System.putFloat(getContentResolver(),
                    Settings.System.LOCKSCREEN_WALLPAPER_ALPHA, value / 100);
            return true;
        }
        return false;
    }

    /**
     * Checks if the device has hardware buttons.
     * @return has Buttons
     */
    public boolean hasButtons() {
        return (getResources().getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys) > 0);
    }

    /**
     * Checks if a specific policy is disabled by a device administrator, and disables the
     * provided preference if so.
     * @param preference Preference
     * @param feature Feature
     */
    private void checkDisabledByPolicy(Preference preference, int feature) {
        boolean disabled = featureIsDisabled(feature);

        if (disabled) {
            preference.setSummary(R.string.security_enable_widgets_disabled_summary);
        }

        preference.setEnabled(!disabled);
    }

    /**
     * Checks if a specific policy is disabled by a device administrator.
     * @param feature Feature
     * @return Is disabled
     */
    private boolean featureIsDisabled(int feature) {
        return (mDPM.getKeyguardDisabledFeatures(null) & feature) != 0;
    }

    public boolean showCarousel() {
        return !getResources().getBoolean(R.bool.config_show_carousel);
    }

    private void updateVisiblePreferences() {
        int visible = Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_BACKGROUND_STYLE, 2);
        if (visible == 1) {
            backgroundCategory.addPreference(mWallpaperAlpha);
        } else {
            backgroundCategory.removePreference(mWallpaperAlpha);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PICK_WALLPAPER) {
            if (resultCode == Activity.RESULT_OK) {
                if (wallpaperTemporary.exists()) {
                    wallpaperTemporary.renameTo(wallpaperImage);
                }
                wallpaperImage.setReadable(true, false);
                Toast.makeText(getActivity(), getResources().getString(R.string.
                        background_result_successful), Toast.LENGTH_LONG).show();
                Settings.System.putInt(getContentResolver(),
                        Settings.System.LOCKSCREEN_BACKGROUND_STYLE, 1);
                updateVisiblePreferences();
            } else {
                if (wallpaperTemporary.exists()) {
                    wallpaperTemporary.delete();
                }
                Toast.makeText(getActivity(), getResources().getString(R.string.
                        background_result_not_successful), Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean handleBackgroundSelection(int index) {
        if (index == COLOR_FILL) {
            final ColorPickerView colorView = new ColorPickerView(getActivity());
            int currentColor = Settings.System.getInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_BACKGROUND_COLOR, -1);

            if (currentColor != -1) {
                colorView.setColor(currentColor);
            }
            colorView.setAlphaSliderVisible(true);

            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.background_color_fill_title)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Settings.System.putInt(getContentResolver(),
                                    Settings.System.LOCKSCREEN_BACKGROUND_STYLE, 0);
                            Settings.System.putInt(getContentResolver(),
                                    Settings.System.LOCKSCREEN_BACKGROUND_COLOR,
                                    colorView.getColor());
                            updateVisiblePreferences();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setView(colorView)
                    .show();
        } else if (index == CUSTOM_IMAGE) {
            // Launches intent for user to select an image/crop it to set as background
            final Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
            intent.setType("image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("scale", true);
            intent.putExtra("scaleUpIfNeeded", false);
            intent.putExtra("scaleType", 6);
            intent.putExtra("layout_width", -1);
            intent.putExtra("layout_height", -2);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());

            final Display display = getActivity().getWindowManager().getDefaultDisplay();
            boolean isPortrait = getResources().getConfiguration().orientation ==
                    Configuration.ORIENTATION_PORTRAIT;

            int width = display.getWidth();
            int height = display.getHeight();

            intent.putExtra("aspectX", isPortrait ? width : height);
            intent.putExtra("aspectY", isPortrait ? height : width);

            try {
                wallpaperTemporary.createNewFile();
                wallpaperTemporary.setWritable(true, false);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(wallpaperTemporary));
                intent.putExtra("return-data", false);
                getActivity().startActivityFromFragment(this, intent, REQUEST_PICK_WALLPAPER);
            } catch (IOException e) {
            } catch (ActivityNotFoundException e) {
            }
        } else if (index == DEFAULT) {
            // Sets background to default
            Settings.System.putInt(getContentResolver(),
                            Settings.System.LOCKSCREEN_BACKGROUND_STYLE, 2);
            updateVisiblePreferences();
            return true;
        }
        return false;
    }
}
