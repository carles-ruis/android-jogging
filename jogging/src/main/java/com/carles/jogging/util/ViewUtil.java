package com.carles.jogging.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ViewUtil {

    private static final String TAG = ViewUtil.class.getName();

    /**
     * Obtains the size in dips of a resource
     */
    public static int getDps(Context ctx, int resId) {
        Resources res = ctx.getResources();
        return (int) (res.getDimension(resId) / res.getDisplayMetrics().density);
    }

    /**
     * Converts Density Independent Pixels (dip, dp) to Pixels (px)
     */
    public static int dpsToPx(Context ctx, int dips) {
        final float density = ctx.getResources().getDisplayMetrics().density;
        return (int) (dips * density);
    }


}
