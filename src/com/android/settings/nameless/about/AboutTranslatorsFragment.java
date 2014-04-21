package com.android.settings.nameless.about;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.nameless.widgets.DeveloperPreference;

public class AboutTranslatorsFragment extends Fragment {

    public AboutTranslatorsFragment() {
        // empty fragment constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View root =
                inflater.inflate(R.layout.fragment_about_nameless_translators, container, false);
        final ViewGroup maintainerGroup = (ViewGroup) root.findViewById(R.id.translators);

        final DeveloperPreference dummyPref =
                (DeveloperPreference) root.findViewById(R.id.dummy_translator);
        final LayoutParams lp = dummyPref.getLayoutParams();

        final Activity activity = getActivity();
        if (activity == null) return root;

        final Resources resources = activity.getResources();
        final String[] names = resources.getStringArray(R.array.translators_name);
        final String[] crowdins = resources.getStringArray(R.array.translators_crowdin);

        String name = "", crowdin = "";
        DeveloperPreference pref;
        TextView seperator;
        final int size = names.length;
        if (size == crowdins.length) {
            for (int i = 0; i < size; i++) {
                name = names[i];
                crowdin = crowdins[i];
                if (name.startsWith("---")) {
                    name = name.replace("---", "");
                    seperator = (TextView)
                            inflater.inflate(R.layout.widget_tv_seperator, null, false);
                    seperator.setText(name);
                    maintainerGroup.addView(seperator);
                } else {
                    pref = new DeveloperPreference(activity, name, crowdin);
                    pref.setLayoutParams(lp);
                    maintainerGroup.addView(pref);
                }
            }
            maintainerGroup.removeView(dummyPref);
        }

        return root;
    }

}
