package com.carles.jogging.jogging.gps_connectivity;

import android.content.Context;
import android.location.LocationManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by carles1 on 26/04/14.
 */
public class GpsConnectivityManager {

    private static List<GpsConnectivityObserver> mObservers;

    /*- singleton manager instance */
    private static GpsConnectivityManager mManager;
    private static Context mContext;
    private boolean enabled;

    private GpsConnectivityManager(Context context) {
        mObservers = new ArrayList<GpsConnectivityObserver>();
        mContext = context;
        checkIfGpsEnabled();
    }

    public static GpsConnectivityManager instance(Context context) {
        if (mManager == null) {
            mManager = new GpsConnectivityManager(context);
        }
        return mManager;
    }

    private void checkIfGpsEnabled() {
        LocationManager service = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void addObserver(GpsConnectivityObserver observer) {
        mObservers.add(observer);
        observer.manageGpsConnectivityNotification(enabled);
    }

    public void removeObserver(GpsConnectivityObserver observer) {
        mObservers.remove(observer);
    }

    public void notifyConnectionChange() {
        checkIfGpsEnabled();
        for (GpsConnectivityObserver observer : mObservers) {
            observer.manageGpsConnectivityNotification(enabled);
        }
    }
}