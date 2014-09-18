/*
 * Copyright (C) 2012 Slimroms Project
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

package com.android.settings.slim;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.nameless.ColorPickerDialogFragment;
import com.android.settings.widget.SeekBarPreference;

import java.io.File;

public class NotificationDrawerStyle extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener, ColorPickerDialogFragment.OnColorPickedListener {

    private static final String TAG = "NotificationDrawerStyle";

    private static final String PREF_NOTIFICATION_WALLPAPER           =
            "notification_wallpaper";
    private static final String PREF_NOTIFICATION_WALLPAPER_LANDSCAPE =
            "notification_wallpaper_landscape";
    private static final String PREF_NOTIFICATION_WALLPAPER_ALPHA     =
            "notification_wallpaper_alpha";
    private static final String PREF_NOTIFICATION_ALPHA               =
            "notification_alpha";

    private static final int DLG_PICK_COLOR = 0;

    private DialogFragment dialogColorPicker;

    private ListPreference mNotificationWallpaper;
    private ListPreference mNotificationWallpaperLandscape;
    SeekBarPreference mWallpaperAlpha;
    SeekBarPreference mNotificationAlpha;

    private File mImageTmp;

    private static final int REQUEST_PICK_WALLPAPER           = 201;
    private static final int REQUEST_PICK_WALLPAPER_LANDSCAPE = 202;

    private Activity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = getActivity();

        addPreferencesFromResource(R.xml.notification_bg_pref);

        mImageTmp = new File(getActivity().getFilesDir() + "/notifi_bg.tmp");

        mNotificationWallpaper = (ListPreference) findPreference(PREF_NOTIFICATION_WALLPAPER);
        mNotificationWallpaper.setOnPreferenceChangeListener(this);

        mNotificationWallpaperLandscape =
                (ListPreference) findPreference(PREF_NOTIFICATION_WALLPAPER_LANDSCAPE);
        mNotificationWallpaperLandscape.setOnPreferenceChangeListener(this);

        float transparency;
        try {
            transparency = Settings.System.getFloat(getContentResolver(),
                    Settings.System.NOTIFICATION_BACKGROUND_ALPHA);
        } catch (Exception e) {
            transparency = 0;
            Settings.System.putFloat(getContentResolver(),
                    Settings.System.NOTIFICATION_BACKGROUND_ALPHA, 0.1f);
        }
        mWallpaperAlpha = (SeekBarPreference) findPreference(PREF_NOTIFICATION_WALLPAPER_ALPHA);
        mWallpaperAlpha.setInitValue((int) (transparency * 100));
        mWallpaperAlpha.setProperty(Settings.System.NOTIFICATION_BACKGROUND_ALPHA);
        mWallpaperAlpha.setOnPreferenceChangeListener(this);

        try {
            transparency = Settings.System.getFloat(getContentResolver(),
                    Settings.System.NOTIFICATION_ALPHA);
        } catch (Exception e) {
            transparency = 0;
            Settings.System.putFloat(getContentResolver(),
                    Settings.System.NOTIFICATION_ALPHA, 0.0f);
        }
        mNotificationAlpha = (SeekBarPreference) findPreference(PREF_NOTIFICATION_ALPHA);
        mNotificationAlpha.setInitValue((int) (transparency * 100));
        mNotificationAlpha.setProperty(Settings.System.NOTIFICATION_ALPHA);
        mNotificationAlpha.setOnPreferenceChangeListener(this);

        updateCustomBackgroundSummary();
    }


    @Override
    public void onResume() {
        super.onResume();
        updateCustomBackgroundSummary();
    }

    private void updateCustomBackgroundSummary() {
        int resId;
        String value = Settings.System.getString(getContentResolver(),
                Settings.System.NOTIFICATION_BACKGROUND);
        if (value == null) {
            resId = R.string.notification_background_default_wallpaper;
            mNotificationWallpaper.setValueIndex(2);
            mNotificationWallpaperLandscape.setEnabled(false);
        } else if (value.startsWith("color=")) {
            resId = R.string.notification_background_color_fill;
            mNotificationWallpaper.setValueIndex(0);
            mNotificationWallpaperLandscape.setEnabled(false);
        } else {
            resId = R.string.notification_background_custom_image;
            mNotificationWallpaper.setValueIndex(1);
            mNotificationWallpaperLandscape.setEnabled(true);
        }
        mNotificationWallpaper.setSummary(getResources().getString(resId));

        value = Settings.System.getString(getContentResolver(),
                Settings.System.NOTIFICATION_BACKGROUND_LANDSCAPE);
        if (value == null) {
            resId = R.string.notification_background_default_wallpaper;
            mNotificationWallpaperLandscape.setValueIndex(1);
        } else {
            resId = R.string.notification_background_custom_image;
            mNotificationWallpaperLandscape.setValueIndex(0);
        }
        mNotificationWallpaperLandscape.setSummary(getResources().getString(resId));
    }

    public void deleteWallpaper(boolean orientation) {
        String path = Settings.System.getString(getContentResolver(),
                Settings.System.NOTIFICATION_BACKGROUND);
        if (path != null && !path.startsWith("color=")) {
            File wallpaperToDelete = new File(Uri.parse(path).getPath());

            if (wallpaperToDelete.exists() && !orientation) {
                wallpaperToDelete.delete();
            }
        }

        path = Settings.System.getString(getContentResolver(),
                Settings.System.NOTIFICATION_BACKGROUND_LANDSCAPE);
        if (path != null) {
            File wallpaperToDelete = new File(Uri.parse(path).getPath());

            if (wallpaperToDelete.exists() && orientation) {
                wallpaperToDelete.delete();
            }
            if (orientation) {
                Settings.System.putString(getContentResolver(),
                        Settings.System.NOTIFICATION_BACKGROUND_LANDSCAPE, null);
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_PICK_WALLPAPER
                    || requestCode == REQUEST_PICK_WALLPAPER_LANDSCAPE) {

                if (mImageTmp.length() == 0 || !mImageTmp.exists()) {
                    Toast.makeText(mActivity,
                            getResources().getString(R.string.shortcut_image_not_valid),
                            Toast.LENGTH_LONG).show();
                    return;
                }
                File image = new File(mActivity.getFilesDir() + File.separator
                        + "notification_background_" + System.currentTimeMillis() + ".png");
                String path = image.getAbsolutePath();
                mImageTmp.renameTo(image);
                image.setReadable(true, false);

                if (requestCode == REQUEST_PICK_WALLPAPER) {
                    Settings.System.putString(getContentResolver(),
                            Settings.System.NOTIFICATION_BACKGROUND, path);
                } else {
                    Settings.System.putString(getContentResolver(),
                            Settings.System.NOTIFICATION_BACKGROUND_LANDSCAPE, path);
                }
            }
        } else {
            if (mImageTmp.exists()) {
                mImageTmp.delete();
            }
        }
        updateCustomBackgroundSummary();
    }

    private void startPictureCrop(int request, boolean landscape) {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        final Point point = new Point();
        display.getSize(point);
        final int width = point.x;
        final int height = point.y;
        final Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.putExtra("crop", "true");
        boolean isPortrait = getResources()
                .getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        intent.putExtra("aspectX", (landscape ? !isPortrait : isPortrait)
                ? width : height);
        intent.putExtra("aspectY", (landscape ? !isPortrait : isPortrait)
                ? height : width);
        intent.putExtra("outputX", (landscape ? !isPortrait : isPortrait)
                ? width : height);
        intent.putExtra("outputY", (landscape ? !isPortrait : isPortrait)
                ? height : width);
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
        try {
            mImageTmp.createNewFile();
            mImageTmp.setWritable(true, false);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mImageTmp));
            startActivityForResult(intent, request);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mWallpaperAlpha) {
            float valNav = Float.parseFloat((String) newValue);
            Settings.System.putFloat(getContentResolver(),
                    Settings.System.NOTIFICATION_BACKGROUND_ALPHA, valNav / 100);
            return true;
        } else if (preference == mNotificationAlpha) {
            float valNav = Float.parseFloat((String) newValue);
            Settings.System.putFloat(getContentResolver(),
                    Settings.System.NOTIFICATION_ALPHA, valNav / 100);
            return true;
        } else if (preference == mNotificationWallpaper) {
            int indexOf = mNotificationWallpaper.findIndexOfValue(newValue.toString());
            switch (indexOf) {
                //Displays color dialog when user has chosen color fill
                case 0:
                    showDialogInner(DLG_PICK_COLOR);
                    break;
                //Launches intent for user to select an image/crop it to set as background
                case 1:
                    startPictureCrop(REQUEST_PICK_WALLPAPER, false);
                    break;
                //Sets background to default
                case 2:
                    deleteWallpaper(false);
                    deleteWallpaper(true);
                    Settings.System.putString(getContentResolver(),
                            Settings.System.NOTIFICATION_BACKGROUND, null);
                    updateCustomBackgroundSummary();
                    break;
            }
            return true;
        } else if (preference == mNotificationWallpaperLandscape) {
            int indexOf = mNotificationWallpaperLandscape.findIndexOfValue(newValue.toString());
            switch (indexOf) {
                //Launches intent for user to select an image/crop it to set as background
                case 0:
                    startPictureCrop(REQUEST_PICK_WALLPAPER_LANDSCAPE, true);
                    break;
                //Sets background to default
                case 1:
                    deleteWallpaper(true);
                    updateCustomBackgroundSummary();
                    break;
            }
            return true;
        }
        return false;
    }

    private void showDialogInner(final int id) {
        if (dialogColorPicker != null) {
            dialogColorPicker.dismiss();
            dialogColorPicker = null;
        }

        dialogColorPicker = ColorPickerDialogFragment.newInstance(id, this,
                getActivity().getResources().getColor(android.R.color.holo_blue_light));
        dialogColorPicker.setTargetFragment(this, 0);
        dialogColorPicker.show(getFragmentManager(), "dialog " + id);
    }

    @Override
    public void onColorPicked(final int color) {
        deleteWallpaper(false);
        deleteWallpaper(true);
        Settings.System.putString(getContentResolver(),
                Settings.System.NOTIFICATION_BACKGROUND,
                "color=" + String.format("#%06X", (0xFFFFFF & color)));
        updateCustomBackgroundSummary();
        if (dialogColorPicker != null) {
            dialogColorPicker.dismiss();
        }
    }

}
