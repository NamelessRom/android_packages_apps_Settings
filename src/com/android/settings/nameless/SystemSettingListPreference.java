/*
 * Copyright (C) 2014 The NamelessROM Project
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

package com.android.settings.nameless;

import android.content.ContentResolver;
import android.content.Context;
import android.preference.ListPreference;
import android.provider.Settings;
import android.util.AttributeSet;

public class SystemSettingListPreference extends ListPreference {

    public SystemSettingListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SystemSettingListPreference(Context context) {
        super(context);
    }

    @Override
    protected boolean persistString(String value) {
        if (shouldPersist()) {
            final ContentResolver resolver = getContext().getContentResolver();
            Settings.System.putInt(resolver, getKey(), Integer.parseInt(value));
            return true;
        }
        return false;
    }

    @Override
    protected String getPersistedString(String defaultReturnValue) {
        if (!shouldPersist()) {
            return defaultReturnValue;
        }

        final String value = Settings.System.getString(getContext().getContentResolver(), getKey());

        return (value != null ? value : defaultReturnValue);
    }

    @Override
    protected boolean isPersisted() {
        // Using getString instead of getInt so we can simply check for null
        // instead of catching an exception. (All values are stored as strings.)
        return Settings.System.getString(getContext().getContentResolver(), getKey()) != null;
    }
}
