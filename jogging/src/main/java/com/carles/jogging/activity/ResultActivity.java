package com.carles.jogging.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import com.carles.jogging.C;
import com.carles.jogging.R;
import com.carles.jogging.fragment.ResultDetailFragment;
import com.carles.jogging.fragment.ResultMapFragment;
import com.carles.jogging.helper.LocationHelper;
import com.carles.jogging.model.PartialJogging;

import java.util.ArrayList;

/**
 * Created by carles1 on 20/04/14.
 */
public class ResultActivity extends BaseActivity {

    private ResultDetailFragment detailFragment = null;
    private ResultMapFragment mapFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_result);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_result);

        /*- we're being restored from a previous state, so the fragments already exist */
        if (savedInstanceState != null) {
            return;
        }

        if (detailFragment == null) {
            detailFragment = ResultDetailFragment.newInstance();
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, detailFragment).commit();
    }

    public void addResultMapFragment() {
        if(mapFragment == null) {
            mapFragment = ResultMapFragment.newInstance();
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mapFragment).addToBackStack(null).commit();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            /*- if it comes from LocationService, it's the only activity in the backstack*/
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else{
            /*- it comes from another activity, go back to previous one in the back stack */
            super.onBackPressed();
        }
    }
}
