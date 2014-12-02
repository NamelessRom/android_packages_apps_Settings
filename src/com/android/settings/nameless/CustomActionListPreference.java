package com.android.settings.nameless;

import android.content.Context;
import android.preference.ListPreference;
import android.provider.Settings;
import android.util.AttributeSet;

import com.android.internal.util.nameless.ActionConstants;

import java.util.ArrayList;

/**
 * Created by alex on 02.12.14.
 */
public class CustomActionListPreference extends ListPreference {


    public CustomActionListPreference(Context context) {
        this(context, null);
    }

    public CustomActionListPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomActionListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(final Context context) {
        // TODO: remove filter once everything is implemented
        //final ActionConstants.ActionConstant[] actions = ActionConstants.ActionConstant.values();
        final ActionConstants.ActionConstant[] actions = new ActionConstants.ActionConstant[]{
                ActionConstants.ActionConstant.ACTION_NULL,
                ActionConstants.ActionConstant.ACTION_SCREENSHOT};
        setEntries(getActionEntries(context, actions));
        setEntryValues(ActionConstants.fromActionArray(actions));
    }

    private String[] getActionEntries(final Context context,
                                      final ActionConstants.ActionConstant[] actions) {
        final ArrayList<String> entries = new ArrayList<String>(actions.length);
        for (ActionConstants.ActionConstant constant : actions) {
            entries.add(ActionConstants.getProperName(context, constant.value()));
        }
        return entries.toArray(new String[entries.size()]);
    }

    @Override
    protected boolean isPersisted() {
        // Using getString instead of getInt so we can simply check for null
        // instead of catching an exception. (All values are stored as strings.)
        return Settings.System.getString(getContext().getContentResolver(), getKey()) != null;
    }

    @Override
    protected boolean persistString(String value) {
        if (shouldPersist()) {
            if (value == getPersistedString(value)) {
                // It's already there, so the same as persisting
                return true;
            }
            Settings.System.putString(getContext().getContentResolver(), getKey(), value);
            return true;
        }
        return false;
    }

    @Override
    protected String getPersistedString(String defaultReturnValue) {
        if (!shouldPersist()) {
            return defaultReturnValue;
        }
        return Settings.System.getString(getContext().getContentResolver(), getKey());
    }
}
