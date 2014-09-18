/*
 * <!--
 *    Copyright (C) 2014 The NamelessRom Project
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * -->
 */
package com.android.settings.nameless;

import android.app.DialogFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

import com.android.settings.R;

public class ColorPickerDialogFragment extends DialogFragment {

    private final OnColorPickedListener onColorPickedListener;

    private ColorPicker colorPicker;

    private int defaultColor;

    public static ColorPickerDialogFragment newInstance(final int id,
            final OnColorPickedListener listener, final int color) {
        final ColorPickerDialogFragment frag = new ColorPickerDialogFragment(listener, color);
        final Bundle args = new Bundle();
        args.putInt("id", id);
        frag.setArguments(args);
        return frag;
    }

    private ColorPickerDialogFragment(final OnColorPickedListener listener, final int color) {
        super();
        this.onColorPickedListener = listener;
        this.defaultColor = color;
    }

    @Override public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.dialog_color_picker, container, false);

        final SaturationBar saturationBar = (SaturationBar) v.findViewById(R.id.saturation_bar);
        final ValueBar valueBar = (ValueBar) v.findViewById(R.id.value_bar);
        final OpacityBar opacityBar = (OpacityBar) v.findViewById(R.id.opacity_bar);

        colorPicker = (ColorPicker) v.findViewById(R.id.color_picker);

        colorPicker.addSaturationBar(saturationBar);
        colorPicker.addValueBar(valueBar);
        colorPicker.addOpacityBar(opacityBar);

        colorPicker.setColor(defaultColor);
        colorPicker.setOldCenterColor(defaultColor);

        final Button resetColor = (Button) v.findViewById(R.id.color_reset);
        resetColor.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                colorPicker.setColor(defaultColor);
            }
        });

        final Button pickColor = (Button) v.findViewById(R.id.color_pick);
        pickColor.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                onColorPickedListener.onColorPicked(colorPicker.getColor());
            }
        });

        if (getDialog() != null) getDialog().setTitle(R.string.color_pick);

        return v;
    }

    public static String convertToARGB(final int color) {
        String alpha = Integer.toHexString(Color.alpha(color));
        String red = Integer.toHexString(Color.red(color));
        String green = Integer.toHexString(Color.green(color));
        String blue = Integer.toHexString(Color.blue(color));

        if (alpha.length() == 1) {
            alpha = "0" + alpha;
        }

        if (red.length() == 1) {
            red = "0" + red;
        }

        if (green.length() == 1) {
            green = "0" + green;
        }

        if (blue.length() == 1) {
            blue = "0" + blue;
        }

        return "#" + alpha + red + green + blue;
    }

    public static int convertToColorInt(String argb) throws NumberFormatException {
        if (argb.startsWith("#")) {
            argb = argb.replace("#", "");
        }

        int alpha = -1, red = -1, green = -1, blue = -1;

        if (argb.length() == 8) {
            alpha = Integer.parseInt(argb.substring(0, 2), 16);
            red = Integer.parseInt(argb.substring(2, 4), 16);
            green = Integer.parseInt(argb.substring(4, 6), 16);
            blue = Integer.parseInt(argb.substring(6, 8), 16);
        } else if (argb.length() == 6) {
            alpha = 255;
            red = Integer.parseInt(argb.substring(0, 2), 16);
            green = Integer.parseInt(argb.substring(2, 4), 16);
            blue = Integer.parseInt(argb.substring(4, 6), 16);
        }

        return Color.argb(alpha, red, green, blue);
    }

    public static interface OnColorPickedListener {
        public void onColorPicked(final int color);
    }

}
