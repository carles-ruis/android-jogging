package com.carles.jogging.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.carles.jogging.C;
import com.carles.jogging.R;
import com.carles.jogging.jogging.FootingResult;
import com.carles.jogging.model.JoggingModel;
import com.carles.jogging.model.UserModel;
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
public class LocationService extends Service implements GpsConnectivityObserver, GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = LocationService.class.getName();
    private static final int NOTIFICATION_ID = 1;

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

    private List<Float> accuracies = new ArrayList<Float>(); // TODO delete

    @Override
    public void onCreate() {
          // request wakelock to avoid the device goes to sleep and misses location updates
        acquireWakelock();

        // subscribe to gps connectivity changes
        GpsConnectivityManager.instance(this).addObserver(this);

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
        Log.e("carles","onStartCommand");

        // init variables from the intent received
        startLocation = intent.getParcelableExtra(C.EXTRA_FIRST_LOCATION);
        previousLocation = startLocation;
        bestLocation = null;
        currentDistance = 0f;
        totalDistance = intent.getIntExtra(C.EXTRA_DISTANCE_IN_METERS, C.DEFAULT_DISTANCE);
        totalDistanceText = intent.getStringExtra(C.EXTRA_DISTANCE_TEXT);
        startTime = System.currentTimeMillis();
        // first location time has to be "now" because user starts running now
        previousLocation.setTime(startTime);

        // PendingIntent won't have an attachment, there's no action to perform when user clicks it
        PendingIntent emptyIntent = PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_notification_is_running).
                setContentTitle(getString(R.string.notification_is_running_title)).setContentText(getString(R.string.notification_is_running_text)).setContentIntent(emptyIntent);
        Notification notification = builder.build();
        Log.e("carles", "service is going to start in foreground");
        startForeground(NOTIFICATION_ID, notification);

        // connect to the LocationClient that is going to request for locations
        locationClient.connect();

        // if the system kills the service don't bother recreating it
        return START_NOT_STICKY;
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
        accuracies.add(location.getAccuracy()); // TODO delete

        // check if this is the first location or the best accurated location
        if (bestLocation == null || location.getAccuracy() <= bestLocation.getAccuracy()) {
           /*- ignore repeated locations */
            if (location.distanceTo(previousLocation) > 0.0f) {
                bestLocation = location;
            }

        /*- check if we should keeping requesting location updates */
            if (bestLocation == null || System.currentTimeMillis() < stopRequestingTime) {
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

        // update the view with the distance ran and time passed
        client.get().onLocationObtained(totalTime, currentDistance);
        //        Intent intent = new Intent(C.ACTION_UPDATE_KILOMETERS_RUN);
        //        intent.putExtra(C.EXTRA_FOOTING_TIME_TEXT, FormatUtil.time(totalTime));
        //        intent.putExtra(C.EXTRA_DISTANCE_IN_METERS, (int)currentDistance);
        //        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        // check if running is over
        if (currentDistance < totalDistance) {
            handler.postDelayed(locationStartUpdating, TIME_BETWEEN_REQUESTS);

        } else {
            stopRunning(FootingResult.SUCCESS);
        }

    }

    private void stopRunning(FootingResult footingResult) {
        // prepare intent with the results
        Log.e("carles", "stop running with footingResult=" + footingResult.toString());
        Bundle extras = new Bundle();
        extras.putSerializable(C.EXTRA_FOOTING_RESULT, footingResult);

        if (partials.size() > 0) {
            UserModel user = PrefUtil.getLoggedUser(this);
            extras.putParcelableArrayList(C.EXTRA_JOGGING_PARTIALS, (ArrayList<JoggingModel>) partials);
            extras.putParcelable(C.EXTRA_JOGGING_TOTAL, new JoggingModel(partials, totalDistance, footingResult, user));
        }

        if (footingResult == FootingResult.SUCCESS) {
            // only successful joggings will be saved
            extras.putBoolean(C.EXTRA_SHOULD_SAVE_RUNNING, true);
        }

        if (footingResult != FootingResult.CANCELLED_BY_USER) {
            // notify user with a sound
            extras.putBoolean(C.EXTRA_SHOULD_PLAY_SOUND, true);
        }

        // the activity will start a new task
        //        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);

        if (extras.getSerializable(C.EXTRA_FOOTING_RESULT)==null) {
            Log.e("carles","before calling onrunningfinished extra footing result is null");
        } else {
            Log.e("carles", "before calling onrunningfinished extra footing result is " + extras.getSerializable(C.EXTRA_FOOTING_RESULT));
        }
            client.get().onRunningFinished(extras);
            //        startActivity(intent);

            //        } else {
            //            PendingIntent pintent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            //
            //            String notiTitle = "";
            //            String notiText = "";
            //            Uri sound = null;
            //            if (footingResult == FootingResult.SUCCESS) {
            //                notiTitle = getString(R.string.footing_result_success_title);
            //                notiText = getString(R.string.footing_result_success);
            //                sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sound_crowd_cheering);
            //
            //            } else {
            //                notiTitle = getString(R.string.footing_result_failure_title);
            //                int resId = getResources().getIdentifier(footingResult.getResourceId(), "string", getPackageName());
            //                notiText = getString(getResources().getIdentifier(footingResult.getResourceId(), "string", getPackageName()));
            //                sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.alert_stop_footing);
            //            }
            //
            //            if (StringUtils.isBlank(notiText) || StringUtils.isBlank(notiTitle) || sound == null) {
            //                return;
            //            }
            //
            //            NotificationCompat.Builder builder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_notification_stop_running).
            //                    setContentTitle(notiTitle).setContentText(notiText).setContentIntent(pintent).setSound(sound);
            //            Notification notification = builder.build();
            //            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            //            final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            //            notificationManager.notify(NOTIFICATION_ID, notification);
            //        }

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
                Log.i(TAG, "LIST OF ACCURACIES OBTAINED (NOT ENOUGH) = " + accuracies.toString());
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
    private WeakReference<LocationService.Client> client;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocationServiceBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }

    public void start(Intent intent) {
        startService(intent);
    }

    public void cancelRun() {
        stopRunning(FootingResult.CANCELLED_BY_USER);
    }

    public void setClient(LocationService.Client client) {
        this.client = new WeakReference<Client>(client);
    }

    public interface Client {
        void onRunningFinished(Bundle extras);
        void onLocationObtained(long time, float meters);
    }
}