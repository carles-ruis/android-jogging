package com.carles.jogging;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.preference.PreferenceManager;

import com.carles.jogging.util.Log;
import com.carles.jogging.util.PrefUtil;

/**
 * Created by carles1 on 19/04/14.
 */
public class JoggingApp extends Application {

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
            Log.i(C.LOG_TAG, "Strict Mode on");
        } else {
            Log.i(C.LOG_TAG, "Strict Mode off");
        }
    }

    public boolean getIsStraightCircuit() {
        return getPreference(C.PREF_IS_STRAIGHT_CIRCUIT, Boolean.class);
    }

    public void setIsStraightCircuit(boolean isStraightCircuit) {
        putPreference(C.PREF_IS_STRAIGHT_CIRCUIT, isStraightCircuit);
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