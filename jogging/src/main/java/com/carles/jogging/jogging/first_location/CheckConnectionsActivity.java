package com.carles.jogging.jogging.first_location;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.support.v4.app.DialogFragment;
import android.util.Log;

import com.carles.jogging.BaseActivity;
import com.carles.jogging.C;
import com.carles.jogging.R;
import com.carles.jogging.jogging.JoggingActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Created by carles1 on 20/04/14.
 */
public class CheckConnectionsActivity extends BaseActivity implements FirstLocationService.OnFirstLocationResultListener {

    private static final String TAG = CheckConnectionsActivity.class.getSimpleName();

    private ProgressDialog dialog;

    // Use to control if this activity is in the foreground
    private boolean isActive = true;
    // Error that must be shown if a onLocationFailed was received while activity was not visible
    private Error errorPending = null;

    private FirstLocationService service;
    private boolean isServiceBound = false;
    private ServiceConnection serviceConnection = new FirstLocationServiceConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_connections);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle(R.string.title_check_connections);

        if (checkServicesConnected()) {
            checkGpsConnected();
        }

    }

    private boolean checkServicesConnected() {
        /*- Check that Google Play services is available */
        int resultCode = GooglePlayServicesUtil.
                isGooglePlayServicesAvailable(this);

        if (ConnectionResult.SUCCESS == resultCode) {
            return true;

        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, C.REQ_CODE_GOOGLE_CONNECTION_FAILURE);

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
        // avoids IllegalStateException when showing ActivateGpsDialog for the second time
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case C.REQ_CODE_GOOGLE_CONNECTION_FAILURE:
                if (resultCode == Activity.RESULT_OK) {
                        /*- at the end device was able to connect */
                    checkGpsConnected();
                } else {
                    Log.i(TAG, "Google Play services not available.");
                        /*- device was not able to connect to google play services */
                    FirstLocationFailedDialog.newInstance(Error.GOOGLE_PLAY_SERVICES_UNAVAILABLE).
                            show(getSupportFragmentManager(), C.TAG_CONNECTION_FAILED_DIALOG);
                }
                break;
            case C.REQ_CODE_ENABLE_GPS:
                checkGpsConnected();
                break;
        }
    }

    private void checkGpsConnected() {
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (enabled) {
            showProgressDialog();
            Intent intent = new Intent(this, FirstLocationService.class);

            // BIND_AUTO_CREATE ties the service lifecycle with the binding
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            isServiceBound = true;

        } else {
            Log.i(TAG, "GPS is not activated");
            ActivateGpsDialog dialog = new ActivateGpsDialog();
            dialog.show(getSupportFragmentManager(), C.TAG_ACTIVATE_GPS_DIALOG);
        }
    }

    private void showProgressDialog() {
        dialog = new ProgressDialog(this);
        dialog.setTitle(R.string.progress_waiting);
        dialog.setMessage(getString(R.string.check_connection_progress_msg));
        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.progress_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                finish();
                overridePendingTransition(R.anim.slide_activity_to_right_in, R.anim.slide_activity_to_right_out);
            }
        });

        // dialog is not cancelable with the back key, only with the cancel button
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        isActive = true;
        if (errorPending != null) {
            showFirstLocationFailedDialog(errorPending);
            errorPending = null;
        }
    }

    @Override
    protected void onStop() {
        isActive = false;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (isServiceBound) {
            unbindService(serviceConnection);
            /*- unset isServiceBound because onServiceDisconnected is not called when the service is unbound from code */
            isServiceBound = false;
        }

        /*- avoid leaking the dialog when the fragment loses foreground */
        if (dialog != null) {
            dialog.dismiss();
        }

        super.onDestroy();
    }

    @Override
    public void onLocationObtained(Location location) {
        Log.i(TAG, "First location obtained. Accuracy " + location.getAccuracy());

        // destroy the service, stop requesting for location
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }

        Intent intent = new Intent(this, JoggingActivity.class);
        intent.putExtras(getIntent().getExtras());
        intent.putExtra(C.EXTRA_FIRST_LOCATION, location);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_activity_to_left_in, R.anim.slide_activity_to_left_out);
    }

    @Override
    public void onLocationFailed(Error error) {
        Log.i(TAG, "First location failed");

        /*- destroy the service, stop requesting for  location */
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }

        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }

        // if activity is not visible when receiving a callback, don't show dialog because the
        // fragment transaction will cause
        // java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
        if (isActive) {
            showFirstLocationFailedDialog(error);
        } else {
            errorPending = error;
        }
    }

    private void showFirstLocationFailedDialog(Error error) {
        FirstLocationFailedDialog.newInstance(error).show(getSupportFragmentManager(),
                C.TAG_FIRST_LOCATION_NOT_OBTAINED);
        getSupportFragmentManager().executePendingTransactions();
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

        /*- a dialog is cancelled when the user presses the back button*/
        public void onCancel(DialogInterface dialog) {
            getActivity().finish();
            getActivity().overridePendingTransition(R.anim.slide_activity_to_right_in, R.anim.slide_activity_to_right_out);
        }

    }

    /*- ********************************************************************************* */
    /*- ********************************************************************************* */
    private class FirstLocationServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            isServiceBound = true;

            FirstLocationService.FirstLocationServiceBinder binder = (FirstLocationService.FirstLocationServiceBinder) iBinder;
            service = binder.getService();

            service.setClient(CheckConnectionsActivity.this);
            service.requestLocation();
        }

        @Override
        /*- onServiceDisconnected is only called when a crash cause a service unbind */
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(TAG, "FirstLocationServiceConnection onServiceDisconnected");
            isServiceBound = false;
        }
    }

}

