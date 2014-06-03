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
import android.support.v4.content.IntentCompat;

import com.carles.jogging.C;
import com.carles.jogging.R;
import com.carles.jogging.activity.ResultActivity;
import com.carles.jogging.enums.FootingResult;
import com.carles.jogging.helper.LocationHelper;
import com.carles.jogging.model.PartialJogging;
import com.carles.jogging.util.FormatUtil;
import com.carles.jogging.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by carles1 on 26/04/14.
 */
public class LocationService extends Service implements GpsConnectivityObserver, GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {

    private static final long MIN_REQUEST_TIME = 30 * 1000;
    private static final long TIME_BETWEEN_REQUESTS = 30 * 1000;
    private static final long MAX_REQUEST_TIME = 90 * 1000;
    private static final long UPDATE_INTERVAL = 4 * 1000;
    private static final float SMALLEST_DISPLACEMENT = 1.0f;

    private static final float ACCURACY_LIMIT = 25.0f;
    private static final float LOWEST_ACCURACY_LIMIT = 50.0f;

    private static final String WAKE_LOCK_TAG = "wake_lock_tag";
    private static PowerManager.WakeLock wakelock;

    /*- unused at this moment */
    //    private LocationServiceBinder locationServiceBinder = new LocationServiceBinder();
    //    private OnKilometerRanListener client;

    private List<PartialJogging> partials = new ArrayList<PartialJogging>();

    private long startTime;
    private long totalTime;

    private float currentDistance;
    private float totalDistance;
    private String totalDistanceText;

    private Location startLocation;
    private Location previousLocation;
    private Location bestLocation;

    private LocationClient locationClient;
    private LocationRequest locationRequest;

    /*- used to check if there aren't location updates */
    private Handler handler = new Handler();
    private Runnable locationTimeoutUpdating = new LocationTimeoutUpdating();
    private Runnable locationStartUpdating = new LocationStartUpdating();
    private long stopRequestingTime;

    private List<Float> accuracies = new ArrayList<Float>(); // TODO delete

    @Override
    public void onCreate() {
          /*- request wakelock to avoid the device goes to sleep and misses location updates */
        acquireWakelock();

        /*- subscribe to gps connectivity changes */
        GpsConnectivityManager.instance(this).addObserver(this);

        /*- configuring accuracy of timing of requests to gps */
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(UPDATE_INTERVAL);
        locationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT);

