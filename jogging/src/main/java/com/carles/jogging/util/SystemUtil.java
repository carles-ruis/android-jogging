package com.carles.jogging.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by carles1 on 5/09/14.
 */
public class SystemUtil {

    private static final String TAG = SystemUtil.class.getName();

    public static boolean isAppInForeground(Context ctx) {
        final ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        final String runningAppPackageName = activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
        return runningAppPackageName.startsWith(ctx.getPackageName());
    }

    public static boolean isScreenOn(Context ctx) {
        return ((PowerManager) ctx.getSystemService(Context.POWER_SERVICE)).isScreenOn();
    }

    public static boolean takeScreenshot(Activity ctx, String filename) {
        // image naming and path  to include sd card  appending name you choose for file
        String mPath = Environment.getExternalStorageDirectory().toString() + "/" + filename;

        // create bitmap screen capture
        Bitmap bitmap;
        View v1 = ctx.getWindow().getDecorView().getRootView();
        v1.setDrawingCacheEnabled(true);
        bitmap = Bitmap.createBitmap(v1.getDrawingCache());
        v1.setDrawingCacheEnabled(false);

        OutputStream fout = null;
        File imageFile = new File(mPath);

        try {
            fout = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
            fout.flush();
            fout.close();
            return true;

        } catch (FileNotFoundException e) {
            Log.e(TAG, "Unable to take screenshot. Error:" + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            Log.e(TAG, "Unable to take screenshot. Error:" + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static Bitmap loadBitmap(String filename) {
        Bitmap ret = null;
        try {
            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + filename;

            BitmapFactory.Options options = new BitmapFactory.Options();
            // use a format that preserves alpha when decoding
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            ret = BitmapFactory.decodeFile(mPath, options);

            Log.e("carles","bitmap was decoded successfully");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

}
