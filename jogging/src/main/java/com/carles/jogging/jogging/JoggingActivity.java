package com.carles.jogging.jogging;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import com.actionbarsherlock.view.MenuItem;
import com.carles.jogging.BaseActivity;
import com.carles.jogging.R;
import com.carles.jogging.result.ResultDetailActivity;
import com.carles.jogging.service.LocationService;

/**
 * Created by carles1 on 20/04/14.
 */
public class JoggingActivity extends BaseActivity implements JoggingFragment.Callbacks, LocationService.Client {

    private static final String TAG = JoggingActivity.class.getName();

    private static final String TAG_CANCEL_RUN_DIALOG = "tag_cancel_run_dialog";

    private LocationService service;
    private boolean isServiceBound = false;
    private LocationServiceConnection serviceConnection = new LocationServiceConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jogging);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_jogging);

        // bind the service to the activity without impacting in the priority
        // of the target service's hosting process
        bindService(new Intent(this,LocationService.class), serviceConnection, Context.BIND_WAIVE_PRIORITY);
    }

    @Override
    protected void onDestroy() {
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }
        super.onDestroy();
    }

    @Override
    public void cancelRun() {
        CancelRunDialog cancelRunDialog = new CancelRunDialog();
        cancelRunDialog.show(getSupportFragmentManager(), TAG_CANCEL_RUN_DIALOG);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // override the back button in order to show confirmation
        CancelRunDialog cancelRunDialog = new CancelRunDialog();
        cancelRunDialog.show(getSupportFragmentManager(), TAG_CANCEL_RUN_DIALOG);
    }

    /*- ********************************************************************************* */
    /*- ********************************************************************************* */
    class CancelRunDialog extends DialogFragment {

        public CancelRunDialog() {}

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(getString(R.string.cancel_run_title));
            builder.setMessage(getString(R.string.cancel_run_msg));

            builder.setPositiveButton(R.string.cancel_run_button_yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Activity parent = getActivity();
                    if (isServiceBound) {
                        service.cancelRun();
                    } else {
                        Log.e(TAG, "Service is not bound");
                    }
                }
            });

            builder.setNegativeButton(R.string.cancel_run_button_no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dismiss();
                }
            });

            final Dialog dialog = builder.create();
            return dialog;
        }
    }

    /*- ********************************************************************************* */
    /*- ********************************************************************************* */
    private class LocationServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            LocationService.LocationServiceBinder binder = (LocationService.LocationServiceBinder) iBinder;
            service = binder.getService();
            service.setClient(JoggingActivity.this);
            service.start(getIntent());
            isServiceBound = true;
        }

        @Override
        /*- onServiceDisconnected is only called when a crash cause a service unbind */
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(TAG, "FirstLocationServiceConnection onServiceDisconnected");
            isServiceBound = false;
        }
    }

    @Override
    public void startGetLocationsService() {
        if (isServiceBound) {
            service.start(getIntent());
        } else {
            Log.e(TAG, "LocationService is not bound");
        }
    }

    @Override
    public void onRunningFinished(Intent intent) {
        if (isServiceBound) {
            Intent newIntent = new Intent(this, ResultDetailActivity.class);
            newIntent.putExtras(getIntent().getExtras());
            startActivity(newIntent);
            finish();
        } else {
            Log.e(TAG, "LocationService is not bound");
        }
    }

}