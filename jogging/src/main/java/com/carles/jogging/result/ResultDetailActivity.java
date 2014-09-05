package com.carles.jogging.result;

import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.carles.jogging.R;
import com.carles.jogging.BaseActivity;
import com.carles.jogging.main.MainActivity;

/**
 * Created by carles1 on 20/04/14.
 */
public class ResultDetailActivity extends BaseActivity {

    private ResultDetailFragment detailFragment = null;
    private ResultMapFragment mapFragment = null;

    private ShareActionProvider shareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_detail);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportMenuInflater().inflate(R.menu.menu_result_detail, menu);
        MenuItem shareItem = menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider) shareItem.getActionProvider();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case android.R.id.home:
                    onBackPressed();
                    return true;

                case R.id.action_share:
                    // TODO shareActionProvider
                    return true;

                default:
                    return super.onOptionsItemSelected(item);
            }
    }
}
