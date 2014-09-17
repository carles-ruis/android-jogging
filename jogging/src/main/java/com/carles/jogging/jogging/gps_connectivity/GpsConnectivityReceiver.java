package com.carles.jogging.jogging.gps_connectivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.carles.jogging.jogging.gps_connectivity.GpsConnectivityManager;

/**
 * Created by carles1 on 26/04/14.
 */
public class GpsConnectivityReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        GpsConnectivityManager.instance(context).notifyConnectionChange();
    }
}
