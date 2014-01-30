package com.android.settings.nameless.eggs;

import android.app.Activity;
import android.os.Bundle;

public class EasterEgg extends Activity {

    private EasterEggView mView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mView = new EasterEggView(this);
        setContentView(mView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mView.pause();
    }

}
