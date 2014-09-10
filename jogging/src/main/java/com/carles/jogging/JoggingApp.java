package com.carles.jogging;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;

import com.carles.jogging.util.PrefUtil;

/**
 * Created by carles1 on 19/04/14.
 */
public class JoggingApp extends Application {

    private static final String TAG = JoggingApp.class.getName();

    @Override
    public void onCreate() {
        super.onCreate();

        configStrictMode();

        // TODO add issue tracker. google analytics ???

    }

    private void configStrictMode() {
        if (C.DEBUG) {
            StrictMode.enableDefaults();
        }
    }

}