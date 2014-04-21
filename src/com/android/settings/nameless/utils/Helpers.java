package com.android.settings.nameless.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Helper class for various stuffs
 */
public class Helpers {

    public static boolean isSecretModeEnabled() {
        return existsInBuildProp("ro.nameless.secret=1");
    }

    public static boolean existsInBuildProp(final String filter) {
        final File f = new File("/system/build.prop");
        BufferedReader bufferedReader = null;
        if (f.exists() && f.canRead()) {
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
                String s;
                while ((s = bufferedReader.readLine()) != null) {
                    if (s.contains(filter)) return true;
                }
            } catch (Exception whoops) {
                return false;
            } finally {
                try {
                    if (bufferedReader != null) bufferedReader.close();
                } catch (Exception ignored) {
                    // mepmep
                }
            }
        }
        return false;
    }

    public static boolean supportsOpenGLES20(Context context) {
        final ActivityManager am = (ActivityManager)
                context.getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo info = am.getDeviceConfigurationInfo();
        return (info != null && info.reqGlEsVersion >= 0x20000);
    }

}
