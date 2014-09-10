package com.carles.jogging.util;

import android.app.ActivityManager;
import android.content.Context;
import android.os.PowerManager;
import android.provider.SyncStateContract;
import android.util.Log;

import com.carles.jogging.C;

/**
 * Created by carles1 on 5/09/14.
 */
public class SystemUtil {

    public static boolean isAppInForeground(Context ctx) {
        final ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        final String runningAppPackageName = activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
        return runningAppPackageName.startsWith(ctx.getPackageName());
    }

    public static boolean isScreenOn(Context ctx) {
        return ((PowerManager) ctx.getSystemService(Context.POWER_SERVICE)).isScreenOn();
    }

}
