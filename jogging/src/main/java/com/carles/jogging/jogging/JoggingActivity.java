package com.carles.jogging.jogging;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.view.MenuItem;
import com.carles.jogging.BaseActivity;
import com.carles.jogging.C;
import com.carles.jogging.R;
import com.carles.jogging.service.LocationService;

/**
 * Created by carles1 on 20/04/14.
 */
public class JoggingActivity extends BaseActivity implements JoggingFragment.OnCountdownFinishedListener {

    private static final String TAG = JoggingActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jogging);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_jogging);
    }

    @Override
    public void startGetLocationsService() {
        Intent intent = new Intent(this, LocationService.class);
        intent.putExtras(getIntent().getExtras());
        startService(intent);
    }

    public void cancelRun() {
        Log.i(TAG, "RUNNING WILL BE CANCELLED BY THE USER");
        Intent intent = new Intent(this, LocationService.class);
        intent.putExtra(C.EXTRA_FOOTING_RESULT, FootingResult.CANCELLED_BY_USER);
        startService(intent);
        finish();
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
        /*- override the back button in order to show confirmation */
        CancelRunDialog cancelRunDialog = new CancelRunDialog();
        cancelRunDialog.show(getSupportFragmentManager(), C.TAG_CANCEL_RUN_DIALOG);
    }


}