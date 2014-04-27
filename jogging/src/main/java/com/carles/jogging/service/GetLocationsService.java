package com.carles.jogging.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;

import com.carles.jogging.C;
import com.carles.jogging.R;
import com.carles.jogging.enums.FootingResult;
import com.carles.jogging.util.Decimals;
import com.carles.jogging.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.joda.time.Seconds;

/**
 * Created by carles1 on 26/04/14.
 */
public class GetLocationsService extends Service implements GpsConnectivityObserver, GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {

    private static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    private static final int MILLISECONDS_PER_SECOND = 1000;
    private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    private static final int FASTEST_INTERVAL_IN_SECONDS = 5;
    private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    private static final float SMALLEST_DISPLACEMENT = 5.0f;
    private static final float MIN_ACCURACY = 13.0f;

    private static final String WAKE_LOCK_TAG = "wake_lock_tag";
    private static PowerManager.WakeLock wakelock;

    private long startTime;
    private long currentTime;
    private long noRespondingTime;
    private long totalTime;

    private Location startLocation;
    private Location previousLocation;
    private Location currentLocation;

    private float totalDistance;
    private float currentDistance;

    private LocationClient locationClient;
    private LocationRequest locationRequest;

    /*- handle if there hasn't been location updates */
    private Handler handler = new Handler();
    private Runnable monitor = new LocationUpdatesMonitor();

    @Override
    public void onCreate() {
        Log.i("service onCreate");

         /*- request wakelock to avoid the device goes to sleep and misses location updates */
        acquireWakelock();

        /*- subscribe to gps connectivity changes */
        GpsConnectivityManager.instance(this).addObserver(this);

         /*- run in the foreground, it will show an ongoing notification with an empty intent attached */
        PendingIntent emptyIntent = PendingIntent.getActivity(this, C.NOT_USED, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_notification_is_running).
                setContentTitle(getString(R.string.notification_is_running_title)).setContentText(getString(R.string.notification_is_running_text)).setContentIntent(emptyIntent);
        Notification notification = builder.build();
        startForeground(C.ONGOING_NOTIFICATION_IS_RUNNING, notification);

        /*- configuring accuracy of timing of requests to gps */
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT);

        /*- set the connection callbacks and location update callbacks */
        locationClient = new LocationClient(this, this, this);

    }

    private void acquireWakelock() {
        Log.i("service acquires wakelock");

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakelock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG);
        wakelock.acquire();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("service onStartCommand");

        totalDistance = intent.getIntExtra(C.EXTRA_DISTANCE_IN_METERS, C.DEFAULT_DISTANCE);

        locationClient.connect();

          /*- if the system runs the service don't bother recreating it */
        return START_NOT_STICKY;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i("service became connected");

        /*- start running: app requests location updates */
        locationClient.requestLocationUpdates(locationRequest, this);
        /*- control if the service is  waiting too much time for the first location update */
        handler.postDelayed(monitor, C.MAX_LOCATION_NOT_UPDATED_TIME);

        Log.i("START LOCATION=" + startLocation);
        Log.i(Decimals.one("ON CONNECTED DELAY (SEC)=", (System.currentTimeMillis() - startTime) / 1000));
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i("service location changed");

        /*- check if the location obtained is enough accurated */
        if (location.getAccuracy() > MIN_ACCURACY) {
            return;
        }

        /*- control if the service is  waiting too much time for a location update */
        handler.removeCallbacks(monitor);
        handler.postDelayed(monitor, C.MAX_LOCATION_NOT_UPDATED_TIME);

        /*- get first location */
        if (startLocation == null) {
            startTime = System.currentTimeMillis();
            currentDistance = 0f;

            startLocation = location;
            previousLocation = location;
            currentLocation = location;
            return;
        }

        /*- update location and distance runned */
        currentLocation = location;
        float distance = currentLocation.distanceTo(previousLocation);
        currentDistance += distance;
        previousLocation = currentLocation;

        Log.i("Distance in meters=" + distance);
        Log.i("Accuracy in meters=" + location.getAccuracy());
//        Log.i("Current distance runned=" + currentDistance);
//        Log.i("Provisional time runned in minutes=" + Minutes.minutesBetween(new DateTime(startTime), new DateTime(System.currentTimeMillis())).getMinutes());
        Log.i("Provisional time runned in seconds=" + Seconds.secondsBetween(new DateTime(startTime), new DateTime(System.currentTimeMillis())).getSeconds());

        /*- check if has reached the goal */
        if (currentDistance >= totalDistance) {
            currentTime = System.currentTimeMillis() - startTime;
            // TODO refine totalTime considering the distance overrunned
            totalTime = currentTime;

            stopRunning();

            notifyFootingEnded(FootingResult.SUCCESS);

            Log.i("Total time runned=" + Minutes.minutesBetween(new DateTime(startTime), new DateTime(totalTime)).getMinutes());
        }

    }

    private void stopRunning() {
        /*- stopping the service implies calling onDestroy */
        Log.i("service stopSelf");
        stopSelf();
    }

    @Override
    public void onDestroy() {
        Log.i("service destroyed");

        /*- stop checking periodicity of location updates */
        handler.removeCallbacks(monitor);

        /*- stop requesting location updates and close connection to google play services */
        if (locationClient.isConnected()) {
            locationClient.removeLocationUpdates(this);
        }
        locationClient.disconnect();
        locationClient = null;

        stopForeground(true);
        GpsConnectivityManager.instance(this).removeObserver(this);
        releaseWakeLock();
    }

    private void releaseWakeLock() {
        Log.i("service wakelock released");
        if (wakelock != null) {
            wakelock.release();
        }
    }

    @Override
    public void manageGpsConnectivityNotification(boolean connectionEnabled) {
        if (connectionEnabled) {
            Log.i("service observed: gps connectivity on");
        } else {
            Log.i("service observed: gps connectivity off");
            stopRunning();

            notifyFootingEnded(FootingResult.GPS_DISABLED);

        }
    }

    @Override
    public void onDisconnected() {
        Log.i("Google play services status: disconnected");
        stopRunning();

        notifyFootingEnded(FootingResult.GOOGLE_SERVICES_DISCONNECTED);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("Google play services status: connection failed");
        stopRunning();

        notifyFootingEnded(FootingResult.GOOGLE_SERVICES_FAILURE);
    }

    @Override
    /*- we are not using binding */
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void notifyFootingEnded(FootingResult result) {
        Log.i("FootingResult=" + result.toString());

        // TODO sent broadcast with the results

    }

    /*- ************************************************************************************************************** */
    /*- ************************************************************************************************************** */
    private class LocationUpdatesMonitor implements Runnable {

        @Override
        public void run() {
            Log.i("location updates delay triggered : device has not obtained location in too much time");
            stopRunning();

            notifyFootingEnded(FootingResult.DETECTED_STOPPED_RUNNING);
        }
    }
}




