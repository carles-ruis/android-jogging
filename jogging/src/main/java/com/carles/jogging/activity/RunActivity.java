package com.carles.jogging.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.carles.jogging.C;
import com.carles.jogging.R;
import com.carles.jogging.dialog.ConnectionFailedDialog;
import com.carles.jogging.dialog.ErrorDialogFragment;
import com.carles.jogging.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Created by carles1 on 20/04/14.
 */
public class RunActivity extends BaseActivity {

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private String sKilometers;
    private Integer meters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_run);

        final Bundle extras = getIntent().getExtras();
        sKilometers = extras.getString(C.EXTRA_KILOMETERS_TEXT);
        meters = extras.getInt(C.EXTRA_METERS);

        final TextView improveText = (TextView)findViewById(R.id.improve_your_time_text);
        improveText.setText(getString(R.string.improve_your_time, sKilometers));

        if (servicesConnected()) {
            checkIfGpsIsActivated();
        }
    }

    private void checkIfGpsIsActivated() {
        Log.i("Check if GPS is activated");

        startCountdown();
    }

    private void startCountdown() {
        Log.i("Start countdown...");
    }

    /*- ************************************************************************************************************** */
    /*- ************************************************************************************************************** */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i("Now google play services is available");
                        /*- at the end device was able to connect */
                        checkIfGpsIsActivated();
                        break;
                    default:
                        Log.i("Google play services remain unavailable");
                        /*- couldn't manage to connect to google play services for location */
                        ConnectionFailedDialog connectionFailedDialog = ConnectionFailedDialog.newInstance();
                        connectionFailedDialog.show(getSupportFragmentManager(), C.TAG_CONNECTION_FAILED_DIALOG);
                }
        }
    }

    private boolean servicesConnected() {
        // Check that Google Play services is available
        Log.i("Checking if google play services are available...");
        int resultCode = GooglePlayServicesUtil.
                isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            Log.i("Google Play services is available.");
            return true;

            // Google Play services was not available for some reason
        } else {
            Log.i("Google Play services is not available.");
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(getSupportFragmentManager(), C.TAG_GOOGLE_PLAY_SERVICES_ERROR_DIALOG);
            }
            return false;
        }
    }

    /*- ************************************************************************************************************** */
    /*- ************************************************************************************************************** */

    private boolean isGpsActivated() {
        return true; //TODO

    }
}
