package com.android.settings.nameless;

import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.SeekBarPreference;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.WindowManager;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.cyanogenmod.SystemSettingCheckBoxPreference;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class LockscreenNotifications extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String LOCKSCREEN_NOTIFICATIONS = "lockscreen_notifications";
    private static final String INCLUDED_APPS            = "included_apps";
    private static final String EXCLUDED_APPS            = "excluded_apps";
    private static final String POCKET_MODE              = "lockscreen_notifications_pocket_mode";
    private static final String SHOW_ALWAYS              = "lockscreen_notifications_show_always";
    private static final String WAKE_ON_NOTIFICATION     =
            "lockscreen_notifications_wake_on_notification";
    private static final String OFFSET_TOP               = "offset_top";
    private static final String NOTIFICATIONS_HEIGHT     = "notifications_height";
    private static final String NOTIFICATION_COLOR       = "notification_color";
    private static final String PRIVACY_MODE             = "privacy_mode";
    private static final String EXPANDED_VIEW            = "lockscreen_notifications_expanded_view";

    private SystemSettingCheckBoxPreference mLockscreenNotifications;
    private AppMultiSelectListPreference    mExcludedAppsPref;
    private AppMultiSelectListPreference    mIncludedAppsPref;
    private SystemSettingCheckBoxPreference mPocketMode;
    private SystemSettingCheckBoxPreference mShowAlways;
    private SystemSettingCheckBoxPreference mWakeOnNotification;
    private SeekBarPreference               mOffsetTop;
    private NumberPickerPreference          mNotificationsHeight;
    private Preference                      mNotificationColor;
    private CheckBoxPreference              mPrivacyMode;
    private SystemSettingCheckBoxPreference mExpandedView;

    private Context        mContext;
    private DialogFragment colorDialogFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.lockscreen_notifications);
        PreferenceScreen prefs = getPreferenceScreen();
        final ContentResolver cr = getActivity().getContentResolver();

        mContext = getActivity().getApplicationContext();

        mLockscreenNotifications =
                (SystemSettingCheckBoxPreference) prefs.findPreference(LOCKSCREEN_NOTIFICATIONS);
        mLockscreenNotifications.setOnPreferenceChangeListener(this);

        mExcludedAppsPref = (AppMultiSelectListPreference) prefs.findPreference(EXCLUDED_APPS);
        Set<String> excludedApps = getExcludedApps();
        if (excludedApps != null) {
            mExcludedAppsPref.setValues(excludedApps);
        }
        mExcludedAppsPref.setOnPreferenceChangeListener(this);

        mIncludedAppsPref = (AppMultiSelectListPreference) prefs.findPreference(INCLUDED_APPS);
        Set<String> includedApps = getIncludedApps();
        if (includedApps != null) {
            mIncludedAppsPref.setValues(includedApps);
        }
        mIncludedAppsPref.setOnPreferenceChangeListener(this);

        mPocketMode = (SystemSettingCheckBoxPreference) prefs.findPreference(POCKET_MODE);
        mShowAlways = (SystemSettingCheckBoxPreference) prefs.findPreference(SHOW_ALWAYS);
        mWakeOnNotification =
                (SystemSettingCheckBoxPreference) prefs.findPreference(WAKE_ON_NOTIFICATION);
        boolean hasProximitySensor =
                getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_PROXIMITY);
        if (!hasProximitySensor) {
            prefs.removePreference(mPocketMode);
            prefs.removePreference(mShowAlways);
            prefs.removePreference(mWakeOnNotification);
        }

        mOffsetTop = (SeekBarPreference) prefs.findPreference(OFFSET_TOP);
        mOffsetTop.setProgress((int) (Settings.System.getFloat(cr,
                Settings.System.LOCKSCREEN_NOTIFICATIONS_OFFSET_TOP, 0.3f) * 100));
        mOffsetTop.setTitle(
                getResources().getText(R.string.offset_top) + " " + mOffsetTop.getProgress() + "%");
        mOffsetTop.setOnPreferenceChangeListener(this);

        mNotificationsHeight = (NumberPickerPreference) prefs.findPreference(NOTIFICATIONS_HEIGHT);
        mNotificationsHeight.setValue(Settings.System.getInt(cr,
                Settings.System.LOCKSCREEN_NOTIFICATIONS_HEIGHT, 4));
        Point displaySize = new Point();
        ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                .getSize(displaySize);
        int max = Math.round((float) displaySize.y * (1f - (mOffsetTop.getProgress() / 100f)) /
                (float) mContext.getResources()
                        .getDimensionPixelSize(R.dimen.notification_row_min_height));
        mNotificationsHeight.setMinValue(1);
        mNotificationsHeight.setMaxValue(max);
        mNotificationsHeight.setOnPreferenceChangeListener(this);

        final int color = Settings.System.getInt(cr,
                Settings.System.LOCKSCREEN_NOTIFICATIONS_COLOR, 0x55555555);
        final String hexColor = String.format("#%08x", color);
        mNotificationColor = prefs.findPreference(NOTIFICATION_COLOR);
        mNotificationColor.setSummary(hexColor);

        mPrivacyMode = (CheckBoxPreference) prefs.findPreference(PRIVACY_MODE);
        mPrivacyMode.setChecked(Settings.System.getInt(cr,
                Settings.System.LOCKSCREEN_NOTIFICATIONS_PRIVACY_MODE, 0) == 1);
        mPrivacyMode.setOnPreferenceChangeListener(this);

        mExpandedView = (SystemSettingCheckBoxPreference) prefs.findPreference(EXPANDED_VIEW);
        mExpandedView.setEnabled(mLockscreenNotifications.isChecked() && !mPrivacyMode.isChecked());
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object value) {
        if (pref == mLockscreenNotifications) {
            final boolean enabled = (Boolean) value;
            Settings.System.putBoolean(getContentResolver(),
                    Settings.System.LOCKSCREEN_NOTIFICATIONS, enabled);
            mExpandedView.setEnabled(enabled && !mPrivacyMode.isChecked());
        } else if (pref == mIncludedAppsPref) {
            storeIncludedApps((HashSet<String>) value);
        } else if (pref == mExcludedAppsPref) {
            storeExcludedApps((HashSet<String>) value);
        } else if (pref == mPrivacyMode) {
            boolean privacyMode = (Boolean) value;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_PRIVACY_MODE, privacyMode ? 1 : 0);
            mExpandedView.setEnabled(!privacyMode && mLockscreenNotifications.isChecked());
        } else if (pref == mOffsetTop) {
            Settings.System.putFloat(getContentResolver(),
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_OFFSET_TOP, (Integer) value / 100f);
            mOffsetTop.setTitle(
                    getResources().getText(R.string.offset_top) + " " + (Integer) value + "%");
            Point displaySize = new Point();
            ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                    .getSize(displaySize);
            int max = Math.round((float) displaySize.y * (1f - (mOffsetTop.getProgress() / 100f)) /
                    (float) mContext.getResources()
                            .getDimensionPixelSize(R.dimen.notification_row_min_height));
            mNotificationsHeight.setMaxValue(max);
        } else if (pref == mNotificationsHeight) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_HEIGHT, (Integer) value);
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (mNotificationColor == preference) {
            showDialogInner(0);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private HashSet<String> getIncludedApps() {
        String included = Settings.System.getString(getContentResolver(),
                Settings.System.LOCKSCREEN_NOTIFICATIONS_INCLUDED_APPS);
        if (TextUtils.isEmpty(included)) {
            return null;
        }

        return new HashSet<String>(Arrays.asList(included.split("\\|")));
    }

    private void storeIncludedApps(HashSet<String> values) {
        StringBuilder builder = new StringBuilder();
        String delimiter = "";
        for (String value : values) {
            builder.append(delimiter);
            builder.append(value);
            delimiter = "|";
        }
        Settings.System.putString(getContentResolver(),
                Settings.System.LOCKSCREEN_NOTIFICATIONS_INCLUDED_APPS, builder.toString());
    }

    private HashSet<String> getExcludedApps() {
        String excluded = Settings.System.getString(getContentResolver(),
                Settings.System.LOCKSCREEN_NOTIFICATIONS_EXCLUDED_APPS);
        if (TextUtils.isEmpty(excluded)) {
            return null;
        }

        return new HashSet<String>(Arrays.asList(excluded.split("\\|")));
    }

    private void storeExcludedApps(HashSet<String> values) {
        StringBuilder builder = new StringBuilder();
        String delimiter = "";
        for (String value : values) {
            builder.append(delimiter);
            builder.append(value);
            delimiter = "|";
        }
        Settings.System.putString(getContentResolver(),
                Settings.System.LOCKSCREEN_NOTIFICATIONS_EXCLUDED_APPS, builder.toString());
    }

    private void showDialogInner(final int id) {
        if (colorDialogFragment != null) {
            colorDialogFragment.dismiss();
            colorDialogFragment = null;
        }

        colorDialogFragment = ColorPickerDialogFragment.newInstance(id,
                new ColorPickerDialogFragment.OnColorPickedListener() {
                    @Override public void onColorPicked(final int color) {
                        final String hex = ColorPickerDialogFragment.convertToARGB(color);
                        mNotificationColor.setSummary(hex);

                        final int intHex = ColorPickerDialogFragment.convertToColorInt(hex);
                        Settings.System.putInt(getContentResolver(),
                                Settings.System.LOCKSCREEN_NOTIFICATIONS_COLOR, intHex);
                        if (colorDialogFragment != null) {
                            colorDialogFragment.dismiss();
                        }
                    }
                }, 0x55555555);
        colorDialogFragment.setTargetFragment(this, 0);
        colorDialogFragment.show(getFragmentManager(), "dialog " + id);
    }

}
