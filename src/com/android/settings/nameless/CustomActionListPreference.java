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

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.preference.ListPreference;
import android.provider.Settings;
import android.util.AttributeSet;

import com.android.internal.util.nameless.ActionConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomActionListPreference extends ListPreference {

    public CustomActionListPreference(Context context) {
        super(context);
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
        final ArrayList<ActionConstants.ActionConstant> actions = new ArrayList<>();
        actions.addAll(ActionConstants.Actions());

        final String[] actionNames = ActionConstants.fromActionArray(
                actions.toArray(new ActionConstants.ActionConstant[actions.size()]));

        // save actions and the names in a list, to allow to add items dynamically
        ArrayList<String> entries = getActionEntries(context, actions);
        ArrayList<String> entryValues = new ArrayList<>(Arrays.asList(actionNames));

        // query the package manager and add suitable entries
        addCustomActions(entries, entryValues);

        setEntries(entries.toArray(new String[entries.size()]));
        setEntryValues(entryValues.toArray(new String[entryValues.size()]));

        final String value = getSecureValue("**null**");
        setValue(value);
        updateSummary(value);
    }

    private void addCustomActions(ArrayList<String> entries, ArrayList<String> entryValues) {
        final PackageManager packageManager = getContext().getPackageManager();
        final Intent intent = new Intent(Intent.ACTION_POWER_CHORD);

        // Search for all apps that can handle ACTION_POWER_CHORD
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);

        for (ResolveInfo info : activities) {
            if (!info.activityInfo.isEnabled()) {
                continue;
            }

            int perm = packageManager.checkPermission(Manifest.permission.LAUNCH_WITH_POWER_CHORD,
                    info.activityInfo.packageName);

            if (perm != PackageManager.PERMISSION_GRANTED) {
                continue;
            }

            ComponentName componentName = new ComponentName(info.activityInfo.packageName,
                    info.activityInfo.name);
            Intent targetIntent = new Intent().setComponent(componentName);

            entries.add(String.valueOf(info.loadLabel(packageManager)));
            entryValues.add(targetIntent.toUri(0));
        }
    }

    private ArrayList<String> getActionEntries(final Context context,
            final ArrayList<ActionConstants.ActionConstant> actions) {
        final ArrayList<String> entries = new ArrayList<>(actions.size());
        for (ActionConstants.ActionConstant constant : actions) {
            entries.add(ActionConstants.getProperName(context, constant.value()));
        }
        return entries;
    }

    private void updateSummary(String value) {
        final int index = findIndexOfValue(value);
        if (index != -1) {
            setValueIndex(index);
            setSummary(getEntries()[index]);
        } else {
            setValueIndex(0);
            setSummary(getEntries()[0]);
        }
    }

    public String getSecureValue(String defaultReturnValue) {
        String value = Settings.Secure.getString(getContext().getContentResolver(), getKey());
        return value != null ? value : defaultReturnValue;
    }

    public void putSecureValue(String value) {
        Settings.Secure.putString(getContext().getContentResolver(), getKey(), value);
        updateSummary(value);
    }
}
