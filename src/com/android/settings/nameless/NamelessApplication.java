package com.android.settings.nameless;

import android.app.Application;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

@ReportsCrashes(
        httpMethod = HttpSender.Method.PUT,
        reportType = HttpSender.Type.JSON,
        formKey = "",
        formUri = "http://reports.nameless-rom.org/acra-settings/_design/acra-storage/_update/report",
        formUriBasicAuthLogin = "namelessreporter",
        formUriBasicAuthPassword = "weareopentoeveryone",
        mode = ReportingInteractionMode.SILENT)
public class NamelessApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);

        // Uncomment to test if ACRA is working
        // throw new NullPointerException("I hate you");
    }
}
