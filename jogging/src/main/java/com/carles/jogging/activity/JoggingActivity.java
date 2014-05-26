package com.carles.jogging.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.TextView;

import com.carles.jogging.C;
import com.carles.jogging.R;
import com.carles.jogging.dialog.CancelRunDialog;
import com.carles.jogging.service.LocationService;
import com.carles.jogging.util.Log;

/**
 * Created by carles1 on 20/04/14.
 */
public class JoggingActivity extends BaseActivity implements LocationService.OnKilometerRanListener {

    /*- receiver in order to kill the activity when the user has finished running */
    private final BroadcastReceiver joggingFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("joggingFinishedReceiver RECEIVED INTENT IN JOGGING ACTIVITY");
            JoggingActivity.this.finish();
        }
    };

    private String sKilometers;
    private Integer meters;

    /*- ************************************************************************************************************** */
    /*- ************************************************************************************************************** */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jogging);

        getSupportActionBar().setHomeButtonEnabled(false);
    }

    @Override
    public void onBackPressed() {
        /*- override the back button in order to show confirmation */
        CancelRunDialog cancelRunDialog = new CancelRunDialog();
        cancelRunDialog.show(getSupportFragmentManager(), C.TAG_CANCEL_RUN_DIALOG);
    }

    public void startGetLocationsService() {
        Intent intent = new Intent(this, LocationService.class);
        intent.putExtras(getIntent().getExtras());
        startService(intent);
    }

    public void cancelRun() {
        /*- stop the service in order to stop requesting locations */
        stopService(new Intent(this, LocationService.class));

        /*- jogging result will not be shown to the user, so get back to main */
        finish();
    }

    @Override
    protected void onResume() {
        registerReceiver(joggingFinishedReceiver, new IntentFilter(C.ACTION_JOGGING_FINISHED));
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(joggingFinishedReceiver);
        super.onPause();
    }

    /*- unused for the moment */
    @Override
    public void onKilometerRun(int kilometers) {
        TextView kilometersRun = (TextView) findViewById(R.id.jogging_kilometers_run);
        kilometersRun.setText(getString(R.string.jogging_kilometers_run, kilometers));
    }

}

