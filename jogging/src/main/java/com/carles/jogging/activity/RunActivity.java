package com.carles.jogging.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.widget.TextView;

import com.carles.jogging.C;
import com.carles.jogging.R;
import com.carles.jogging.dialog.ConnectionFailedDialog;
import com.carles.jogging.service.GetLocationsService;
import com.carles.jogging.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Created by carles1 on 20/04/14.
 */
public class RunActivity extends BaseActivity {

    private String sKilometers;
    private Integer meters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_run);

        final Bundle extras = getIntent().getExtras();
        sKilometers = extras.getString(C.EXTRA_KILOMETERS_TEXT);
        meters = extras.getInt(C.EXTRA_METERS);

        final TextView improveText = (TextView) findViewById(R.id.improve_your_time_text);
        improveText.setText(getString(R.string.improve_your_time, sKilometers));

        servicesConnected();

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
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, C.CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                GooglePlayServicesErrorDialogFragment errorFragment = new GooglePlayServicesErrorDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(getSupportFragmentManager(), C.TAG_GOOGLE_PLAY_SERVICES_ERROR_DIALOG);
            }
            return false;
        }
    }

    private void gpsConnected() {
        Log.i("Check if GPS is activated");

        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (enabled) {
            Log.i("GPS is activated");
            startCountdown();

        } else {
            Log.i("GPS is not activated");
            ActivateGpsDialogFragment dialog = new ActivateGpsDialogFragment();
            dialog.show(getSupportFragmentManager(), C.TAG_ACTIVATE_GPS_DIALOG);

        }
    }

    private void startCountdown() {
        Log.i("Start countdown...");

        // TODO countdown

        Intent intent = new Intent(this, GetLocationsService.class);
        intent.putExtra(C.EXTRA_METERS, meters);
        startService(intent);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case C.CONNECTION_FAILURE_RESOLUTION_REQUEST:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i("Now google play services is available");
                        /*- at the end device was able to connect */
                        gpsConnected();
                        break;
                    default:
                        Log.i("Google play services remain unavailable");
                        /*- device was not able to connect to google play services */
                        showConnectionFailedDialog(getString(R.string.connection_google));
                        break;
                }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*- check it after onCreate, and also after returning from devices gps settings */
        gpsConnected();
    }

    private void showConnectionFailedDialog(String connectionType) {
        ConnectionFailedDialog connectionFailedDialog = ConnectionFailedDialog.newInstance(connectionType);
        connectionFailedDialog.show(getSupportFragmentManager(), C.TAG_CONNECTION_FAILED_DIALOG);
    }

    /*- ************************************************************************************************************** */
    /*- ************************************************************************************************************** */
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

    /*- ************************************************************************************************************** */
    /*- ************************************************************************************************************** */
    private class ActivateGpsDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.gps_disabled_title));
            builder.setMessage(getString(R.string.gps_disabled_msg));

            builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Show location settings when the user acknowledges the alert dialog
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    //                    startActivityForResult(intent, GPS_ACTIVATION_REQUEST);
                    startActivity(intent);
                }
            });

            builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    /*- user doesn't want to activate gps */
                    RunActivity.this.showConnectionFailedDialog(getString(R.string.connection_gps));
                }
            });

            Dialog alertDialog = builder.create();

            alertDialog.setCanceledOnTouchOutside(false);
            return alertDialog;

        }
    }
}
