package com.carles.jogging.jogging.first_location;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import com.actionbarsherlock.view.MenuItem;
import com.carles.jogging.C;
import com.carles.jogging.R;
import com.carles.jogging.common.LocationHelper;
import com.carles.jogging.jogging.JoggingActivity;
import com.carles.jogging.BaseActivity;
import com.carles.jogging.service.FirstLocationService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Created by carles1 on 20/04/14.
 */
public class CheckConnectionsActivity extends BaseActivity implements FirstLocationService.OnFirstLocationResultListener {

    private static final String TAG = CheckConnectionsActivity.class.getName();

    private ProgressDialog dialog;

    private FirstLocationService service;
    private boolean serviceBound = false;
    private ServiceConnection serviceConnection = new FirstLocationServiceConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_connections);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle(R.string.title_check_connections);

        checkServicesConnected();

    }

    private boolean checkServicesConnected() {
        /*- Check that Google Play services is available */
        int resultCode = GooglePlayServicesUtil.
                isGooglePlayServicesAvailable(this);

        if (ConnectionResult.SUCCESS == resultCode) {
            return true;

        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, C.CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                GooglePlayServicesErrorDialogFragment errorFragment = new GooglePlayServicesErrorDialogFragment();
                errorFragment.setDialog(errorDialog);
                /*- no need to use getSupportFragmentManager inside a v4.Fragment */
                errorFragment.show(getSupportFragmentManager(), C.TAG_GOOGLE_PLAY_SERVICES_ERROR_DIALOG);
            }
            return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case C.CONNECTION_FAILURE_RESOLUTION_REQUEST:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        /*- at the end device was able to connect */
                        checkGpsConnected();
                        break;
                    default:
                        Log.i(TAG, "Google Play services not available.");
                        /*- device was not able to connect to google play services */
                        showConnectionFailedDialog(getString(R.string.connection_failed_google));
                        break;
                }
        }
    }

    public void showConnectionFailedDialog(String connectionType) {
        ConnectionFailedDialog connectionFailedDialog = ConnectionFailedDialog.newInstance(connectionType);
        connectionFailedDialog.show(getSupportFragmentManager(), C.TAG_CONNECTION_FAILED_DIALOG);
    }

    private void checkGpsConnected() {
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (enabled) {
            requestForFirstLocation();

        } else {
            Log.i(TAG, "GPS is not activated");
            ActivateGpsDialog dialog = new ActivateGpsDialog();
            dialog.show(getSupportFragmentManager(), C.TAG_ACTIVATE_GPS_DIALOG);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*- check it after onCreate, and also after returning from devices gps settings */
        checkGpsConnected();
    }

    private void requestForFirstLocation() {
        showRequestForFirstLocationProgressDialog();

        Intent intent = new Intent(this, FirstLocationService.class);

        /*- BIND_AUTO_CREATE ties the service lifecycle with the binding */
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        serviceBound = true;
    }

    private void showRequestForFirstLocationProgressDialog() {
        dialog = new ProgressDialog(this);
        dialog.setTitle(R.string.check_connection_progress_title);
        dialog.setMessage(getString(R.string.check_connection_progress_msg));
        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.check_connection_progress_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                finish();
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (serviceBound) {
            unbindService(serviceConnection);
            /*- unset serviceBound because onServiceDisconnected is not called when the service is unbound from code */
            serviceBound = false;
        }

        /*- avoid leaking the dialog when the fragment loses foreground */
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    @Override
    public void onLocationObtained(Location location) {
        /*- destroy the service, stop requesting for  location */
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
        }

        Log.i(TAG, "First location found:" + LocationHelper.toString(location));
        /*- start next activity */
        Intent intent = new Intent(this, JoggingActivity.class);
        intent.putExtras(getIntent().getExtras());
        intent.putExtra(C.EXTRA_FIRST_LOCATION, location);
        /*- when navigating away, the user cannot return to the activity with the back button */
//        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        startActivity(intent);
        finish();
    }

    @Override
    public void onLocationFailed() {
        Log.i(TAG, "first-location-listener. onLocationFailed()");

        /*- destroy the service, stop requesting for  location */
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
        }

        FirstLocationNotObtainedDialog dialog = new FirstLocationNotObtainedDialog();
        dialog.show(getSupportFragmentManager(), C.TAG_FIRST_LOCATION_NOT_OBTAINED);
    }

    /*- ********************************************************************************* */
    /*- ********************************************************************************* */
    @SuppressLint("ValidFragment")
    private static class GooglePlayServicesErrorDialogFragment extends DialogFragment {

        private Dialog mDialog;

        public GooglePlayServicesErrorDialogFragment() {
            super();
            mDialog = null;
        }

        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    /*- ********************************************************************************* */
    /*- ********************************************************************************* */
    private class FirstLocationServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            serviceBound = true;

            FirstLocationService.FirstLocationServiceBinder binder = (FirstLocationService.FirstLocationServiceBinder) iBinder;
            service = binder.getService();

            service.setClient(CheckConnectionsActivity.this);
            service.requestLocation();
        }

        @Override
        /*- onServiceDisconnected is only called when a crash cause a service unbind */
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(TAG, "first-location-service-connection onServiceDisconnected");
            serviceBound = false;
        }
    }

}

