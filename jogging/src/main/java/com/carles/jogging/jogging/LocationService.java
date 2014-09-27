package com.carles.jogging.jogging;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.carles.jogging.C;
import com.carles.jogging.R;
import com.carles.jogging.jogging.gps_connectivity.GpsConnectivityManager;
import com.carles.jogging.jogging.gps_connectivity.GpsConnectivityObserver;
import com.carles.jogging.model.JoggingModel;
import com.carles.jogging.model.JoggingSQLiteHelper;
import com.carles.jogging.model.UserModel;
import com.carles.jogging.result.ResultDetailActivity;
import com.carles.jogging.util.PrefUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by carles1 on 26/04/14.
 */
public class LocationService extends Service implements GpsConnectivityObserver,
        GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = LocationService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 1;
    private static final int SOUND_MAX_DURATION = 2000;

    private static final long TIME_BETWEEN_REQUESTS = 20 * 1000;
    private static final long MIN_REQUEST_TIME = 20 * 1000;
    private static final long MAX_REQUEST_TIME = 60 * 1000;
    private static final long UPDATE_INTERVAL = 4 * 1000;
    private static final float SMALLEST_DISPLACEMENT = 1.0f;
    private static final float ACCURACY_LIMIT = 25.0f;
    private static final float AVERAGE_ACCURACY_LIMIT = 50.0f;
    private static final float LOW_ACCURACY_LIMIT = 100.0f;

    private static final String WAKE_LOCK_TAG = "wake_lock_tag";
    private static PowerManager.WakeLock wakelock;

    private List<JoggingModel> partials = new ArrayList<JoggingModel>();

    private long startTime;
    private long totalTime;
    private float currentDistance;
    private float goalDistance;
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

    // sound to notify user that running is over
    private SoundPool soundPool;
    private int cheerSoundId;
    private int booSoundId;
    private Handler soundHandler;

    @Override
    public void onCreate() {
        // request wakelock to avoid the device goes to sleep and misses location updates
        acquireWakelock();

        // subscribe to gps connectivity changes
        GpsConnectivityManager.instance(this).addObserver(this);

        // configuring accuracy of timing of requests to gps
        locationRequest = LocationRequest.create();
        // use PRIORITY_HIGH_ACCURACY to use all location providers
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(UPDATE_INTERVAL);
        locationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT);

        // set the connection callbacks and location update callbacks
        locationClient = new LocationClient(this, this, this);

        // load sounds
        soundPool = new SoundPool(C.MAX_SOUND_STREAMS, AudioManager.STREAM_MUSIC, 0);
        cheerSoundId = soundPool.load(this, R.raw.sound_crowd_cheering, 1);
        booSoundId = soundPool.load(this, R.raw.alert_stop_footing, 1);
    }

    private void acquireWakelock() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakelock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG);
        wakelock.acquire();
    }

    @Override
    public void onConnected(Bundle bundle) {
        // wait before start requesting new updates
        handler.postDelayed(locationStartUpdating, TIME_BETWEEN_REQUESTS);
    }

    private void startRequestingLocationUpdates() {
        stopRequestingTime = System.currentTimeMillis() + MIN_REQUEST_TIME;
        handler.postDelayed(locationTimeoutUpdating, MAX_REQUEST_TIME);

        // start running: app requests location updates
        // don't use getLastLocation because could keep obsolete data
        locationClient.requestLocationUpdates(locationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {

        // check if this is the first location or the best accurated location
        if (bestLocation == null || location.getAccuracy() <= bestLocation.getAccuracy()) {
            // ignore repeated locations
            if (location.distanceTo(previousLocation) > 0.0f) {
                bestLocation = location;
            }

            // check if we should keeping requesting location updates
            if (bestLocation == null || System.currentTimeMillis() < stopRequestingTime) {
                return;
            }

            // take best location obtained if it's enough accurated
            if (bestLocation != null && bestLocation.getAccuracy() <= ACCURACY_LIMIT) {
                onLocationObtained();
            }

            // if MIN_REQUEST_TIME passed, accept not so good accuracy
            if (bestLocation != null && bestLocation.getAccuracy() <= AVERAGE_ACCURACY_LIMIT &&
                    System.currentTimeMillis() > stopRequestingTime) {
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
        JoggingModel partial = new JoggingModel(previousLocation, bestLocation, totalTime, currentDistance,
                PrefUtil.getLoggedUser(this));
        partials.add(partial);

        previousLocation = bestLocation;
        bestLocation = null;

        if (client != null && client.get() != null) {
            // update the view with the distance ran and time passed
            client.get().onLocationObtained(totalTime, currentDistance);
            //        Intent intent = new Intent(C.ACTION_UPDATE_KILOMETERS_RUN);
            //        intent.putExtra(C.EXTRA_FOOTING_TIME_TEXT, FormatUtil.time(totalTime));
            //        intent.putExtra(C.EXTRA_DISTANCE_IN_METERS, (int)currentDistance);
            //        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }

        // check if running is over
        if (currentDistance < goalDistance) {
            handler.postDelayed(locationStartUpdating, TIME_BETWEEN_REQUESTS);

        } else {
            stopRunning(FootingResult.SUCCESS);
        }

    }

    private void stopRunning(FootingResult footingResult) {
        Log.i(TAG, "Stop running. Result:" + footingResult);

        // prepare intent with the results
        Bundle extras = new Bundle();
        extras.putSerializable(C.EXTRA_FOOTING_RESULT, footingResult);

        UserModel user = PrefUtil.getLoggedUser(this);
        // "partials" object is updated in the JoggingModel constructor
        JoggingModel jogging = null;
        if (partials.size() > 0) {
            jogging = new JoggingModel(partials, goalDistance, footingResult, user);
            extras.putParcelable(C.EXTRA_JOGGING_TOTAL, jogging);
        }

        if (footingResult == FootingResult.SUCCESS) {

            // jogging cannot be null if the footing result is success
            if (jogging.getGoalTime() < JoggingSQLiteHelper.getInstance(this).
                    queryBestTimeByDistance(user, jogging.getGoalDistance())) {
                // user has overtaken his record
                extras.putBoolean(C.EXTRA_BEST_TIME, true);
            }

            // only successful joggings will be saved
            JoggingSQLiteHelper.getInstance(this).insertJogging(jogging, partials);
            extras.putBoolean(C.EXTRA_RUNNING_SAVED, true);
        }

        if (client != null && client.get() != null) {
            client.get().onRunningFinished(extras);
        } else {
            handleOnRunningFinished(extras);
        }

        // play sound to notify the user
        if (footingResult == FootingResult.SUCCESS) {
            soundPool.play(cheerSoundId, C.VOLUME, C.VOLUME, 1, 0, 1f);
        } else {
            soundPool.play(booSoundId, C.VOLUME, C.VOLUME, 1, 0, 1f);
        }

        // wait for the sound to play and stop the service
        soundHandler = new Handler();
        soundHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                soundPool.stop(cheerSoundId);
                soundPool.stop(booSoundId);
                stopSelf();
            }
        }, SOUND_MAX_DURATION);

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
            Log.i(TAG, "gps connection disabled");
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

    /*- *************************************************************** */
    /*- *************************************************************** */
    private class LocationTimeoutUpdating implements Runnable {

        @Override
        public void run() {

            if (bestLocation != null && bestLocation.getAccuracy() <= LOW_ACCURACY_LIMIT) {
                onLocationObtained();

            } else {
                Log.i(TAG, "Location timeout. Locations obtained are not enough accurate");
                stopRunning(FootingResult.NO_LOCATION_UPDATES);
            }
        }
    }

    /*- *********************************************************** */
    /*- *********************************************************** */
    private class LocationStartUpdating implements Runnable {
        @Override
        public void run() {
            startRequestingLocationUpdates();
        }
    }

    /*- *********************************************************** */
    /*- *********************************************************** */
    private final IBinder binder = new LocationServiceBinder();
    private WeakReference<Client> client;

    @Override
    public IBinder onBind(Intent intent) {
        // init variables from the intent received
        startLocation = intent.getParcelableExtra(C.EXTRA_FIRST_LOCATION);
        previousLocation = startLocation;
        bestLocation = null;
        currentDistance = 0f;
        goalDistance = intent.getIntExtra(C.EXTRA_DISTANCE_IN_METERS, C.DEFAULT_DISTANCE);
        startTime = System.currentTimeMillis();
        // first location time has to be "now" because user starts running now
        previousLocation.setTime(startTime);

        // PendingIntent won't have an attachment, there's no action to perform when user clicks it
        PendingIntent emptyIntent = PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.notification).
                setContentTitle(getString(R.string.notification_is_running_title)).setContentText(getString(R.string.notification_is_running_text)).
                setContentIntent(emptyIntent);
        Notification notification = builder.build();
        startForeground(NOTIFICATION_ID, notification);

        // connect to the LocationClient that is going to request for locations
        locationClient.connect();

        return binder;
    }

    public class LocationServiceBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }

    public void cancelRun() {
        stopRunning(FootingResult.CANCELLED_BY_USER);
    }

    public void setClient(Client client) {
        this.client = new WeakReference<Client>(client);
    }

    public interface Client {
        void onRunningFinished(Bundle extras);
        void onLocationObtained(long time, float meters);
    }

    public void handleOnRunningFinished(Bundle extras) {
        Log.i(TAG, "client is null, create and send running finished intent");
        Intent newIntent = new Intent(this, ResultDetailActivity.class);
        newIntent.putExtras(extras);
        startActivity(newIntent);
    }

}