        /*- set the connection callbacks and location update callbacks */
        locationClient = new LocationClient(this, this, this);

    }

    private void acquireWakelock() {
        Log.i("service acquireWakelock");

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakelock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG);
        wakelock.acquire();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /*- case 1: running cancelled by the user from activity */
        FootingResult footingResult = (FootingResult) intent.getSerializableExtra(C.EXTRA_FOOTING_RESULT);
        if (FootingResult.CANCELLED_BY_USER == footingResult) {
            Log.i("RUNNING IS CANCELLED BY THE USER");
            stopRunning(FootingResult.CANCELLED_BY_USER);
        }

        /*- case 2: countdown finished from activity: start running */
        /*- init variables from the intent received */
        startLocation = intent.getParcelableExtra(C.EXTRA_FIRST_LOCATION);
        previousLocation = startLocation;
        bestLocation = null;

        currentDistance = 0f;
        totalDistance = intent.getIntExtra(C.EXTRA_DISTANCE_IN_METERS, C.DEFAULT_DISTANCE);
        totalDistanceText = intent.getStringExtra(C.EXTRA_DISTANCE_TEXT);

        startTime = System.currentTimeMillis();

        /*- add location #1, that is the location obtained before start running */
        PartialJogging partialJogging = new PartialJogging(startLocation, currentDistance, 0, startTime);
        partials.add(partialJogging);
        Log.i("FIRST LOCATION = " + partialJogging.toString());

        startForegroundAndShowOngoingNotification();

        locationClient.connect();

          /*- if the system kills the service don't bother recreating it */
        return START_NOT_STICKY;
    }

    private void startForegroundAndShowOngoingNotification() {
        /*- notification will have an empty attachment */
        PendingIntent emptyIntent = PendingIntent.getActivity(this, C.NOT_USED, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_notification_is_running).
                setContentTitle(getString(R.string.notification_is_running_title)).setContentText(getString(R.string.notification_is_running_text)).setContentIntent(emptyIntent);
        Notification notification = builder.build();

        startForeground(C.ONGOING_NOTIFICATION_IS_RUNNING, notification);
    }

    @Override
    public void onConnected(Bundle bundle) {
        /*- wait before start requesting new updates */
        handler.postDelayed(locationStartUpdating, TIME_BETWEEN_REQUESTS);
        Log.i("LOCATION WILL START UPDATING AT " + FormatUtil.time(System.currentTimeMillis() + TIME_BETWEEN_REQUESTS));
    }

    private void startRequestingLocationUpdates() {
        long now = System.currentTimeMillis();
        stopRequestingTime = now + MIN_REQUEST_TIME;
        Log.i("LOCATION WILL STOP UPDATING AT " + FormatUtil.time(stopRequestingTime));

        handler.postDelayed(locationTimeoutUpdating, MAX_REQUEST_TIME);
        Log.i("LOCATION MAY TRIGGER TIMEOUT AT " + FormatUtil.time(now + MAX_REQUEST_TIME));

        /*- start running: app requests location updates */
        locationClient.requestLocationUpdates(locationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        /*- we're storing accuracies for debug */
        accuracies.add(location.getAccuracy()); // TODO delete

        /*- check if this is the first location or the best accurated location */
        if (bestLocation == null || location.getAccuracy() <= bestLocation.getAccuracy()) {
           /*- ignore repeated locations */
            if (location.distanceTo(previousLocation) > 0.0f) {
                bestLocation = location;
                Log.i("BEST LOCATION UPDATED " + LocationHelper.toString(location));
            } else { // TODO Delete
                Log.i("BEST LOCATION REPEATED " + LocationHelper.toString(location));
            }
        } else {
            Log.i("IS NOT THE BEST LOCATION " + LocationHelper.toString(location));

        /*- check if we should keeping requesting location updates */
            long now = System.currentTimeMillis();
            if (bestLocation == null || now < stopRequestingTime) {
                return;
            }

        /*- take best location obtained if it's enough accurated */
            if (bestLocation != null && bestLocation.getAccuracy() <= ACCURACY_LIMIT) {
                onLocationObtained();
            }
        }
    }

    private void onLocationObtained() {
        Log.i("LOCATIONS DIFFERENCE IN METERS " + bestLocation.distanceTo(previousLocation));

        /*- stop requesting location updates end reset timeout handler */
        handler.removeCallbacks(locationTimeoutUpdating);
        locationClient.removeLocationUpdates(this);

        /*- update location and distance */
        currentDistance = currentDistance + bestLocation.distanceTo(previousLocation);
        long partialTime = bestLocation.getTime() - startTime;
        PartialJogging partial = new PartialJogging(bestLocation, currentDistance, partialTime, bestLocation.getTime());
        partials.add(partial);
        Log.i("PARTIAL LOCATION STORED = " + partial.toString());

        Log.i("LIST OF ACCURACIES OBTAINED (GOOD) = " + accuracies.toString());
        accuracies.clear();

        previousLocation = bestLocation;
        bestLocation = null;

        if (currentDistance < totalDistance) {
            handler.postDelayed(locationStartUpdating, TIME_BETWEEN_REQUESTS);

        } else {
            totalTime = bestLocation.getTime() - startTime;

            Log.i("TOTAL TIME " + LocationHelper.formatRunningTime(totalTime));
            Log.i("TOTAL DISTANCE " + currentDistance);

            stopRunning(FootingResult.SUCCESS);
        }

    }

    private void stopRunning(FootingResult footingResult) {

        Log.i("FOOTING RESULT=" + footingResult.toString());

        /*- send broadcast with the results : ResultActivity and maybe JoggingActivity will get the intent */
        //        Intent intent = new Intent(C.ACTION_JOGGING_FINISHED);
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra(C.EXTRA_DISTANCE_TEXT, totalDistanceText);
        intent.putExtra(C.EXTRA_DISTANCE_IN_METERS, currentDistance);
        intent.putExtra(C.EXTRA_FOOTING_TIME, totalTime);
        intent.putExtra(C.EXTRA_FOOTING_RESULT, footingResult);
        intent.putParcelableArrayListExtra(C.EXTRA_PARTIALS, (ArrayList<PartialJogging>) partials);

        /*- the activity will start a new task. Maybe it interrupts the user from something he's doing. Then use PendingIntent+Notification */
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);

        //        sendBroadcast(intent);

        Log.i("Service about to start activity");
        startActivity(intent);



        /*- stopping the service implies calling onDestroy */
        Log.i("service stopSelf");
        stopSelf();
    }

    @Override
    public void onDestroy() {
        Log.i("service onDestroy");

        /*- stop handling periodicity of location updates */
        handler.removeCallbacks(locationTimeoutUpdating);
        handler.removeCallbacks(locationStartUpdating);

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
        Log.i("service releaseWakelock");
        if (wakelock != null) {
            wakelock.release();
        }
    }

    @Override
    public void manageGpsConnectivityNotification(boolean connectionEnabled) {
        if (connectionEnabled) {
            Log.i("gps_connectivity_observer on");
        } else {
            Log.i("gps_connectivity_observer off");
            stopRunning(FootingResult.GPS_DISABLED);
        }
    }

    @Override
    public void onDisconnected() {
        Log.i("google play services disconnected");
        stopRunning(FootingResult.GOOGLE_SERVICES_DISCONNECTED);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("google play services connection failed");
        stopRunning(FootingResult.GOOGLE_SERVICES_FAILURE);
    }

    /*- ************************************************************************************************************** */
    /*- ************************************************************************************************************** */

    @Override
    public IBinder onBind(Intent intent) {
        return null;
        //        return locationServiceBinder;
    }

    /*- ************************************************************************************************************** */
    /*- ************************************************************************************************************** */
    public interface OnKilometerRanListener {
        void onKilometerRun(int kilometers);
    }

    /*- ************************************************************************************************************** */
    /*- ************************************************************************************************************** */
    private class LocationTimeoutUpdating implements Runnable {

        @Override
        public void run() {
            Log.i("LOCATION NOW TIMED OUT " + FormatUtil.time(System.currentTimeMillis()));

            Log.i("LIST OF ACCURACIES OBTAINED (BAD) = " + accuracies.toString());

            if (bestLocation != null && bestLocation.getAccuracy() <= LOWEST_ACCURACY_LIMIT) {
                onLocationObtained();
            } else {
                stopRunning(FootingResult.NO_LOCATION_UPDATES);
            }
        }
    }

    /*- ************************************************************************************************************** */
    /*- ************************************************************************************************************** */
    private class LocationStartUpdating implements Runnable {
        @Override
        public void run() {
            Log.i("LOCATION NOW START UPDATING " + FormatUtil.time(System.currentTimeMillis()));
            startRequestingLocationUpdates();
        }
    }

}