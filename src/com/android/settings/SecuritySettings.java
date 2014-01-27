/*
 * Copyright (c) 2012-2013 The Linux Foundation. All rights reserved.
 * Not a Contribution.
 * Copyright (C) 2007 The Android Open Source Project
 * Modifications Copyright (C) 2012-2013 CyanogenMod
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

package com.android.settings;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.security.KeyStore;
import android.telephony.MSimTelephonyManager;
import android.telephony.TelephonyManager;

import com.android.settings.R;

/**
 * Gesture lock pattern settings.
 */
public class SecuritySettings extends RestrictedSettingsFragment
        implements OnPreferenceChangeListener, DialogInterface.OnClickListener {
    static final String TAG = "SecuritySettings";

    private static final String KEY_DEVICE_ADMIN_CATEGORY = "device_admin_category";
    private static final String KEY_OWNER_INFO_SETTINGS = "owner_info_settings";
<<<<<<< HEAD
=======
    private static final String KEY_ENABLE_WIDGETS = "keyguard_enable_widgets";

    private static final int SET_OR_CHANGE_LOCK_METHOD_REQUEST = 123;
    private static final int CONFIRM_EXISTING_FOR_BIOMETRIC_WEAK_IMPROVE_REQUEST = 124;
    private static final int CONFIRM_EXISTING_FOR_BIOMETRIC_WEAK_LIVELINESS_OFF = 125;
    private static final int CONFIRM_EXISTING_FOR_TEMPORARY_INSECURE = 126;
    private static final int DLG_SHAKE_WARN = 0;
>>>>>>> bde84fe... Gesture Lockscreen [2/2]

    // Misc Settings
    private static final String KEY_SIM_LOCK = "sim_lock";
    private static final String KEY_SIM_LOCK_SETTINGS = "sim_lock_settings";
    private static final String KEY_SHOW_PASSWORD = "show_password";
    private static final String KEY_CREDENTIAL_STORAGE_TYPE = "credential_storage_type";
    private static final String KEY_RESET_CREDENTIALS = "reset_credentials";
    private static final String KEY_CREDENTIALS_INSTALL = "credentials_install";
    private static final String KEY_TOGGLE_INSTALL_APPLICATIONS = "toggle_install_applications";
    private static final String KEY_TOGGLE_VERIFY_APPLICATIONS = "toggle_verify_applications";
    private static final String KEY_CREDENTIALS_MANAGER = "credentials_management";
    private static final String KEY_NOTIFICATION_ACCESS = "manage_notification_access";
    private static final String PACKAGE_MIME_TYPE = "application/vnd.android.package-archive";

<<<<<<< HEAD
    // CyanogenMod Additions
    private static final String KEY_APP_SECURITY_CATEGORY = "app_security";
    private static final String KEY_SMS_SECURITY_CHECK_PREF = "sms_security_check_limit";
=======
    // Mahdi-Rom Additions
    private static final String CATEGORY_ADDITIONAL = "additional_options";
    private static final String KEY_VISIBLE_ERROR_PATTERN = "visible_error_pattern";
    private static final String KEY_VISIBLE_DOTS = "visibledots";
    private static final String LOCK_BEFORE_UNLOCK = "lock_before_unlock";
    private static final String LOCK_NUMPAD_RANDOM = "lock_numpad_random";
    private static final String KEY_SHAKE_TO_SECURE = "shake_to_secure";
    private static final String KEY_SHAKE_AUTO_TIMEOUT = "shake_auto_timeout";
    private static final String LOCKSCREEN_QUICK_UNLOCK_CONTROL = "quick_unlock_control";
    private static final String SLIDE_LOCK_TIMEOUT_DELAY = "slide_lock_timeout_delay";
    private static final String SLIDE_LOCK_SCREENOFF_DELAY = "slide_lock_screenoff_delay";
    private static final String KEY_VISIBLE_GESTURE = "visiblegesture";
>>>>>>> bde84fe... Gesture Lockscreen [2/2]

    private PackageManager mPM;
    private DevicePolicyManager mDPM;

    private CheckBoxPreference mShowPassword;

    private KeyStore mKeyStore;
    private Preference mResetCredentials;

    private CheckBoxPreference mToggleAppInstallation;
    private DialogInterface mWarnInstallApps;
    private CheckBoxPreference mToggleVerifyApps;

<<<<<<< HEAD
=======
    private boolean mIsPrimary;

    // Mahdi-Rom Additions
    private PreferenceGroup mSecurityCategory;
    private CheckBoxPreference mVisibleErrorPattern;
    private CheckBoxPreference mVisibleDots;
    private CheckBoxPreference mLockBeforeUnlock;
    private ListPreference mLockNumpadRandom;
    private CheckBoxPreference mShakeToSecure;
    private ListPreference mShakeTimer;
    private CheckBoxPreference mQuickUnlockScreen;
    private ListPreference mSlideLockTimeoutDelay;
    private ListPreference mSlideLockScreenOffDelay;
    private CheckBoxPreference mVisibleGesture;
>>>>>>> bde84fe... Gesture Lockscreen [2/2]

    // CyanogenMod Additions
    private ListPreference mSmsSecurityCheck;
    public SecuritySettings() {
        super(null /* Don't ask for restrictions pin on creation. */);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPM = getPackageManager();
        mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
    }

    private PreferenceScreen createPreferenceHierarchy() {
        PreferenceScreen root = getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        addPreferencesFromResource(R.xml.security_settings);
        root = getPreferenceScreen();

<<<<<<< HEAD
=======
        final ContentResolver resolver = getContentResolver();
        final Resources res = getResources();

        // Add package manager to check if features are available
        PackageManager pm = getPackageManager();

        int resid = 0;
        if (!mLockPatternUtils.isSecure()) {
            // if there are multiple users, disable "None" setting
            UserManager mUm = (UserManager) getSystemService(Context.USER_SERVICE);
            List<UserInfo> users = mUm.getUsers(true);
            final boolean singleUser = users.size() == 1;

            if (singleUser && mLockPatternUtils.isLockScreenDisabled()) {
                resid = R.xml.security_settings_lockscreen;
            } else {
                resid = R.xml.security_settings_chooser;
            }
        } else if (mLockPatternUtils.usingBiometricWeak() &&
                mLockPatternUtils.isBiometricWeakInstalled()) {
            resid = R.xml.security_settings_biometric_weak;
        } else {
            switch (mLockPatternUtils.getKeyguardStoredPasswordQuality()) {
                case DevicePolicyManager.PASSWORD_QUALITY_SOMETHING:
                    resid = R.xml.security_settings_pattern;
                    break;
                case DevicePolicyManager.PASSWORD_QUALITY_NUMERIC:
                    resid = R.xml.security_settings_pin;
                    break;
                case DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC:
                case DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC:
                case DevicePolicyManager.PASSWORD_QUALITY_COMPLEX:
                    resid = R.xml.security_settings_password;
                    break;
                case DevicePolicyManager.PASSWORD_QUALITY_GESTURE_WEAK:
                    resid = R.xml.security_settings_gesture;
                    break;
            }
        }
        addPreferencesFromResource(resid);

>>>>>>> bde84fe... Gesture Lockscreen [2/2]
        // Add options for device encryption
        final boolean mIsPrimary = UserHandle.myUserId() == UserHandle.USER_OWNER;

        if (!mIsPrimary) {
            // Rename owner info settings
            Preference ownerInfoPref = findPreference(KEY_OWNER_INFO_SETTINGS);
            if (ownerInfoPref != null) {
                if (UserManager.get(getActivity()).isLinkedUser()) {
                    ownerInfoPref.setTitle(R.string.profile_info_settings_title);
                } else {
                    ownerInfoPref.setTitle(R.string.user_info_settings_title);
                }
            }
        }

<<<<<<< HEAD
=======
        // lock after preference
        mLockAfter = (ListPreference) root.findPreference(KEY_LOCK_AFTER_TIMEOUT);
        if (mLockAfter != null) {
            setupLockAfterPreference();
            updateLockAfterPreferenceSummary();
        } else if (!mLockPatternUtils.isLockScreenDisabled()) {
            addPreferencesFromResource(R.xml.security_settings_slide_delay_mahdi);

            mSlideLockTimeoutDelay = (ListPreference) root
                    .findPreference(SLIDE_LOCK_TIMEOUT_DELAY);
            int slideTimeoutDelay = Settings.System.getInt(resolver,
                    Settings.System.SCREEN_LOCK_SLIDE_TIMEOUT_DELAY, 5000);
            mSlideLockTimeoutDelay.setValue(String.valueOf(slideTimeoutDelay));
            updateSlideAfterTimeoutSummary();
            mSlideLockTimeoutDelay.setOnPreferenceChangeListener(this);

            mSlideLockScreenOffDelay = (ListPreference) root
                    .findPreference(SLIDE_LOCK_SCREENOFF_DELAY);
            int slideScreenOffDelay = Settings.System.getInt(resolver,
                    Settings.System.SCREEN_LOCK_SLIDE_SCREENOFF_DELAY, 0);
            mSlideLockScreenOffDelay.setValue(String.valueOf(slideScreenOffDelay));
            updateSlideAfterScreenOffSummary();
                mSlideLockScreenOffDelay.setOnPreferenceChangeListener(this);
        }

>>>>>>> bde84fe... Gesture Lockscreen [2/2]
        if (mIsPrimary) {
            switch (mDPM.getStorageEncryptionStatus()) {
            case DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE:
                // The device is currently encrypted.
                addPreferencesFromResource(R.xml.security_settings_encrypted);
                break;
            case DevicePolicyManager.ENCRYPTION_STATUS_INACTIVE:
                // This device supports encryption but isn't encrypted.
                addPreferencesFromResource(R.xml.security_settings_unencrypted);
                break;
            }
        }

<<<<<<< HEAD
        mSmsSecurityCheck = (ListPreference) root.findPreference(KEY_SMS_SECURITY_CHECK_PREF);
        // Determine options based on device telephony support
        if (mPM.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
            mSmsSecurityCheck.setOnPreferenceChangeListener(this);
            int smsSecurityCheck = Integer.valueOf(mSmsSecurityCheck.getValue());
            updateSmsSecuritySummary(smsSecurityCheck);
        } else {
            // No telephony, remove dependent options
            PreferenceGroup appCategory = (PreferenceGroup)
                    root.findPreference(KEY_APP_SECURITY_CATEGORY);
            appCategory.removePreference(mSmsSecurityCheck);
            root.removePreference(appCategory);
        }

        if (MSimTelephonyManager.getDefault().isMultiSimEnabled()) {
            MSimTelephonyManager tm = MSimTelephonyManager.getDefault();
            int numPhones = MSimTelephonyManager.getDefault().getPhoneCount();
            boolean disableLock = true;
            boolean removeLock = true;
            for (int i = 0; i < numPhones; i++) {
                // Do not display SIM lock for devices without an Icc card
                if (tm.hasIccCard(i)) {
                    // Disable SIM lock if sim card is missing or unknown
                    removeLock = false;
                    if (!((tm.getSimState(i) == TelephonyManager.SIM_STATE_ABSENT)
                            || (tm.getSimState(i) == TelephonyManager.SIM_STATE_UNKNOWN)
                            || (tm.getSimState(i) == TelephonyManager.SIM_STATE_CARD_IO_ERROR))) {
                        disableLock = false;
                    }
                }
=======
        // biometric weak liveliness
        mBiometricWeakLiveliness =
                (CheckBoxPreference) root.findPreference(KEY_BIOMETRIC_WEAK_LIVELINESS);

        // visible pattern
        mVisiblePattern = (CheckBoxPreference) root.findPreference(KEY_VISIBLE_PATTERN);

         // visible error pattern
        mVisibleErrorPattern = (CheckBoxPreference) root.findPreference(KEY_VISIBLE_ERROR_PATTERN);

        // visible dots
        mVisibleDots = (CheckBoxPreference) root.findPreference(KEY_VISIBLE_DOTS);

        // visible gesture
        mVisibleGesture = (CheckBoxPreference) root.findPreference(KEY_VISIBLE_GESTURE);

        // lock instantly on power key press
        mPowerButtonInstantlyLocks = (CheckBoxPreference) root.findPreference(
                KEY_POWER_INSTANTLY_LOCKS);
        checkPowerInstantLockDependency();

        mSecurityCategory = (PreferenceGroup)
                root.findPreference(KEY_SECURITY_CATEGORY);

        // don't display visible pattern if biometric and backup is not pattern
        if (resid == R.xml.security_settings_biometric_weak &&
                mLockPatternUtils.getKeyguardStoredPasswordQuality() !=
                DevicePolicyManager.PASSWORD_QUALITY_SOMETHING) {
            if (mSecurityCategory != null && mVisiblePattern != null &&
                    mVisibleErrorPattern != null && mVisibleDots != null && mVisibleGesture != null) {
                mSecurityCategory.removePreference(root.findPreference(KEY_VISIBLE_PATTERN));
                mSecurityCategory.removePreference(root.findPreference(KEY_VISIBLE_ERROR_PATTERN));
                mSecurityCategory.removePreference(root.findPreference(KEY_VISIBLE_DOTS));
                mSecurityCategory.removePreference(root.findPreference(KEY_VISIBLE_GESTURE));
            }
        }
        
	// Quick Unlock Screen Control
        mQuickUnlockScreen = (CheckBoxPreference) root
                .findPreference(LOCKSCREEN_QUICK_UNLOCK_CONTROL);
        if (mQuickUnlockScreen != null) {
            mQuickUnlockScreen.setChecked(Settings.System.getInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_QUICK_UNLOCK_CONTROL, 0) == 1);
        }

        // Lock Numpad Random
        mLockNumpadRandom = (ListPreference) root.findPreference(LOCK_NUMPAD_RANDOM);
        if (mLockNumpadRandom != null) {
            mLockNumpadRandom.setValue(String.valueOf(
                    Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.LOCK_NUMPAD_RANDOM, 0)));
            mLockNumpadRandom.setSummary(mLockNumpadRandom.getEntry());
            mLockNumpadRandom.setOnPreferenceChangeListener(this);
        }

        // Shake to secure
        // Don't show if device admin requires security
        boolean shakeEnabled = mLockPatternUtils.getRequestedMinimumPasswordLength()
                == DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED;
        mShakeToSecure = (CheckBoxPreference) root
                .findPreference(KEY_SHAKE_TO_SECURE);
        if (mShakeToSecure != null) {
            mShakeToSecure.setChecked(
                    Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.LOCK_SHAKE_TEMP_SECURE, 0) == 1);
            mShakeToSecure.setOnPreferenceChangeListener(this);
            if (!shakeEnabled) {
                mSecurityCategory.removePreference(mShakeToSecure);
>>>>>>> bde84fe... Gesture Lockscreen [2/2]
            }

            if (removeLock) {
                root.removePreference(root.findPreference(KEY_SIM_LOCK));
            } else {
                if (disableLock) {
                    root.findPreference(KEY_SIM_LOCK).setEnabled(false);
                }
            }
        } else {
            // Do not display SIM lock for devices without an Icc card
            TelephonyManager tm = TelephonyManager.getDefault();
            if (!mIsPrimary || !tm.hasIccCard()) {
                root.removePreference(root.findPreference(KEY_SIM_LOCK));
            } else {
                // Disable SIM lock if sim card is missing or unknown
                if ((TelephonyManager.getDefault().getSimState() ==
                                 TelephonyManager.SIM_STATE_ABSENT) ||
                        (TelephonyManager.getDefault().getSimState() ==
                                 TelephonyManager.SIM_STATE_UNKNOWN)) {
                    root.findPreference(KEY_SIM_LOCK).setEnabled(false);
                }
            }
        }

        // Show password
        mShowPassword = (CheckBoxPreference) root.findPreference(KEY_SHOW_PASSWORD);
        mResetCredentials = root.findPreference(KEY_RESET_CREDENTIALS);

        if (root.findPreference(KEY_SIM_LOCK) != null) {
            // SIM/RUIM lock
            Preference iccLock = (Preference) root.findPreference(KEY_SIM_LOCK_SETTINGS);
 
            Intent intent = new Intent();
            if (MSimTelephonyManager.getDefault().isMultiSimEnabled()) {
                intent.setClassName("com.android.settings",
                        "com.android.settings.SelectSubscription");
                intent.putExtra(SelectSubscription.PACKAGE, "com.android.settings");
                intent.putExtra(SelectSubscription.TARGET_CLASS,
                        "com.android.settings.IccLockSettings");
            } else {
                intent.setClassName("com.android.settings", "com.android.settings.IccLockSettings");
            }
            iccLock.setIntent(intent);
        }

        // Credential storage
        final UserManager um = (UserManager) getActivity().getSystemService(Context.USER_SERVICE);
        mKeyStore = KeyStore.getInstance(); // needs to be initialized for onResume()
        if (!um.hasUserRestriction(UserManager.DISALLOW_CONFIG_CREDENTIALS)) {
            Preference credentialStorageType = root.findPreference(KEY_CREDENTIAL_STORAGE_TYPE);

            final int storageSummaryRes =
                mKeyStore.isHardwareBacked() ? R.string.credential_storage_type_hardware
                        : R.string.credential_storage_type_software;
            credentialStorageType.setSummary(storageSummaryRes);

        } else {
            removePreference(KEY_CREDENTIALS_MANAGER);
        }

        // Application install
        PreferenceGroup deviceAdminCategory = (PreferenceGroup)
                root.findPreference(KEY_DEVICE_ADMIN_CATEGORY);
        mToggleAppInstallation = (CheckBoxPreference) findPreference(
                KEY_TOGGLE_INSTALL_APPLICATIONS);
        mToggleAppInstallation.setChecked(isNonMarketAppsAllowed());

        // Side loading of apps.
        mToggleAppInstallation.setEnabled(mIsPrimary);

        // Package verification, only visible to primary user and if enabled
        mToggleVerifyApps = (CheckBoxPreference) findPreference(KEY_TOGGLE_VERIFY_APPLICATIONS);
        if (mIsPrimary && showVerifierSetting()) {
            if (isVerifierInstalled()) {
                mToggleVerifyApps.setChecked(isVerifyAppsEnabled());
            } else {
                mToggleVerifyApps.setChecked(false);
                mToggleVerifyApps.setEnabled(false);
            }
        } else {
            if (deviceAdminCategory != null) {
                deviceAdminCategory.removePreference(mToggleVerifyApps);
            } else {
                mToggleVerifyApps.setEnabled(false);
            }
        }

        final Preference mNotificationAccess = findPreference(KEY_NOTIFICATION_ACCESS);
        if (mNotificationAccess != null) {
            final int total = NotificationAccessSettings.getListenersCount(mPM);
            if (total == 0) {
                if (deviceAdminCategory != null) {
                    deviceAdminCategory.removePreference(mNotificationAccess);
                }
            } else {
                final int n = getNumEnabledNotificationListeners();
                if (n == 0) {
                    mNotificationAccess.setSummary(getResources().getString(
                            R.string.manage_notification_access_summary_zero));
                } else {
                    mNotificationAccess.setSummary(String.format(getResources().getQuantityString(
                            R.plurals.manage_notification_access_summary_nonzero,
                            n, n)));
                }
            }
        }

        if (shouldBePinProtected(RESTRICTIONS_PIN_SET)) {
            protectByRestrictions(mToggleAppInstallation);
            protectByRestrictions(mToggleVerifyApps);
            protectByRestrictions(mResetCredentials);
            protectByRestrictions(root.findPreference(KEY_CREDENTIALS_INSTALL));
        }
        return root;
    }

    private int getNumEnabledNotificationListeners() {
        final String flat = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ENABLED_NOTIFICATION_LISTENERS);
        if (flat == null || "".equals(flat)) return 0;
        final String[] components = flat.split(":");
        return components.length;
    }

    private boolean isNonMarketAppsAllowed() {
        return Settings.Global.getInt(getContentResolver(),
                                      Settings.Global.INSTALL_NON_MARKET_APPS, 0) > 0;
    }

    private void setNonMarketAppsAllowed(boolean enabled) {
        final UserManager um = (UserManager) getSystemService(Context.USER_SERVICE);
        if (um.hasUserRestriction(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES)) {
            return;
        }
        // Change the system setting
        Settings.Global.putInt(getContentResolver(),
                Settings.Global.INSTALL_NON_MARKET_APPS, enabled ? 1 : 0);
    }

    private boolean isVerifyAppsEnabled() {
        return Settings.Global.getInt(getContentResolver(),
                Settings.Global.PACKAGE_VERIFIER_ENABLE, 1) > 0;
    }

    private boolean isVerifierInstalled() {
        final Intent verification = new Intent(Intent.ACTION_PACKAGE_NEEDS_VERIFICATION);
        verification.setType(PACKAGE_MIME_TYPE);
        verification.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return (mPM.queryBroadcastReceivers(verification, 0).size() > 0);
    }

    private boolean showVerifierSetting() {
        return Settings.Global.getInt(getContentResolver(),
                Settings.Global.PACKAGE_VERIFIER_SETTING_VISIBLE, 1) > 0;
    }

    private void warnAppInstallation() {
        // TODO: DialogFragment?
        mWarnInstallApps = new AlertDialog.Builder(getActivity()).setTitle(
                getResources().getString(R.string.error_title))
                .setIcon(com.android.internal.R.drawable.ic_dialog_alert)
                .setMessage(getResources().getString(R.string.install_all_warning))
                .setPositiveButton(android.R.string.yes, this)
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (dialog == mWarnInstallApps && which == DialogInterface.BUTTON_POSITIVE) {
            setNonMarketAppsAllowed(true);
            if (mToggleAppInstallation != null) {
                mToggleAppInstallation.setChecked(true);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mWarnInstallApps != null) {
            mWarnInstallApps.dismiss();
        }
    }

    private void updateSmsSecuritySummary(int selection) {
        final String message = selection > 0
                ? getString(R.string.sms_security_check_limit_summary, selection)
                : getString(R.string.sms_security_check_limit_summary_none);
        mSmsSecurityCheck.setSummary(message);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Make sure we reload the preference hierarchy since some of these settings
        // depend on others...
        createPreferenceHierarchy();

<<<<<<< HEAD
=======
        final LockPatternUtils lockPatternUtils = mChooseLockSettingsHelper.utils();
        if (mBiometricWeakLiveliness != null) {
            mBiometricWeakLiveliness.setChecked(
                    lockPatternUtils.isBiometricWeakLivelinessEnabled());
        }
        if (mVisiblePattern != null) {
            mVisiblePattern.setChecked(lockPatternUtils.isVisiblePatternEnabled());
        }
        if (mVisibleErrorPattern != null) {
            mVisibleErrorPattern.setChecked(lockPatternUtils.isShowErrorPath());
        }
        if (mVisibleDots != null) {
            mVisibleDots.setChecked(lockPatternUtils.isVisibleDotsEnabled());
        }
        if (mVisibleGesture != null) {
            mVisibleGesture.setChecked(lockPatternUtils.isVisibleGestureEnabled());
        }
        if (mPowerButtonInstantlyLocks != null) {
            mPowerButtonInstantlyLocks.setChecked(lockPatternUtils.getPowerButtonInstantlyLocks());
        }
>>>>>>> bde84fe... Gesture Lockscreen [2/2]
        if (mShowPassword != null) {
            mShowPassword.setChecked(Settings.System.getInt(getContentResolver(),
                    Settings.System.TEXT_SHOW_PASSWORD, 1) != 0);
        }
        if (mResetCredentials != null) {
            mResetCredentials.setEnabled(!mKeyStore.isEmpty());
        }
<<<<<<< HEAD
=======
        if (mEnableKeyguardWidgets != null) {
            if (!lockPatternUtils.getWidgetsEnabled()) {
                mEnableKeyguardWidgets.setSummary(R.string.disabled);
            } else {
                mEnableKeyguardWidgets.setSummary(R.string.enabled);
            }
        }
>>>>>>> bde84fe... Gesture Lockscreen [2/2]
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (ensurePinRestrictedPreference(preference)) {
            return true;
        }
        final String key = preference.getKey();

<<<<<<< HEAD
        if (preference == mShowPassword) {
=======
        final LockPatternUtils lockPatternUtils = mChooseLockSettingsHelper.utils();
        if (KEY_UNLOCK_SET_OR_CHANGE.equals(key)) {
            startFragment(this, "com.android.settings.ChooseLockGeneric$ChooseLockGenericFragment",
                    SET_OR_CHANGE_LOCK_METHOD_REQUEST, null);
        } else if (KEY_BIOMETRIC_WEAK_IMPROVE_MATCHING.equals(key)) {
            ChooseLockSettingsHelper helper =
                    new ChooseLockSettingsHelper(this.getActivity(), this);
            if (!helper.launchConfirmationActivity(
                    CONFIRM_EXISTING_FOR_BIOMETRIC_WEAK_IMPROVE_REQUEST, null, null)) {
                // If this returns false, it means no password confirmation is required, so
                // go ahead and start improve.
                // Note: currently a backup is required for biometric_weak so this code path
                // can't be reached, but is here in case things change in the future
                startBiometricWeakImprove();
            }
        } else if (KEY_BIOMETRIC_WEAK_LIVELINESS.equals(key)) {
            if (isToggled(preference)) {
                lockPatternUtils.setBiometricWeakLivelinessEnabled(true);
            } else {
                // In this case the user has just unchecked the checkbox, but this action requires
                // them to confirm their password.  We need to re-check the checkbox until
                // they've confirmed their password
                mBiometricWeakLiveliness.setChecked(true);
                ChooseLockSettingsHelper helper =
                        new ChooseLockSettingsHelper(this.getActivity(), this);
                if (!helper.launchConfirmationActivity(
                                CONFIRM_EXISTING_FOR_BIOMETRIC_WEAK_LIVELINESS_OFF, null, null)) {
                    // If this returns false, it means no password confirmation is required, so
                    // go ahead and uncheck it here.
                    // Note: currently a backup is required for biometric_weak so this code path
                    // can't be reached, but is here in case things change in the future
                    lockPatternUtils.setBiometricWeakLivelinessEnabled(false);
                    mBiometricWeakLiveliness.setChecked(false);
                }
            }
        } else if (KEY_LOCK_ENABLED.equals(key)) {
            lockPatternUtils.setLockPatternEnabled(isToggled(preference));
        } else if (KEY_VISIBLE_PATTERN.equals(key)) {
            lockPatternUtils.setVisiblePatternEnabled(isToggled(preference));
        } else if (KEY_VISIBLE_ERROR_PATTERN.equals(key)) {
            lockPatternUtils.setShowErrorPath(isToggled(preference));
        } else if (KEY_VISIBLE_DOTS.equals(key)) {
            lockPatternUtils.setVisibleDotsEnabled(isToggled(preference));
        } else if (KEY_VISIBLE_GESTURE.equals(key)) {
            lockPatternUtils.setVisibleGestureEnabled(isToggled(preference));
        } else if (KEY_POWER_INSTANTLY_LOCKS.equals(key)) {
            lockPatternUtils.setPowerButtonInstantlyLocks(isToggled(preference));
        } else if (preference == mShowPassword) {
>>>>>>> bde84fe... Gesture Lockscreen [2/2]
            Settings.System.putInt(getContentResolver(), Settings.System.TEXT_SHOW_PASSWORD,
                    mShowPassword.isChecked() ? 1 : 0);
        } else if (preference == mToggleAppInstallation) {
            if (mToggleAppInstallation.isChecked()) {
                mToggleAppInstallation.setChecked(false);
                warnAppInstallation();
            } else {
                setNonMarketAppsAllowed(false);
            }
        } else if (KEY_TOGGLE_VERIFY_APPLICATIONS.equals(key)) {
            Settings.Global.putInt(getContentResolver(), Settings.Global.PACKAGE_VERIFIER_ENABLE,
                    mToggleVerifyApps.isChecked() ? 1 : 0);
        } else {
            // If we didn't handle it, let preferences handle it.
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        if (preference == mSmsSecurityCheck) {
            int smsSecurityCheck = Integer.valueOf((String) value);
            Settings.Global.putInt(getContentResolver(),
                    Settings.Global.SMS_OUTGOING_CHECK_MAX_COUNT, smsSecurityCheck);
            updateSmsSecuritySummary(smsSecurityCheck);
        }
        return true;
    }

    @Override
    protected int getHelpResource() {
        return R.string.help_url_security;
    }
}
