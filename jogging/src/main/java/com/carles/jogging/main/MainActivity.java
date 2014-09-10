package com.carles.jogging.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.view.View;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.carles.jogging.R;
import com.carles.jogging.best_times.BestTimesFragment;
import com.carles.jogging.last_times.LastTimesFragment;

/**
 * Created by carles1 on 20/04/14.
 */
public class MainActivity extends SherlockFragmentActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;
    private DrawerLayout drawerLayout;
    private View navigationDrawerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        navigationDrawerView = findViewById(R.id.navigation_drawer);
        mTitle = getTitle();

        /*- Set up the drawer. */
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        /*- this allows to override the drawer behaviour when back button is pressed */
        drawerLayout.setFocusableInTouchMode(false);

        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            /*- Only show items in the action bar relevant to this screen */
            getSupportMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // fragment will handle the action bar items */
        return super.onOptionsItemSelected(item);
    }

    public void setActionBarTitle(String title) {
        mTitle = title;
    }

    @Override
    public void onBackPressed() {
        /*- change the natural navigation to show the drawer when the back button is closed */
        if (mNavigationDrawerFragment.isDrawerOpen()) {
            finish();
        } else {
            drawerLayout.openDrawer(navigationDrawerView);
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {

        DrawerOption option = DrawerOption.values()[position];
        Fragment fragment = new Fragment();
        String title = getString(option.titleId);

        switch (option) {
            case NEW:
                fragment = MainFragment.newInstance();
                break;
            case BEST_TIMES:
                fragment = BestTimesFragment.newInstance();
                break;
            case LAST_TIMES:
                fragment = LastTimesFragment.newInstance();
                break;
        }

        /*- Insert the fragment by replacing any existing fragment */
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
        setActionBarTitle(title);

    }

}