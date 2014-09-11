package com.carles.jogging.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.carles.jogging.C;
import com.carles.jogging.model.UserModel;
import com.google.gson.Gson;

public class PrefUtil {

    public static int getLastKilometersSelectedPosition(Context ctx) {
        return ctx.getSharedPreferences(C.PREF_FILE, Context.MODE_PRIVATE).getInt(C.PREF_LAST_KILOMETERS_SELECTED, 0);
    }

    public static void setLastKilometersSelectedPosition(Context ctx, int lastKilometersSelected) {
        ctx.getSharedPreferences(C.PREF_FILE, Context.MODE_PRIVATE).edit().putInt(C.PREF_LAST_KILOMETERS_SELECTED, lastKilometersSelected).commit();
    }

    public static UserModel getLoggedUser(Context ctx) {
        return getPreference(ctx.getSharedPreferences(C.PREF_FILE, Context.MODE_PRIVATE), C.PREF_LOGGER_USER, UserModel.class);
    }

    public static void setLoggedUser(Context ctx, UserModel user) {
        putPreference(ctx.getSharedPreferences(C.PREF_FILE, Context.MODE_PRIVATE), C.PREF_LOGGER_USER, user);
    }

    public static void removeLoggedUserFromPrefs(Context ctx) {
        ctx.getSharedPreferences(C.PREF_FILE, Context.MODE_PRIVATE).edit().remove(C.PREF_LOGGER_USER).commit();
    }

    private static void putPreference(SharedPreferences prefs, String key, Object value) {
        PrefUtil.putObject(prefs, key, value);
    }

    private static <T> T getPreference(SharedPreferences prefs, String key, Class<T> clazz) {
        return PrefUtil.getObject(prefs, key, clazz);
    }

    /**
     * Retrieves an object from preferences at the given key, which was stored as a JSON string.
     *
     * If there is no object stored for that key, this method returns null.
     */
    private static <T> T getObject(SharedPreferences preferences, String key, Class<T> clazz) {

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
    private static void putObject(SharedPreferences preferences, String key, Object object) {

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