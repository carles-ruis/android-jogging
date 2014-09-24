package com.carles.jogging.jogging.first_location;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.carles.jogging.jogging.gps_connectivity.GpsConnectivityManager;
import com.carles.jogging.jogging.gps_connectivity.GpsConnectivityObserver;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import java.lang.ref.WeakReference;

/**
 * Obtains first location before the user starts running.
 * <p/>
 * It will keep requesting locations for MIN_REQUEST_TIME_IN_MS. If after this time the best accurated location in sent in case that an enough accurate location
 * had been obtained. If it has not an enough accurate location has been obtained, it will keep requesting locations until it receives an accurated one or a
 * timeout defined by MAX_REQUEST_EXTRA_TIME_IN_MS.
 * <p/>
 * Created by carles1 on 26/04/14.
 */
public class FirstLocationService extends Service implements GpsConnectivityObserver,
        GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = FirstLocationService.class.getSimpleName();
    private static final int MIN_REQUEST_TIME = 20 * 1000;
    private static final int MAX_REQUEST_TIME = 60 * 1000;
    private static final int MAX_GPS_CONNECTION_TIME = 60 * 1000;
    private static final long UPDATE_INTERVAL = 4 * 1000;
    private static final float ACCURACY_LIMIT = 50.0f;
    private static final float LOW_ACCURACY_LIMIT = 100.0f;

    private static final String WAKE_LOCK_TAG = "wake_lock_tag";
    private static PowerManager.WakeLock wakelock;

    private long stopRequestingTime;
    private final IBinder binder = new FirstLocationServiceBinder();
    // using weak reference to the client activity to avoid becoming out of scope when is recreated
    private WeakReference<OnFirstLocationResultListener> client;

    private LocationClient locationClient;
    private LocationRequest locationRequest;
    private Location bestLocation;

    private Handler handler = new Handler();
    private FirstLocationTimeout locationTimeout = new FirstLocationTimeout();
    private GpsConnectionTimeout gpsConnectionTimeout = new GpsConnectionTimeout();

    @Override
    public void onCreate() {
        acquireWakelock();
        GpsConnectivityManager.instance(this).addObserver(this);

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(UPDATE_INTERVAL);
        locationClient = new LocationClient(this, this, this);
    }

    private void acquireWakelock() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakelock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG);
        wakelock.acquire();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
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
        handler.postDelayed(gpsConnectionTimeout, MAX_GPS_CONNECTION_TIME);
        locationClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        handler.removeCallbacks(gpsConnectionTimeout);

        stopRequestingTime = System.currentTimeMillis() + MIN_REQUEST_TIME;
        handler.postDelayed(locationTimeout, MAX_REQUEST_TIME);

        locationClient.requestLocationUpdates(locationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        // check if this is the first location or the best accurated location
        if (bestLocation == null || location.getAccuracy() <= bestLocation.getAccuracy()) {
            bestLocation = location;
        }

        // check if we should keeping requesting location updates
        if (System.currentTimeMillis() < stopRequestingTime) {
            return;
        }

        // sent best location obtained if it's enough accurated
        if (bestLocation.getAccuracy() <= ACCURACY_LIMIT) {
            handler.removeCallbacks(locationTimeout);
            client.get().onLocationObtained(bestLocation);
        }
    }

    @Override
    public void onDestroy() {
        /*- remove all callbacks (at the moment there's only 'locationTimeout' */
        handler.removeCallbacksAndMessages(null);

        /*- stop requesting location updates and close connection to google play services */
        if (locationClient.isConnected()) {
            locationClient.removeLocationUpdates(this);
        }
        locationClient.disconnect();
        locationClient = null;

        GpsConnectivityManager.instance(this).removeObserver(this);
        releaseWakeLock();
    }

    private void releaseWakeLock() {
        if (wakelock != null) {
            wakelock.release();
        }
    }

    @Override
    public void manageGpsConnectivityNotification(boolean connectionEnabled) {
        if (!connectionEnabled) {
            Log.i(TAG, "gps connection lost");
            if (client != null){
                client.get().onLocationFailed(Error.GPS_LOST);
            }
        }
    }

    @Override
    public void onDisconnected() {
        Log.i(TAG, "google play services disconnected");
        if (client != null) {
            client.get().onLocationFailed(Error.GOOGLE_PLAY_SERVICES_UNAVAILABLE);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "google play services connection failed");
        if (client != null) {
            client.get().onLocationFailed(Error.GOOGLE_PLAY_SERVICES_UNAVAILABLE);
        }
    }

    public interface OnFirstLocationResultListener {
        void onLocationObtained(Location location);
        void onLocationFailed(Error error);
    }

    /*- ************************************************************** */
    /*- ************************************************************** */
    private class GpsConnectionTimeout implements Runnable {
        @Override
        public void run() {
            client.get().onLocationFailed(Error.GPS_NOT_CONNECTED);
        }
    }

    private class FirstLocationTimeout implements Runnable {
        @Override
        public void run() {

            if (bestLocation != null && bestLocation.getAccuracy() <= LOW_ACCURACY_LIMIT) {
                Log.i(TAG, "First location timeout: using a location with low accuracy");
                client.get().onLocationObtained(bestLocation);

            } else {
                Log.i(TAG, "First location timeout. No location with reasonable accuracy");
                client.get().onLocationFailed(Error.NO_LOCATIONS);
            }
        }
    }

    /*- ************************************************************* */
    /*- ************************************************************* */

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

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
