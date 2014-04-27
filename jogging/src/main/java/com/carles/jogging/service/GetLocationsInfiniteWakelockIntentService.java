package com.carles.jogging.service;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

import com.carles.jogging.C;
import com.carles.jogging.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import tomtasche.InfiniteWakelockIntentService;

/**
 * Created by carles1 on 21/04/14.
 */
public class GetLocationsInfiniteWakelockIntentService extends InfiniteWakelockIntentService implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {

    private static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    private static final int MILLISECONDS_PER_SECOND = 1000;
    private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

    private boolean isFinished = false;

    private long startTime;
    private double currentDistance;
    private double totalDistance;

    private LocationRequest mLocationRequest;
    private LocationClient mLocationClient;
    private Location previousLocation;

    public GetLocationsInfiniteWakelockIntentService() {
        super(C.GET_LOCATIONS_SERVICE_NAME);
    }

    @Override
    protected boolean isFinished() {
        return isFinished;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        totalDistance = Double.valueOf(intent.getIntExtra(C.EXTRA_DISTANCE_IN_METERS, C.DEFAULT_DISTANCE));
        startTime = System.currentTimeMillis();
        currentDistance = 0.0f;

        Log.i("TOTAL DISTANCE " + totalDistance);
        Log.i("START TIME" + startTime);
        Log.i("CURRENT DISTANCE" + currentDistance);

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        mLocationClient = new LocationClient(this, this, this);
        mLocationClient.connect();

    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i("Google play services status: connected");

        previousLocation = mLocationClient.getLastLocation();

        mLocationClient.requestLocationUpdates(mLocationRequest, this);
    }

    @Override
    public void onDisconnected() {
        Log.i("Google play services status: disconnected");
    }

    @Override
    public void onLocationChanged(Location location) {

        long timeElapsed; // TODO delete
        timeElapsed = System.currentTimeMillis() - startTime;
        double timeElapsedInSeconds = timeElapsed / 1000.0f;

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        double distance = location.distanceTo(previousLocation);

        Log.i("Location change at time : " + timeElapsedInSeconds);
        Log.i("Location position : " + location.getLatitude() + "," + location.getLongitude());
        Log.i("Location accuracy : " + location.getAccuracy());
        Log.i("Location time : " + location.getTime());

        /*- set the new location as the reference location */
        previousLocation = location;
        currentDistance += distance;

        Log.i("LAST DISTANCE: " + distance);
        Log.i("CURRENT DISTANCE: " + currentDistance);

        if (currentDistance > totalDistance) {
            Log.i("DISTANCE OK!!");
            stopRequestingForLocations();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("Google play services status: connection failed");
    }

    private void stopRequestingForLocations() {
        if (mLocationClient.isConnected()) {
            mLocationClient.removeLocationUpdates(this);
        }
        mLocationClient.disconnect();

        isFinished = true;

        // TODO sendBroadcast
    }
}
