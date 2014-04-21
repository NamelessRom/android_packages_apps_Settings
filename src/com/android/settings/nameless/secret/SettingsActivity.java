package com.android.settings.nameless.secret;

import android.app.Activity;
import android.os.Bundle;

import com.android.settings.R;

/**
 * Created by alex on 22.04.14.
 */
public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nameless_settings);

        getFragmentManager().beginTransaction()
                .replace(R.id.container, new SettingsPreferenceFragment())
                .commit();
    }
}
