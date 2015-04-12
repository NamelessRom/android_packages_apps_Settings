package com.android.settings.cyanogenmod;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.android.settings.R;

public class SeekBarPreference extends DialogPreference implements OnSeekBarChangeListener {
    private static final String ANDROID = "http://schemas.android.com/apk/res/android";
    private static final String SETTINGS = "http://schemas.android.com/apk/res/com.android.settings";
    private static final int DEFAULT_VALUE = 50;

    private int mMaxValue      = 100;
    private int mMinValue      = 0;
    private int mInterval      = 1;
    private int mCurrentValue;
    private String mUnitsLeft  = "";
    private String mUnitsRight = "";
    private SeekBar mSeekBar;
    private TextView mStatusText;

    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setValuesFromXml(attrs);
    }

    private void setValuesFromXml(AttributeSet attrs) {
        mMaxValue = attrs.getAttributeIntValue(ANDROID, "max", 100);
        mMinValue = attrs.getAttributeIntValue(SETTINGS, "minSeekBar", 0);
        mUnitsLeft = getAttributeStringValue(attrs, SETTINGS, "unitsLeft", "");
        String units = getAttributeStringValue(attrs, SETTINGS, "units", "");
        mUnitsRight = getAttributeStringValue(attrs, SETTINGS, "unitsRight", units);

        try {
            String newInterval = attrs.getAttributeValue(SETTINGS, "interval");
            if (newInterval != null)
                mInterval = Integer.parseInt(newInterval);
        } catch (Exception e) {
        }
    }

    private String getAttributeStringValue(AttributeSet attrs, String namespace, String name, String defaultValue) {
        String value = attrs.getAttributeValue(namespace, name);
        if (value == null)
            value = defaultValue;

        return value;
    }

    @Override
    protected View onCreateDialogView() {
        LayoutInflater mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = mInflater.inflate(R.layout.seek_bar_preference, null);

        mStatusText = (TextView) view.findViewById(R.id.seekBarPrefValue);
        mStatusText.setText(mUnitsLeft + mCurrentValue + mUnitsRight);

        mSeekBar = (SeekBar) view.findViewById(R.id.seekBarPrefBar);
        mSeekBar.setMax(mMaxValue - mMinValue);
        mSeekBar.setProgress(mCurrentValue - mMinValue);
        mSeekBar.setOnSeekBarChangeListener(this);

        return view;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            mCurrentValue = getValue(mSeekBar.getProgress());
            callChangeListener(mCurrentValue);
            setSummary(mUnitsLeft + mCurrentValue + mUnitsRight);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mStatusText.setText(mUnitsLeft + getValue(progress) + mUnitsRight);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    private int getValue(int value) {
        int newValue = value + mMinValue;
        if (newValue > mMaxValue) {
            newValue = mMaxValue;
        } else if (newValue < mMinValue) {
            newValue = mMinValue;
        } else if (mInterval != 1 && newValue % mInterval != 0) {
            newValue = Math.round(((float) newValue) / mInterval) * mInterval;
        }

        return newValue;
    }

    public void setValue(int value) {
        mCurrentValue = value;
        setSummary(mUnitsLeft + mCurrentValue + mUnitsRight);
    }
}
