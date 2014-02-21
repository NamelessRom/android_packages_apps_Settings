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
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.Rect;
import android.net.Uri;
import android.hardware.Camera;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.Display;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.android.internal.util.nameless.NamelessUtils;
import com.android.internal.util.nameless.constants.FlashLightConstants;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.ChooseLockSettingsHelper;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class LockscreenInterface extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String LOCKSCREEN_GENERAL_CATEGORY = "lockscreen_general_category";
    private static final String LOCKSCREEN_WIDGETS_CATEGORY = "lockscreen_widgets_category";
    private static final String KEY_BATTERY_STATUS = "lockscreen_battery_status";
    private static final String KEY_LOCKSCREEN_BUTTONS = "lockscreen_buttons";
    private static final String KEY_ENABLE_WIDGETS = "keyguard_enable_widgets";
    private static final String KEY_LOCK_CLOCK = "lock_clock";
    private static final String PREF_LOCKSCREEN_USE_CAROUSEL = "lockscreen_use_widget_container_carousel";

    // Omni Additions
    private static final String BATTERY_AROUND_LOCKSCREEN_RING = "battery_around_lockscreen_ring";

    private static final String KEY_ENABLE_CAMERA = "keyguard_enable_camera";
    private static final String KEY_SEE_TRHOUGH = "see_through";
    private static final String KEY_WIDGETS_CATAGORY = "widgets_catagory";

    // Nameless Additions
    private static final String KEY_LOCKSCREEN_TORCH = "lockscreen_glowpad_torch";

    // Lock Screen Background
    private static final String WALLPAPER_NAME = "lockscreen_wallpaper";
    private static final String LOCKSCREEN_BACKGROUND = "lockscreen_background";
    private static final String LOCKSCREEN_BACKGROUND_STYLE = "lockscreen_background_style";
    private static final String LOCKSCREEN_BACKGROUND_COLOR_FILL = "lockscreen_background_color_fill;

    private static final int REQUEST_PICK_WALLPAPER = 201;
    private static final int COLOR_FILL = 0;
    private static final int CUSTOM_IMAGE = 1;
    private static final int DEFAULT = 2;

    private PreferenceCategory mWidgetsCatagory;

    private ListPreference mBatteryStatus;
    private CheckBoxPreference mEnableKeyguardWidgets;
    private CheckBoxPreference mEnableCameraWidget;
    private CheckBoxPreference mSeeThrough;
    private CheckBoxPreference mLockscreenUseCarousel;

    // Lock Screen Background -- will reorganize this later
    private ColorPickerPreference mLockColorFill;
    private ListPreference mLockBackground;

    private PreferenceCategory mLockscreenBackground;
    private File wallpaperImage;
    private File wallpaperTemporary;

    private ChooseLockSettingsHelper mChooseLockSettingsHelper;
    private LockPatternUtils mLockUtils;
    private DevicePolicyManager mDPM;

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
    }

    private PreferenceScreen createCustomView() {
        mLockscreenBackground = (PreferenceCategory) findPreference(LOCKSCREEN_BACKGROUND);

        mLockBackground = (ListPreference) findPreference(LOCKSCREEN_BACKGROUND_STYLE);
        mLockBackground.setOnPreferenceChangeListener(this);
        mLockBackground.setValue(Integer.toString(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_BACKGROUND_STYLE, 2)));
        mLockBackground.setSummary(mLockBackground.getEntry());

        mLockColorFill = (ColorPickerPreference) findPreference(LOCKSCREEN_BACKGROUND_COLOR_FILL);
        mLockColorFill.setOnPreferenceChangeListener(this);
        mLockColorFill.setSummary(ColorPickerPreference.convertToARGB(
                Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_BACKGROUND_COLOR, 0x00000000)));

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
    }

	private Uri getLockscreenExternalUri() {
        File dir = getActivity().getExternalCacheDir();
        File wallpaper = new File(dir, WALLPAPER_NAME);
        return Uri.fromFile(wallpaper);
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
            int index = mLockBackground.findIndexOfValue(String.valueOf(newValue));
            preference.setSummary(mLockBackground.getEntries()[index]);
            return handleBackgroundSelection(index);
        } else if (preference == mLockColorFill) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int value = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_BACKGROUND_COLOR, value);
            return true;
        }
        return false;
    }

    private void updateVisiblePreferences() {
        int visible = Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_BACKGROUND_STYLE, 2);
        if (visible == 0) {
            mLockscreenBackground.addPreference(mLockColorFill);
        } else {
            mLockscreenBackground.removePreference(mLockColorFill);
        }
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

    private boolean handleBackgroundSelection(int index) {
        if (index == COLOR_FILL) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_BACKGROUND_STYLE, 0);
			            updateVisiblePreferences();
            return true;
        } else if (index == CUSTOM_IMAGE) {
            // Used to reset the image when already set
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_BACKGROUND_STYLE, 2);
            // Launches intent for user to select an image/crop it to set as background
            Display display = getActivity().getWindowManager().getDefaultDisplay();

            int width = getActivity().getWallpaperDesiredMinimumWidth();
            int height = getActivity().getWallpaperDesiredMinimumHeight();
            float spotlightX = (float)display.getWidth() / width;
            float spotlightY = (float)display.getHeight() / height;

            Intent intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("scale", true);
            intent.putExtra("scaleUpIfNeeded", true);
            intent.putExtra("aspectX", width);
            intent.putExtra("aspectY", height);
            intent.putExtra("outputX", width);
            intent.putExtra("outputY", height)'
            intent.putExtra("spotlightX", spotlightX);
            intent.putExtra("spotlightY", spotlightY);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, getLockscreenExternalUri());

            startActivityForResult(intent, REQUEST_PICK_WALLPAPER);
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
