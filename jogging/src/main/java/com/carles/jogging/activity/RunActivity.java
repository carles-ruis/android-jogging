package com.carles.jogging.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.carles.jogging.C;
import com.carles.jogging.R;
import com.carles.jogging.dialog.ConnectionFailedDialog;
import com.carles.jogging.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Created by carles1 on 20/04/14.
 */
public class RunActivity extends BaseActivity {

    public static final float VOLUME = 1.0f;
    private static final int MAX_SOUND_STREAMS = 1;
    private String sKilometers;
    private Integer meters;
    private Button cancelRunButton;

    private SoundPool soundPool;
    private int startSoundId = -1;
    private int endSoundId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_run);

        final Bundle extras = getIntent().getExtras();
        sKilometers = extras.getString(C.EXTRA_KILOMETERS_TEXT);
        meters = extras.getInt(C.EXTRA_METERS);

        final TextView improveText = (TextView) findViewById(R.id.improve_your_time_text);
        improveText.setText(getString(R.string.improve_your_time, sKilometers));

        cancelRunButton = (Button) findViewById(R.id.cancel_run_button);
        cancelRunButton.findViewById(R.id.cancel_run_button);
        cancelRunButton.setOnClickListener(new CancelRunButtonOnClickListener());

        /*- Suggests an audio stream whose volume should be changed by the hardware volume controls. */
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
       /*- Load the sounds */
        soundPool = new SoundPool(MAX_SOUND_STREAMS, AudioManager.STREAM_MUSIC, 0);
        startSoundId = soundPool.load(this, R.raw.starting_pistol, 1);
        endSoundId = soundPool.load(this, R.raw.crowd_sound, 1);

        getSupportActionBar().setHomeButtonEnabled(false);

        checkServicesConnected();

    }

    private boolean checkServicesConnected() {
        /*- Check that Google Play services is available */
        int resultCode = GooglePlayServicesUtil.
                isGooglePlayServicesAvailable(this);

        if (ConnectionResult.SUCCESS == resultCode) {
            Log.i("Google Play services is available.");
            return true;

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

    private void startCountdown() {
        Log.i("Start countdown...");

        findViewById(R.id.improve_your_time_text).setVisibility(View.VISIBLE);

        /*- handler enqueues messages which will be executed in the UI thread */
        final Handler handler = new Handler();
        handler.postDelayed(new CountdownOnYourMarksThread(), C.COUNTDOWN_STOP_MILLISECONDS);
        handler.postDelayed(new CountdownGetSetThread(), C.COUNTDOWN_STOP_MILLISECONDS * 2);
        handler.postDelayed(new CountdownGoThread(), C.COUNTDOWN_STOP_MILLISECONDS * 3);

    }

    private void showConnectionFailedDialog(String connectionType) {
        ConnectionFailedDialog connectionFailedDialog = ConnectionFailedDialog.newInstance(connectionType);
        connectionFailedDialog.show(getSupportFragmentManager(), C.TAG_CONNECTION_FAILED_DIALOG);
    }

    private void cancelRun() {
        Log.i("Run cancelled");

        // TODO stop service
        // TODO show results
    }

    @Override
    public void onBackPressed() {
        /*- override the back button in order to show confirmation */
        CancelRunDialog cancelRunDialog = new CancelRunDialog();
        cancelRunDialog.show(getSupportFragmentManager(), C.TAG_CANCEL_RUN_DIALOG);
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

            final Dialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            return dialog;

        }
    }

    /*- ************************************************************************************************************** */
    /*- ************************************************************************************************************** */
    private class CancelRunDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(getString(R.string.cancel_run_title));
            builder.setMessage(getString(R.string.cancel_run_msg));

            builder.setPositiveButton(R.string.confirm_cancel_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    RunActivity.this.cancelRun();
                }
            });

            builder.setNegativeButton(R.string.dont_cancel_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dismiss();
                }
            });

            final Dialog dialog = builder.create();
            return dialog;
        }
    }

    /*- ************************************************************************************************************** */
    /*- ************************************************************************************************************** */
    private class CancelRunButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            CancelRunDialog dialog = new CancelRunDialog();
            dialog.show(getSupportFragmentManager(), C.TAG_CANCEL_RUN_DIALOG);
        }
    }

    /*- ************************************************************************************************************** */
    /*- ************************************************************************************************************** */
    private class CountdownOnYourMarksThread extends Thread {
        @Override
        public void run() {
            findViewById(R.id.on_your_marks).setVisibility(View.VISIBLE);
        }
    }

    /*- ************************************************************************************************************** */
    /*- ************************************************************************************************************** */
    private class CountdownGetSetThread extends Thread {
        @Override
        public void run() {
            findViewById(R.id.get_set).setVisibility(View.VISIBLE);
        }
    }

    /*- ************************************************************************************************************** */
    /*- ************************************************************************************************************** */
    private class CountdownGoThread extends Thread {
        @Override
        public void run() {
            findViewById(R.id.go).setVisibility(View.VISIBLE);
            cancelRunButton.setVisibility(View.VISIBLE);

            if (startSoundId != -1) {
                soundPool.play(startSoundId, VOLUME, VOLUME, 1, 0, 1f);
            }

            //        Intent intent = new Intent(this, GetLocationsService.class);
            //        intent.putExtra(C.EXTRA_METERS, meters);
            //        startService(intent);

        }
    }
}
