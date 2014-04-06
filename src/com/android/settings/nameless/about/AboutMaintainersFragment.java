package com.android.settings.nameless.about;

import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import com.android.settings.R;
import com.android.settings.nameless.widgets.DeveloperPreference;

import java.util.Random;

public class AboutMaintainersFragment extends Fragment {

    public AboutMaintainersFragment() {
        // empty fragment constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_about_nameless_maintainers, container, false);
        final ViewGroup maintainerGroup = (ViewGroup) root.findViewById(R.id.maintainers);

        final DeveloperPreference dummyPref = (DeveloperPreference) root.findViewById(R.id.dummy_maintainer);
        final LayoutParams lp = dummyPref.getLayoutParams();

        final Activity activity = getActivity();
        final String packageName = activity.getPackageName();
        final Resources resources = activity.getResources();
        final String[] maintainers = resources.getStringArray(R.array.maintainers);

        String name = "", github = "", email = "";
        DeveloperPreference pref;
        for (final String s : maintainers) {
            name = resources.getString(
                    resources.getIdentifier("maintainers_" + s + "_name", "string", packageName));
            github = resources.getString(
                    resources.getIdentifier("maintainers_" + s + "_github", "string", packageName));
            email = resources.getString(
                    resources.getIdentifier("maintainers_" + s + "_email", "string", packageName));
            pref = new DeveloperPreference(activity, name, github, email);
            pref.setLayoutParams(lp);
            maintainerGroup.addView(pref);
        }
        maintainerGroup.removeView(dummyPref);

        final Random rng = new Random();
        int N = maintainerGroup.getChildCount();
        View removed;
        while (N > 0) {
            removed = maintainerGroup.getChildAt(rng.nextInt(N));
            maintainerGroup.removeView(removed);
            maintainerGroup.addView(removed);
            N -= 1;
        }

        return root;
    }

    private void launchActivity(String packageName, String activity)
            throws ActivityNotFoundException {
        Intent launch = new Intent();
        launch.setComponent(new ComponentName(packageName, packageName
                + activity));
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getActivity().startActivity(launch);
    }

}
