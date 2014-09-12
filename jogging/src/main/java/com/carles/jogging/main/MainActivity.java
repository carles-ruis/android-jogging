package com.carles.jogging.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.carles.jogging.R;
import com.carles.jogging.best_times.BestTimesFragment;
import com.carles.jogging.last_times.LastTimesContentFragment;
import com.carles.jogging.login.LoginActivity;
import com.carles.jogging.util.PrefUtil;

/**
 * Created by carles1 on 20/04/14.
 */
public class MainActivity extends SherlockFragmentActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private Context ctx;

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;
    private DrawerLayout drawerLayout;
    private View navigationDrawerView;

    private int navigationMode = ActionBar.NAVIGATION_MODE_STANDARD;

    // options for the distances selector
    private String[] distanceEntries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ctx = this;

        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        navigationDrawerView = findViewById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        // this allows to override the drawer behaviour when back button is pressed
        drawerLayout.setFocusableInTouchMode(false);
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        // prepare navigation list for navigation between LastTimesFragment instances
        setUpNavigationList();
    }

    private void setUpNavigationList() {
        final String[] distanceEntries = getResources().getStringArray(R.array.main_entries_kms);
        this.distanceEntries = distanceEntries;
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.main_entries_kms, R.layout.sherlock_spinner_dropdown_item);

        ActionBar.OnNavigationListener callback = new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                Fragment fragment = LastTimesContentFragment.newInstance();
                getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
                return true;
            }
        };
        getSupportActionBar().setListNavigationCallbacks(adapter, callback);
    }

    public void setActionBarTitle(String title) {
        mTitle = title;
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {

        DrawerOption option = DrawerOption.values()[position];
        Fragment fragment = new Fragment();
        String title = getString(option.titleId);

        switch (option) {
            case NEW:
                navigationMode = ActionBar.NAVIGATION_MODE_STANDARD;
                fragment = MainFragment.newInstance();
                break;
            case BEST_TIMES:
                navigationMode = ActionBar.NAVIGATION_MODE_STANDARD;
                fragment = BestTimesFragment.newInstance();
                break;
            case LAST_TIMES:
                navigationMode = ActionBar.NAVIGATION_MODE_LIST;
                fragment = LastTimesContentFragment.newInstance();
                break;
        }

        // Insert the fragment by replacing any existing fragment
        getSupportActionBar().setNavigationMode(navigationMode);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
        setActionBarTitle(title);
    }

    public void actionLogout(View view) {
        PrefUtil.removeLoggedUserFromPrefs(ctx);
        startActivity(new Intent(ctx, LoginActivity.class));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            getSupportMenuInflater().inflate(R.menu.main, menu);
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(mTitle);            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // fragment will handle the action bar items
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // change the natural navigation to show the drawer when the back button is clicked
        if (mNavigationDrawerFragment.isDrawerOpen()) {
            finish();
        } else {
            drawerLayout.openDrawer(navigationDrawerView);
        }
    }

}