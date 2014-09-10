package com.carles.jogging.result;

import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.carles.jogging.C;
import com.carles.jogging.R;
import com.carles.jogging.BaseActivity;
import com.carles.jogging.jogging.FootingResult;
import com.carles.jogging.main.MainActivity;
import com.carles.jogging.model.JoggingModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by carles1 on 20/04/14.
 */
public class ResultDetailActivity extends BaseActivity implements ResultDetailFragment.OnLocationClickedListener {

    private static final String TAG = ResultDetailActivity.class.getName();

    private ResultDetailFragment detailFragment = null;
    private ResultMapFragment mapFragment = null;

    private ShareActionProvider shareActionProvider;

    // sound to notify user that running is over
    private SoundPool soundPool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_detail);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_result);

        // if should play a sound, load it
        if (getIntent().getBooleanExtra(C.EXTRA_SHOULD_PLAY_SOUND, false)) {
            playSoundToNotifyTheUser();
        }

        // we're being restored from a previous state, so the fragments already exist
        if (savedInstanceState != null) {
            return;
        }

        // show detail fragment as is the ResultDetailActivity's initial fragment
        if (detailFragment == null) {
            detailFragment = ResultDetailFragment.newInstance();
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, detailFragment).commit();
    }

    private void playSoundToNotifyTheUser() {
        soundPool = new SoundPool(C.MAX_SOUND_STREAMS, AudioManager.STREAM_MUSIC, 0);
        if (getIntent().getSerializableExtra(C.EXTRA_FOOTING_RESULT) == FootingResult.SUCCESS) {
            soundPool.load(this, R.raw.sound_crowd_cheering, 1);
        } else {
            soundPool.load(this, R.raw.alert_stop_footing, 1);
        }

        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                // check if loading the sound was successful (status == 0)
                if (status == 0) {
                    soundPool.play(sampleId, C.VOLUME, C.VOLUME, 1, 0, 1f);
                }
            }
        });
    }

    @Override
    public void onLocationClicked(int position) {
        addResultMapFragment(position+1);
    }

    private void addResultMapFragment(int position) {
        if (mapFragment == null) {
            ArrayList<JoggingModel> partials = getIntent().<JoggingModel>getParcelableArrayListExtra(C.EXTRA_JOGGING_PARTIALS);
            mapFragment = ResultMapFragment.newInstance(position, partials);
        } else {
            mapFragment.setPosition(position);
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (mapFragment.isAdded()) {
            ft.show(mapFragment);
        } else {
            ft.add(R.id.fragment_container, mapFragment);
        }
        ft.hide(detailFragment);
        ft.commit();
    }

    @Override
    public void onBackPressed() {

        if (mapFragment != null && mapFragment.isAdded() && mapFragment.isVisible()) {
            if (detailFragment != null) {
                // if we are showing the map, re-show results detail fragment
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.show(detailFragment).hide(mapFragment).commit();
            }

        } else if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            // if it comes from LocationService it's the only activity in the backstack
            // open MainActivity and finish this one
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();

        } else{
            // it comes from another activity, go back to previous one in the back stack
            super.onBackPressed();
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getSupportMenuInflater().inflate(R.menu.menu_result_detail, menu);
//        MenuItem shareItem = menu.findItem(R.id.action_share);
//        shareActionProvider = (ShareActionProvider) shareItem.getActionProvider();
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case android.R.id.home:
                    onBackPressed();
                    return true;

                case R.id.action_share:
                    // TODO shareActionProvider
                    return true;

                case R.id.action_map:
                    addResultMapFragment(0);
                    return true;

                default:
                    return super.onOptionsItemSelected(item);
            }
    }

}
