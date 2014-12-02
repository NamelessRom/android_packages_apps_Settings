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
import android.preference.ListPreference;
import android.provider.Settings;
import android.util.AttributeSet;

import com.android.internal.util.nameless.ActionConstants;

import java.util.ArrayList;

public class CustomActionListPreference extends ListPreference {

    public CustomActionListPreference(Context context) {
        super(context, null);
        init(context);
    }

    public CustomActionListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomActionListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(final Context context) {
        // TODO: remove filter once everything is implemented
        final ArrayList<ActionConstants.ActionConstant> actions =
                new ArrayList<>(ActionConstants.Actions());
        setEntries(getActionEntries(context, actions));
        setEntryValues(ActionConstants.fromActionArray(
                actions.toArray(new ActionConstants.ActionConstant[actions.size()])));

        final String value = getSystemValue("**null**");
        setValue(value);
        updateSummary(value);
    }

    private String[] getActionEntries(final Context context,
            final ArrayList<ActionConstants.ActionConstant> actions) {
        final ArrayList<String> entries = new ArrayList<>(actions.size());
        for (ActionConstants.ActionConstant constant : actions) {
            entries.add(ActionConstants.getProperName(context, constant.value()));
        }
        return entries.toArray(new String[entries.size()]);
    }

    private void updateSummary(String value) {
        final int index = findIndexOfValue(value);
        if (index != -1) {
            setValueIndex(index);
            setSummary(getEntries()[index]);
        }
    }

    public String getSystemValue(String defaultReturnValue) {
        final String value = Settings.System.getString(getContext().getContentResolver(), getKey());
        return (value != null ? value : defaultReturnValue);
    }

    public void putSystemValue(String value) {
        Settings.System.putString(getContext().getContentResolver(), getKey(), value);
        updateSummary(value);
    }
}
