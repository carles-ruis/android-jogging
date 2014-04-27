package com.carles.jogging.service;

/**
 * Created by carles1 on 26/04/14.
 */
public interface GpsConnectivityObserver {

    /*- action to be made when the observer is notificated */
    public void manageGpsConnectivityNotification(boolean connectionEnabled);
}