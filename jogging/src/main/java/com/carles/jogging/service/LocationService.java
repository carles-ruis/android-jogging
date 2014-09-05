package com.carles.jogging.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.IntentCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.carles.jogging.C;
import com.carles.jogging.R;
import com.carles.jogging.common.FormatUtil;
import com.carles.jogging.jogging.FootingResult;
import com.carles.jogging.model.JoggingModel;
import com.carles.jogging.result.ResultDetailActivity;
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

    private static final String TAG = LocationService.class.getName();
    private static final long TIME_BETWEEN_REQUESTS = 30 * 1000;
    private static final long MIN_REQUEST_TIME = 30 * 1000;
    private static final long MAX_REQUEST_TIME = 90 * 1000;
    private static final long UPDATE_INTERVAL = 4 * 1000;
    private static final float SMALLEST_DISPLACEMENT = 1.0f;

    private static final float ACCURACY_LIMIT = 25.0f;
    private static final float LOW_ACCURACY_LIMIT = 100.0f;

    private static final String WAKE_LOCK_TAG = "wake_lock_tag";
    private static PowerManager.WakeLock wakelock;

    private List<JoggingModel> partials = new ArrayList<JoggingModel>();

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

    // used to check if there aren't location updates */
    private Handler handler = new Handler();
    private Runnable locationTimeoutUpdating = new LocationTimeoutUpdating();
    private Runnable locationStartUpdating = new LocationStartUpdating();
    private long stopRequestingTime;

    // sounds when running is over
    private SoundPool soundPool;
    private int stopSoundId = -1;
    private int crowdSoundId = -1;

    private List<Float> accuracies = new ArrayList<Float>(); // TODO delete

    @Override
    public void onCreate() {
          /*- request wakelock to avoid the device goes to sleep and misses location updates */
        acquireWakelock();

        /*- subscribe to gps connectivity changes */
        GpsConnectivityManager.instance(this).addObserver(this);

        /*- load sounds */
        soundPool = new SoundPool(C.MAX_SOUND_STREAMS, AudioManager.STREAM_MUSIC, 0);
        stopSoundId = soundPool.load(this, R.raw.alert_stop_footing, 1);
        crowdSoundId = soundPool.load(this, R.raw.sound_crowd_cheering, 1);

        /*- configuring accuracy of timing of requests to gps */
        locationRequest = LocationRequest.create();
        // use PRIORITY_HIGH_ACCURACY to use all location providers
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(UPDATE_INTERVAL);
        locationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT);

        /*- set the connection callbacks and location update callbacks */
        locationClient = new LocationClient(this, this, this);

    }

    private void acquireWakelock() {
       PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakelock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG);
        wakelock.acquire();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        FootingResult footingResult = (FootingResult) intent.getSerializableExtra(C.EXTRA_FOOTING_RESULT);
        if (FootingResult.CANCELLED_BY_USER == footingResult) {
            // case 1: running cancelled by the user from activity
            stopRunning(FootingResult.CANCELLED_BY_USER);
            // if the system kills the service don't bother recreating it
            return START_NOT_STICKY;

        } else {
            // case 2: countdown finished from activity: start running
            // init variables from the intent received
            startLocation = intent.getParcelableExtra(C.EXTRA_FIRST_LOCATION);
            previousLocation = startLocation;
            bestLocation = null;

            currentDistance = 0f;
            totalDistance = intent.getIntExtra(C.EXTRA_DISTANCE_IN_METERS, C.DEFAULT_DISTANCE);
            totalDistanceText = intent.getStringExtra(C.EXTRA_DISTANCE_TEXT);
            startTime = System.currentTimeMillis();

            startForegroundAndShowOngoingNotification();

            locationClient.connect();

            return START_NOT_STICKY;
        }
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
        // wait before start requesting new updates
        handler.postDelayed(locationStartUpdating, TIME_BETWEEN_REQUESTS);
    }

    private void startRequestingLocationUpdates() {
        long now = System.currentTimeMillis();
        stopRequestingTime = now + MIN_REQUEST_TIME;

        handler.postDelayed(locationTimeoutUpdating, MAX_REQUEST_TIME);

        // start running: app requests location updates
        locationClient.requestLocationUpdates(locationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        accuracies.add(location.getAccuracy()); // TODO delete

        /*- check if this is the first location or the best accurated location */
        if (bestLocation == null || location.getAccuracy() <= bestLocation.getAccuracy()) {
           /*- ignore repeated locations */
            if (location.distanceTo(previousLocation) > 0.0f) {
                bestLocation = location;
            }

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
        // stop requesting location updates end reset timeout handler
        handler.removeCallbacks(locationTimeoutUpdating);
        locationClient.removeLocationUpdates(this);

        // update location and distance and store the partial result
        currentDistance = currentDistance + bestLocation.distanceTo(previousLocation);
        totalTime = bestLocation.getTime() - startTime;
        JoggingModel partial = new JoggingModel(previousLocation, bestLocation, totalTime, currentDistance);
        partials.add(partial);
        previousLocation = bestLocation;
        bestLocation = null;

        // send intent to JoggingFragment to update the distance and kms ran
        Intent intent = new Intent(C.ACTION_UPDATE_KILOMETERS_RUN);
        intent.putExtra(C.EXTRA_FOOTING_TIME_TEXT, FormatUtil.runningTime(totalTime));
        intent.putExtra(C.EXTRA_DISTANCE_TEXT, String.valueOf((int)currentDistance));
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        // TODO delete comments
        Log.e(TAG, "PARTIAL JOGGING STORED = " + partial.toString());
        Log.i(TAG, "LIST OF ACCURACIES OBTAINED (GOOD) = " + accuracies.toString());
        accuracies.clear();

        // check if running is over
        if (currentDistance < totalDistance) {
            handler.postDelayed(locationStartUpdating, TIME_BETWEEN_REQUESTS);

        } else {
            totalTime = bestLocation.getTime() - startTime;
            stopRunning(FootingResult.SUCCESS);
        }

    }

    private void stopRunning(FootingResult footingResult) {
    // TODO delete comments
        Log.e(TAG, "FOOTING RESULT=" + footingResult.toString());
        Log.i(TAG, "Partials:");
        for (JoggingModel partial:partials) {
            Log.i(TAG, partial.toString());
        }

        if (footingResult==FootingResult.SUCCESS) {
            if (crowdSoundId != -1) {
                soundPool.play(crowdSoundId, C.VOLUME, C.VOLUME, 1, 0, 1f);
            }
        } else {
            if (stopSoundId!=-1) {
                soundPool.play(stopSoundId, C.VOLUME, C.VOLUME, 1, 0, 1f);
            }
        }

        // TODO Maybe it interrupts the user from something he's doing. Then use PendingIntent+Notification

        // send intent to show the results
        Intent intent = new Intent(this, ResultDetailActivity.class);
        intent.putExtra(C.EXTRA_DISTANCE_TEXT, totalDistanceText);
        intent.putExtra(C.EXTRA_DISTANCE_IN_METERS, currentDistance);
        intent.putExtra(C.EXTRA_FOOTING_TIME, totalTime);
        intent.putExtra(C.EXTRA_FOOTING_RESULT, footingResult);
        intent.putParcelableArrayListExtra(C.EXTRA_PARTIALS, (ArrayList<JoggingModel>) partials);
        // the activity will start a new task
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
        Log.i(TAG, "Service about to start RESULT activity");
        startActivity(intent);

        // stopping the service implies calling onDestroy
        stopSelf();
    }

    @Override
    public void onDestroy() {
         // stop handling periodicity of location updates
        handler.removeCallbacks(locationTimeoutUpdating);
        handler.removeCallbacks(locationStartUpdating);

        // stop requesting location updates and close connection to google play services/
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
        if (wakelock != null) {
            wakelock.release();
        }
    }

    @Override
    public void manageGpsConnectivityNotification(boolean connectionEnabled) {
        if (!connectionEnabled) {
            Log.i(TAG, "gps_connectivity_observer off");
            stopRunning(FootingResult.GPS_DISABLED);
        }
    }

    @Override
    public void onDisconnected() {
        Log.i(TAG, "google play services disconnected");
        stopRunning(FootingResult.GOOGLE_SERVICES_DISCONNECTED);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "google play services connection failed");
        stopRunning(FootingResult.GOOGLE_SERVICES_FAILURE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*- ************************************************************************************ */
    /*- ************************************************************************************ */
    private class LocationTimeoutUpdating implements Runnable {

        @Override
        public void run() {
            Log.i(TAG, "LIST OF ACCURACIES OBTAINED (BAD) = " + accuracies.toString());

            if (bestLocation != null && bestLocation.getAccuracy() <= LOW_ACCURACY_LIMIT) {
                onLocationObtained();
            } else {
                stopRunning(FootingResult.NO_LOCATION_UPDATES);
            }
        }
    }

    /*- ************************************************************************************* */
    /*- ************************************************************************************* */
    private class LocationStartUpdating implements Runnable {
        @Override
        public void run() {
            startRequestingLocationUpdates();
        }
    }

}