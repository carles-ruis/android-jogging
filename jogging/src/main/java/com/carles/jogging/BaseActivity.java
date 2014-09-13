package com.carles.jogging;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.carles.jogging.util.PrefUtil;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

/**
 * Created by carles1 on 20/04/14.
 */
public abstract class BaseActivity extends SherlockFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trackActivityCreated();
    }

    private void trackActivityCreated() {
        // Send a screen view and track logged user
        Tracker tracker = EasyTracker.getInstance(this);
        tracker.set(Fields.SCREEN_NAME, this.getClass().getName());
        String username = PrefUtil.getLoggedUser(this) == null ? "NONE" : PrefUtil.getLoggedUser(this).getName();
        tracker.set(Fields.customDimension(C.GA_DIMENSION_USER), username);
        EasyTracker.getInstance(this).send(MapBuilder.createAppView().build());
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        EasyTracker.getInstance(this).activityStart(this);
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        EasyTracker.getInstance(this).activityStop(this);
//    }
}
