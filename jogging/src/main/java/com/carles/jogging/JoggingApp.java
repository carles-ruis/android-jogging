package com.carles.jogging;

import android.app.Application;
import android.os.StrictMode;

import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Logger;

/**
 * Created by carles1 on 19/04/14.
 */
public class JoggingApp extends Application {

    private static final String TAG = JoggingApp.class.getName();

    @Override
    public void onCreate() {
        super.onCreate();

        configStrictMode();

        // set to true to disable google analytics
        GoogleAnalytics.getInstance(this).setAppOptOut(false);
        // Set the log level. Warning by default
        GoogleAnalytics.getInstance(this).getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
    }

    private void configStrictMode() {
        if (C.DEBUG) {
            StrictMode.enableDefaults();
        }
    }

}