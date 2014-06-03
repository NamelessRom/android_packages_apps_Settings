package com.android.settings.nameless.about;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import com.android.settings.R;

public class AboutTranslatorsFragment extends ListFragment {

    public AboutTranslatorsFragment() { /* empty fragment constructor */ }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setListAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.translators_name)));
    }
}
