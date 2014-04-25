package com.carles.jogging.activity;

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
import com.carles.jogging.adapter.DrawerOption;
import com.carles.jogging.fragment.BestTimesFragment;
import com.carles.jogging.fragment.LastTimesFragment;
import com.carles.jogging.fragment.MainFragment;
import com.carles.jogging.fragment.NavigationDrawerFragment;

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
        /*- Handle action bar item clicks here. The action bar will automatically handle clicks on the Home/Up button, so long as you specify a parent activity in AndroidManifest.xml. */
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_activity_to_left_in, R.anim.slide_activity_to_left_out);
            return true;
        } else {
        /*- fragment will handle the action bar items */
            return super.onOptionsItemSelected(item);
        }
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

    /*- ************************************************************************************************************** */
    /*- ************************************************************************************************************** */
    @Override
    public void onNavigationDrawerItemSelected(int position) {

        DrawerOption option = DrawerOption.values()[position];
        Fragment fragment = new Fragment();
        String title = getString(option.titleId);

        switch (option) {
            case NEW:
                fragment = MainFragment.newInstance(title);
                break;
            case BEST_TIMES:
                fragment = BestTimesFragment.newInstance(title);
                break;
            case LAST_TIMES:
                fragment = LastTimesFragment.newInstance(title);
                break;
        }

        /*- Insert the fragment by replacing any existing fragment */
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
        /*- do i want to add the fragments into the back stack to navigate to them when back button is pressed? */
        //        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).addToBackStack(C.MAIN_ACTIVIY_BS).commit();
    }

}