/*
 * Copyright (C) 2015 The NamelessRom project
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
import android.os.Bundle;
import android.preference.PreferenceScreen;
import android.provider.SearchIndexableResource;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import java.util.ArrayList;
import java.util.List;

public class GestureSettings extends SettingsPreferenceFragment implements Indexable {
    private static final String KEY_DEVICE_SPECIFIC_GESTURES =
            "device_specific_gesture_settings";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.nameless_gesture_prefs);

        final PreferenceScreen deviceSpecificGestures =
                (PreferenceScreen) findPreference(KEY_DEVICE_SPECIFIC_GESTURES);
        Utils.updatePreferenceToSpecificActivityFromMetaDataOrRemove(getActivity(),
                deviceSpecificGestures, KEY_DEVICE_SPECIFIC_GESTURES);
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result = new ArrayList<>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.nameless_gesture_prefs;
                    result.add(sir);

                    return result;
                }
            };
}

