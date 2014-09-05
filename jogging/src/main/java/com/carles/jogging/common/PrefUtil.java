package com.carles.jogging.common;

import android.content.SharedPreferences;

import com.google.gson.Gson;

public class PrefUtil {

    /**
     * Retrieves an object from preferences at the given key, which was stored as a JSON string.
     *
     * If there is no object stored for that key, this method returns null.
     */
    public static <T> T getObject(SharedPreferences preferences, String key, Class<T> clazz) {

        final Gson gson = new Gson();
        final String json = preferences.getString(key, null);
        if (json != null) {
            return gson.fromJson(json, clazz);
        } else {
            return null;
        }
    }

    /**
     * Stores an object in preferences as a JSON string.
     *
     * If the object is null, the specified key will be removed.
     */
    public static void putObject(SharedPreferences preferences, String key, Object object) {

        final SharedPreferences.Editor editor = preferences.edit();
        if (object != null) {
            final Gson gson = new Gson();
            final String json = gson.toJson(object);
            editor.putString(key, json);
        } else {
            editor.remove(key);
        }
        editor.commit();
    }
}