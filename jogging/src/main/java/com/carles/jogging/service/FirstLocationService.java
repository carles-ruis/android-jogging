package com.carles.jogging.service;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import com.carles.jogging.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Obtains first location before the user starts running.
 * <p/>
 * It will keep requesting locations for MIN_REQUEST_TIME_IN_MS. If after this time the best accurated location in sent in case that an enough accurate location
 * had been obtained. If it has not an enough accurate location has been obtained, it will keep requesting locations until it receives an accurated one or a
 * timeout defined by MAX_REQUEST_EXTRA_TIME_IN_MS.
 * <p/>
 * Created by carles1 on 26/04/14.
 */
public class FirstLocationService extends Service implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {

    private static final int MIN_REQUEST_TIME = 30 * 1000;
    private static final int MAX_REQUEST_TIME = 120 * 1000;
    private static final long UPDATE_INTERVAL = 4 * 1000;
    private static final float ACCURACY_LIMIT = 25.0f;

    private final IBinder binder = new FirstLocationServiceBinder();
    /*- i used weak reference to the client activity because if this is recreated the reference becomes out of scope */
    private WeakReference<OnFirstLocationResultListener> client;

    private LocationClient locationClient;
    private LocationRequest locationRequest;

    private long stopRequestingTime;
    private Location bestLocation;

    private Handler handler = new Handler();
    private FirstLocationTimeout firstLocationTimeout = new FirstLocationTimeout();

    private List<Float> accuracies = new ArrayList<Float>(); // TODO delete

    @Override
    public void onCreate() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(UPDATE_INTERVAL);
        locationClient = new LocationClient(this, this, this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setClient(OnFirstLocationResultListener client) {
        if (client == null) {
            this.client = null;
        } else {
            this.client = new WeakReference<OnFirstLocationResultListener>(client);
        }
    }

    /*- Called from the client */
    public void requestLocation() {
        locationClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        stopRequestingTime = System.currentTimeMillis() + MIN_REQUEST_TIME;
        handler.postDelayed(firstLocationTimeout, MAX_REQUEST_TIME);

        /*- don't use getLastLocation because could keep obsolete data */
        locationClient.requestLocationUpdates(locationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {

        accuracies.add(location.getAccuracy()); // TODO delete

        /*- first location obtained */
        if (bestLocation == null) {
            bestLocation = location;
        }

        /*- check if this is the best accurated location */
        if (location.getAccuracy() <= bestLocation.getAccuracy()) {
            bestLocation = location;
        }

        /*- check if we should keeping requesting location updates */
        if (System.currentTimeMillis() < stopRequestingTime) {
            return;
        }

        /*- sent best location obtained if it's enough accurated */
        if (bestLocation.getAccuracy() <= ACCURACY_LIMIT) {

            Log.i("LIST OF FIRST ACCURACIES OBTAINED = " + accuracies.toString());

            handler.removeCallbacks(firstLocationTimeout);
            client.get().onLocationObtained(bestLocation);
        }
    }

    @Override
    public void onDestroy() {
        /*- remove all callbacks (at the moment there's only 'firstLocationTimeout' */
        handler.removeCallbacksAndMessages(null);

        /*- stop requesting location updates and close connection to google play services */
        if (locationClient.isConnected()) {
            locationClient.removeLocationUpdates(this);
        }
        locationClient.disconnect();
        locationClient = null;
    }

    @Override
    public void onDisconnected() {
        client.get().onLocationFailed();

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        client.get().onLocationFailed();
    }

    /*- ************************************************************************************************************** */
    /*- ************************************************************************************************************** */
    public interface OnFirstLocationResultListener {
        void onLocationObtained(Location location);
        void onLocationFailed();
    }

    /*- ************************************************************************************************************** */
    /*- ************************************************************************************************************** */
    private class FirstLocationTimeout implements Runnable {
        @Override
        public void run() {
            client.get().onLocationFailed();
        }
    }

    /*- ************************************************************************************************************** */
    /*- ************************************************************************************************************** */

    /*
        Class used for the client Binder.  Because we know this service always
         runs in the same process as its clients, we don't need to deal with IPC.
    */
    public class FirstLocationServiceBinder extends Binder {
        public FirstLocationService getService() {
            return FirstLocationService.this;
        }
    }

}
