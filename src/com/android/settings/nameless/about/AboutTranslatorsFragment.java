package com.android.settings.nameless.about;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import com.android.settings.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AboutTranslatorsFragment extends ListFragment {

    public AboutTranslatorsFragment() { /* empty fragment constructor */ }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final List<String> translators = Arrays.asList(
                getResources().getStringArray(R.array.translators_name));
        Collections.sort(translators, new SortIgnoreCase());
        setListAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1,
                translators));
    }

    private class SortIgnoreCase implements Comparator<String> {
        public int compare(final String s1, final String s2) {
            return s1.compareToIgnoreCase(s2);
        }
    }
}
