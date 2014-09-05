package com.carles.jogging;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;

import com.carles.jogging.common.PrefUtil;

/**
 * Created by carles1 on 19/04/14.
 */
public class JoggingApp extends Application {

    private static final String TAG = JoggingApp.class.getName();

    private SharedPreferences prefs;

    @Override
    public void onCreate() {
        super.onCreate();

        configStrictMode();

        // TODO add issue tracker. google analytics ???

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

    }

    private void configStrictMode() {
        if (C.DEBUG) {
            StrictMode.enableDefaults();
        }
    }

    public String getLastKilometersSelected() {
        return getPreference(C.PREF_LAST_KILOMETERS_SELECTED, String.class);
    }

    public void setLastKilometersSelected(String lastKilometersSelected) {
        putPreference(C.PREF_LAST_KILOMETERS_SELECTED, lastKilometersSelected);
    }

    private void putPreference(String key, Object value) {
        PrefUtil.putObject(prefs, key, value);
    }

    private <T> T getPreference(String key, Class<T> clazz) {
        return PrefUtil.getObject(prefs, key, clazz);
    }
}