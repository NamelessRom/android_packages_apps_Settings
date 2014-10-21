package org.namelessrom.settings.fragments;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserManager;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Switch;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.accounts.AccountSyncSettings;
import com.android.settings.accounts.AuthenticatorHelper;
import com.android.settings.accounts.ManageAccountsSettings;
import com.android.settings.location.LocationEnabler;
import com.android.settings.profiles.ProfileEnabler;
import com.android.settings.voicewakeup.VoiceWakeupEnabler;

import org.namelessrom.settings.HeaderAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by alex on 21.10.14.
 */
public class UserSettingsFragment extends PreferenceFragment implements OnAccountsUpdateListener {
    private static final ArrayList<Preference> mPreferences = new ArrayList<Preference>();

    private static final String VOICE_WAKEUP_PKG = "com.cyanogenmod.voicewakeup";

    private static final String KEY_PROFILES = "profiles_settings";
    private static final String KEY_LOCATION = "location_settings";
    private static final String KEY_VOICE_WAKEUP = "voice_wakeup_settings";
    private static final String KEY_SECURITY = "security_settings";
    private static final String KEY_PRIVACY = "privacy_settings_cyanogenmod";
    private static final String KEY_LANGUAGE = "language_settings";
    private static final String KEY_BACKUP = "privacy_settings";
    private static final String KEY_ACCOUNTS = "account_settings";
    private static final String KEY_ACCOUNT_ADD = "account_add";

