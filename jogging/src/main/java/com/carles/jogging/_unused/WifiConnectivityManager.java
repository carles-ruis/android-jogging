package com.carles.jogging._unused;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.carles.jogging.jogging.gps_connectivity.GpsConnectivityObserver;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by carles1 on 26/04/14.
 */
public class WifiConnectivityManager {

    private static List<GpsConnectivityObserver> mObservers;

    /*- singleton manager instance */
    private static WifiConnectivityManager mManager;
    private static Context mContext;

    private boolean mConnected;

    private WifiConnectivityManager(Context context) {
        mObservers = new ArrayList<GpsConnectivityObserver>();
        mContext = context;
        mConnected = isConnectionEnabled();
    }

    public static WifiConnectivityManager instance(Context context) {

        if (mManager == null) {
            mManager = new WifiConnectivityManager(context);
        }

        return mManager;
    }

    private boolean isConnectionEnabled() {
        try {
            ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isGpsConnectionEnabled() {
        try {
            ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI && netInfo.isConnectedOrConnecting();
        } catch (Exception e) {
            return false;
        }
    }

    public void addObserver(GpsConnectivityObserver observer) {
        mObservers.add(observer);
        observer.manageGpsConnectivityNotification(mConnected);
    }

    public void removeObserver(GpsConnectivityObserver observer) {
        mObservers.remove(observer);
    }

    public void notifyConnectionChange() {
        mConnected = isGpsConnectionEnabled();
        for (GpsConnectivityObserver observer : mObservers) {
            observer.manageGpsConnectivityNotification(mConnected);
        }
    }
}