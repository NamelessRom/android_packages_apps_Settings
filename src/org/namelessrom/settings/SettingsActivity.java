package org.namelessrom.settings;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.android.settings.R;

import org.namelessrom.settings.fragments.CommonSettingsFragment;
import org.namelessrom.settings.fragments.DeviceSettingsFragment;
import org.namelessrom.settings.fragments.UserSettingsFragment;

/**
 * Created by alex on 21.10.14.
 */
public class SettingsActivity extends PreferenceActivity {
    private ActionBar mActionBar;

    private ViewPager mViewPager;
    private ViewPagerAdapter mViewPagerAdapter;

    // For storing the loaded fragments
    private SparseArray<PreferenceFragment> mFragmentsCache;

    private CommonSettingsFragment mCommonSettingsFragment;
    private DeviceSettingsFragment mDeviceSettingsFragment;
    private UserSettingsFragment mUserSettingsFragment;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nameless_activity_settings);

        // Setup our viewpager
        mViewPagerAdapter = new ViewPagerAdapter(getFragmentManager());
        mViewPager = ((ViewPager) findViewById(R.id.pager));
        mViewPager.setOnPageChangeListener(new PageChangeListener());
        mViewPager.setOffscreenPageLimit(2);

        // Setup actionbar
        mActionBar = getActionBar();
        if (mActionBar != null) {
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            mActionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
            mActionBar.addTab(mActionBar.newTab()
                    .setText(R.string.header_category_general)
                    .setTabListener(new TabListener(0)));
            mActionBar.addTab(mActionBar.newTab()
                    .setText(R.string.header_category_device)
                    .setTabListener(new TabListener(1)));
            mActionBar.addTab(mActionBar.newTab()
                    .setText(R.string.header_category_personal)
                    .setTabListener(new TabListener(2)));
        }

        int index = 0;
        // restore instance state
        if (savedInstanceState != null) {
            index = savedInstanceState.getInt("tab", 0);
            final String[] savedFragments = savedInstanceState.getStringArray("fragmentid");
            mCommonSettingsFragment = ((CommonSettingsFragment) getFragmentManager()
                    .findFragmentByTag(savedFragments[0]));
            mFragmentsCache.put(0, mCommonSettingsFragment);

            mDeviceSettingsFragment = ((DeviceSettingsFragment) getFragmentManager()
                    .findFragmentByTag(savedFragments[1]));
            mFragmentsCache.put(1, mDeviceSettingsFragment);

            mUserSettingsFragment = ((UserSettingsFragment) getFragmentManager()
                    .findFragmentByTag(savedFragments[2]));
            mFragmentsCache.put(2, mUserSettingsFragment);
        }

        // set the current navigation item
        if (mActionBar != null) {
            mActionBar.setSelectedNavigationItem(index);
        }

        // set the viewpager adapter and the current item
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.setCurrentItem(index);
        mViewPagerAdapter.notifyDataSetChanged();
    }

    @Override protected void onSaveInstanceState(final Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("tab", mViewPager.getCurrentItem());
        final int id = mViewPager.getId();
        final String[] savedFragments = new String[3];
        savedFragments[0] = ("android:switcher:" + id + ":" + 0);
        savedFragments[1] = ("android:switcher:" + id + ":" + 1);
        savedFragments[2] = ("android:switcher:" + id + ":" + 2);
        savedInstanceState.putStringArray("fragmentid", savedFragments);
    }

    private class TabListener implements ActionBar.TabListener, View.OnClickListener {
        private int mIndex = 0;

        public TabListener(final int index) { mIndex = index; }

        public void onClick(View view) {
            mViewPager.setCurrentItem(mIndex);
            //updateTabIcon(mIndex);
        }

        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction transaction) {
            mViewPager.setCurrentItem(mIndex);
        }

        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction transaction) { }

        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction transaction) { }
    }

    private class PageChangeListener implements ViewPager.OnPageChangeListener {
        public PageChangeListener() { }

        public void onPageScrollStateChanged(int state) {
            //mActionBar.updateScrollState(state);
        }

        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            //mActionBar.updateAnimateTab(position, positionOffset, positionOffsetPixels);
        }

        public void onPageSelected(int position) {
            mActionBar.setSelectedNavigationItem(position);
        }
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        public ViewPagerAdapter(final FragmentManager fm) {
            super(fm);
            mFragmentsCache = new SparseArray<PreferenceFragment>();
        }

        @Override public void destroyItem(ViewGroup container, int position, Object fragment) {
            super.destroyItem(container, position, fragment);
            mFragmentsCache.put(position, (PreferenceFragment) fragment);
        }

        @Override public int getCount() {
            // we always have 3 tabs
            return 3;
        }

        @Override public Fragment getItem(final int position) {
            // Check if the fragment is in our cache and return it
            if (mFragmentsCache.get(position) != null) {
                return mFragmentsCache.get(position);
            }

            // else check the position and load it into our fragments cache
            switch (position) {
                default:
                    throw new IllegalStateException("No fragment at position " + position);
                case 0:
                    if (mCommonSettingsFragment == null) {
                        mCommonSettingsFragment = new CommonSettingsFragment();
                        mFragmentsCache.put(0, mCommonSettingsFragment);
                    }
                    return mCommonSettingsFragment;
                case 1:
                    if (mDeviceSettingsFragment == null) {
                        mDeviceSettingsFragment = new DeviceSettingsFragment();
                        mFragmentsCache.put(1, mDeviceSettingsFragment);
                    }
                    return mDeviceSettingsFragment;
                case 2:
                    if (mUserSettingsFragment == null) {
                        mUserSettingsFragment = new UserSettingsFragment();
                        mFragmentsCache.put(2, mUserSettingsFragment);
                    }
                    return mUserSettingsFragment;
            }
        }
    }
}