    private AuthenticatorHelper mAuthenticatorHelper;
    private boolean mListeningToAccountUpdates;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.nameless_user_settings);

        mAuthenticatorHelper = new AuthenticatorHelper();
        mAuthenticatorHelper.updateAuthDescriptions(getActivity());
        mAuthenticatorHelper.onAccountsUpdated(getActivity(), null);

        filterPreferences();
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getListView() != null) {
            //getListView().setAdapter(new UserHeaderAdapter(getActivity(), mPreferences, ));
        }
    }

    private void filterPreferences() {
        final UserManager um = (UserManager) getActivity().getSystemService(Context.USER_SERVICE);
        Preference preference;

        preference = findPreference(KEY_ACCOUNT_ADD);
        if (preference != null) {
            getPreferenceScreen().removePreference(preference);
            final PreferenceCategory accounts = (PreferenceCategory) findPreference(KEY_ACCOUNTS);
            if (accounts != null) {
                insertAccountsHeaders(accounts);
            }
            if (!um.hasUserRestriction(UserManager.DISALLOW_MODIFY_ACCOUNTS)) {
                accounts.addPreference(preference);
            }
        }

        preference = findPreference(KEY_VOICE_WAKEUP);
        if (preference != null && !Utils.isPackageInstalled(getActivity(), VOICE_WAKEUP_PKG)) {
            getPreferenceScreen().removePreference(preference);
        }
    }

    @Override public void onResume() {
        super.onResume();
        if (getListView() != null && getListView().getAdapter() instanceof HeaderAdapter) {
            ((HeaderAdapter) getListView().getAdapter()).resume();
        }
    }

    @Override public void onDestroy() {
        if (mListeningToAccountUpdates) {
            AccountManager.get(getActivity()).removeOnAccountsUpdatedListener(this);
        }
        super.onDestroy();
    }

    private void insertAccountsHeaders(final PreferenceCategory category) {
        String[] accountTypes = mAuthenticatorHelper.getEnabledAccountTypes();
        List<Preference> preferences = new ArrayList<Preference>(accountTypes.length);
        for (String accountType : accountTypes) {
            CharSequence label = mAuthenticatorHelper.getLabelForType(getActivity(), accountType);
            if (label == null) {
                continue;
            }

            Account[] accounts = AccountManager.get(getActivity()).getAccountsByType(accountType);
            boolean skipToAccount = accounts.length == 1
                    && !mAuthenticatorHelper.hasAccountPreferences(accountType);
            Preference preference = new Preference(getActivity());
            preference.setTitle(label);
            if (skipToAccount) {
                preference.setFragment(AccountSyncSettings.class.getName());
                // Need this for the icon
                preference.getExtras().putString(ManageAccountsSettings.KEY_ACCOUNT_TYPE,
                        accountType);
                preference.getExtras().putParcelable(AccountSyncSettings.ACCOUNT_KEY, accounts[0]);
            } else {
                preference.setFragment(ManageAccountsSettings.class.getName());
                preference.getExtras().putString(ManageAccountsSettings.KEY_ACCOUNT_TYPE,
                        accountType);
                if (getActivity() instanceof PreferenceActivity
                        && !((PreferenceActivity) getActivity()).isMultiPane()) {
                    preference.getExtras().putString(ManageAccountsSettings.KEY_ACCOUNT_LABEL,
                            label.toString());
                }
            }
            preferences.add(preference);
            mAuthenticatorHelper.preloadDrawableForType(getActivity(), accountType);
        }

        // Sort by label
        Collections.sort(preferences, new Comparator<Preference>() {
            @Override public int compare(Preference h1, Preference h2) {
                return h1.getTitle().toString().compareTo(h2.getTitle().toString());
            }
        });

        for (Preference preference : preferences) {
            category.addPreference(preference);
        }

        if (!mListeningToAccountUpdates) {
            AccountManager.get(getActivity()).addOnAccountsUpdatedListener(this, null, true);
            mListeningToAccountUpdates = true;
        }
    }

    @Override public void onAccountsUpdated(Account[] accounts) {
        // TODO: watch for package upgrades to invalidate cache; see 7206643
        mAuthenticatorHelper.updateAuthDescriptions(getActivity());
        mAuthenticatorHelper.onAccountsUpdated(getActivity(), accounts);
    }

    private static class UserHeaderAdapter extends HeaderAdapter {
        private final DevicePolicyManager mDevicePolicyManager;

        private final ProfileEnabler mProfileEnabler;
        private final LocationEnabler mLocationEnabler;
        private final VoiceWakeupEnabler mVoiceWakeupEnabler;

        public UserHeaderAdapter(Context context, List<Preference> objects,
                AuthenticatorHelper authenticatorHelper, DevicePolicyManager dpm) {
            super(context, objects, authenticatorHelper);

            mDevicePolicyManager = dpm;

            // Temp Switches provided as placeholder until the adapter replaces these with actual
            // Switches inflated from their layouts. Must be done before adapter is set in super
            mProfileEnabler = new ProfileEnabler(context, new Switch(context));
            mLocationEnabler = new LocationEnabler(context, new Switch(context));
            mVoiceWakeupEnabler = new VoiceWakeupEnabler(context, new Switch(context));
        }

        @Override public int getPreferenceType(final Preference preference) {
            final String key = preference.getKey();
            if (TextUtils.equals(KEY_PROFILES, key)
                    || TextUtils.equals(KEY_LOCATION, key)
                    || TextUtils.equals(KEY_VOICE_WAKEUP, key)) {
                return HEADER_TYPE_SWITCH;
            } else if (TextUtils.equals(KEY_SECURITY, key)) {
                return HEADER_TYPE_BUTTON;
            } else {
                return HEADER_TYPE_NORMAL;
            }
        }

        @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {

        }

        @Override public void setSwitch(final Preference preference,
                final HeaderViewHolder holder) {
            final String key = preference.getKey();
            if (TextUtils.equals(KEY_PROFILES, key)) {
                mProfileEnabler.setSwitch(holder.switch_);
            } else if (TextUtils.equals(KEY_LOCATION, key)) {
                mLocationEnabler.setSwitch(holder.switch_);
            } else if (TextUtils.equals(KEY_VOICE_WAKEUP, key)) {
                mVoiceWakeupEnabler.setSwitch(holder.switch_);
            }
        }

        @Override public void setButton(final Preference preference,
                final HeaderViewHolder holder) {
            final String key = preference.getKey();
            if (TextUtils.equals(KEY_SECURITY, key)) {
                boolean hasCert = DevicePolicyManager.hasAnyCaCertsInstalled();
                if (hasCert) {
                    holder.button_.setVisibility(View.VISIBLE);
                    holder.divider_.setVisibility(View.VISIBLE);
                    boolean isManaged = mDevicePolicyManager.getDeviceOwner() != null;
                    if (isManaged) {
                        holder.button_.setImageResource(R.drawable.ic_settings_about);
                    } else {
                        holder.button_.setImageResource(
                                android.R.drawable.stat_notify_error);
                    }
                    holder.button_.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(
                                    android.provider.Settings.ACTION_MONITORING_CERT_INFO);
                            getContext().startActivity(intent);
                        }
                    });
                } else {
                    holder.button_.setVisibility(View.GONE);
                    holder.divider_.setVisibility(View.GONE);
                }
            }
        }

        @Override public void resume() {
            mProfileEnabler.resume();
            mLocationEnabler.resume();
            mVoiceWakeupEnabler.resume();
        }

        @Override public void pause() {
            mProfileEnabler.pause();
            mLocationEnabler.pause();
            mVoiceWakeupEnabler.pause();
        }
    }

}
