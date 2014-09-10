package com.carles.jogging.util;

import android.content.Context;
import android.content.res.Resources;

public class ViewUtil {

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
