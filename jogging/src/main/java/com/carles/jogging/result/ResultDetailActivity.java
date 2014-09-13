package com.carles.jogging.result;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.carles.jogging.BaseActivity;
import com.carles.jogging.C;
import com.carles.jogging.R;
import com.carles.jogging.jogging.FootingResult;
import com.carles.jogging.model.JoggingModel;
import com.carles.jogging.model.JoggingSQLiteHelper;
import com.carles.jogging.util.PrefUtil;
import com.carles.jogging.util.SystemUtil;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.OpenGraphAction;
import com.facebook.model.OpenGraphObject;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by carles1 on 20/04/14.
 */
public class ResultDetailActivity extends BaseActivity implements ResultDetailFragment.OnLocationClickedListener {

    private static final String TAG = ResultDetailActivity.class.getName();
    private static final String FACEBOOK_OPEN_GRAPH_OBJECT = "run";
    private Context ctx;

    // fragments
    private ResultDetailFragment detailFragment = null;
    private ResultMapFragment mapFragment = null;

    // share with facebook
    private UiLifecycleHelper uiHelper;
    private ShareActionProvider shareActionProvider;

    // sound to notify user that running is over
    private SoundPool soundPool;

    private JoggingModel jogging;
    private List<JoggingModel> partials = new ArrayList<JoggingModel>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_detail);
        ctx = this;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_result);

        // if should play a sound, load it to play it
        if (getIntent().getBooleanExtra(C.EXTRA_SHOULD_PLAY_SOUND, false)) {
            playSoundToNotifyTheUser();
        }

        // save results in the local database if required
        partials = getIntent().<JoggingModel>getParcelableArrayListExtra(C.EXTRA_JOGGING_PARTIALS);
        jogging = getIntent().getParcelableExtra(C.EXTRA_JOGGING_TOTAL);
        if (partials!= null && jogging!=null && !partials.isEmpty() && getIntent().getBooleanExtra(C.EXTRA_SHOULD_SAVE_RUNNING, false)) {
            JoggingSQLiteHelper.getInstance(this).insertJogging(jogging, partials);
        }

        // init helper to user facebook's share dialog
        uiHelper = new UiLifecycleHelper(this, new Session.StatusCallback() {
             @Override
            public void call(Session session, SessionState state, Exception exception) {
                 Log.e("carles","Session.StatusCallback 'call' callback is being performed");

                 Log.e("carles", "session:" + session.toString());
                 Log.e("carles", "state:" + state.toString());
                 Log.e("carles", "exception:"+ exception.getMessage());
                 exception.printStackTrace();
             }
        });

        uiHelper.onCreate(savedInstanceState);

        // we're being restored from a previous state, so the fragments already exist
//        if (savedInstanceState != null) {
//            return;
//        }

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
    protected void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        uiHelper.onPause();
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // handle facebook's ShareDialog callback
        uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {

            @Override
            public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
                Log.e("carles", "FacebookDialog callback onComplete");
                if (FacebookDialog.getNativeDialogDidComplete(data)) {
                    trackSocialInteraction();
                    showShareSuccess();
                    Log.i(TAG, "Shared with facebook via ShareDialog");
                    Log.e("carles", "compartició amb facebook ok");
                } else {
                    showShareFailure();
                    Log.i(TAG, "NOT Shared via ShareDialog. FacebookDialog.getNativeDialogDidComplete returned false");
                    Log.e("carles", "NOT Shared via ShareDialog. FacebookDialog.getNativeDialogDidComplete returned false");
                }
            }

            @Override
            public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
                showShareFailure();
                Log.e(TAG, "NOT shared with facebook via ShareDialog. Error:" + error.getMessage());
                Log.e("carles", "NOT shared with facebook via ShareDialog. Error:" + error.getMessage());
            }
        });
    }

    @Override
    protected void onDestroy() {
        uiHelper.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLocationClicked(int position) {
        addResultMapFragment(position);
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
                return;
            }
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case android.R.id.home:
                    onBackPressed();
                    return true;

//                case R.id.action_share:
//                    return true;

                case R.id.action_facebook:
                    shareWithFacebook();
                    return true;

                case R.id.action_map:
                    addResultMapFragment(-1);
                    return true;

                default:
                    return super.onOptionsItemSelected(item);
            }
    }

    private void shareWithFacebook() {
        // safe check
        if (jogging == null) {
            return;
        }

        Log.e("carles", "about to share with facebook");
        if (FacebookDialog.canPresentOpenGraphActionDialog(getApplicationContext(),
                FacebookDialog.OpenGraphActionDialogFeature.OG_ACTION_DIALOG)) {
            shareWithShareDialog();
        } else {
            shareWithFeedDialog();
        }
    }

    private void shareWithShareDialog() {
        Log.e("carles","share with share dialog");
        OpenGraphObject ogo = OpenGraphObject.Factory.createForPost("jogging:run");
        ogo.setProperty("title",getString(R.string.share_title, (int)jogging.getTotalDistance()));
        ogo.setProperty("description", getString(R.string.share_time, jogging.getTotalTime()));
        ogo.setProperty("url", getString(R.string.play_store_url));

        OpenGraphAction ogAction = GraphObject.Factory.create(OpenGraphAction.class);
        ogAction.setProperty(FACEBOOK_OPEN_GRAPH_OBJECT, ogo);

        String filename = "jogging_" + jogging.getId();

        Log.e("carles","filename will be "+filename);

        Bitmap image= SystemUtil.loadBitmap(filename);
        if (image == null) {
            SystemUtil.takeScreenshot(this, filename);
            Log.e("carles","screenshot was taken");
            image = SystemUtil.loadBitmap(filename);
        }

        if (image != null) {
            Log.e("carles", "bitmap:" + image.toString());
        } else {
            Log.e("carles","bitmap is null");
        }

        FacebookDialog shareDialog;
        if (image == null) {
            shareDialog = new FacebookDialog.OpenGraphActionDialogBuilder(this, ogAction,
                    FACEBOOK_OPEN_GRAPH_OBJECT).build();
            Log.i(TAG, "Unable to load the map that should be shared with facebook");
        } else {
            shareDialog = new FacebookDialog.OpenGraphActionDialogBuilder(this, ogAction,
                    FACEBOOK_OPEN_GRAPH_OBJECT).setImageAttachmentsForObject(FACEBOOK_OPEN_GRAPH_OBJECT,
                    Arrays.asList(image)).build();
        }
        uiHelper.trackPendingDialogCall(shareDialog.present());

        Log.e("carles","pending dialog is being tracked");
    }

    private void shareWithFeedDialog() {
        Log.e("carles", "shareWithFeedDialog");

        Bundle params = new Bundle();
        String user = PrefUtil.getLoggedUser(ctx).getName();
        String appName = getString(R.string.app_name);
        params.putString("name", getString(R.string.share_feed_dialog_name, user, appName));
        params.putString("caption", getString(R.string.share_title, (int) jogging.getTotalDistance()));
        params.putString("description", getString(R.string.share_time, jogging.getTotalTime()));
        params.putString("link", getString(R.string.play_store_url));

        WebDialog feedDialog = (new WebDialog.FeedDialogBuilder(ctx, Session.getActiveSession(), params)).
                setOnCompleteListener(new WebDialog.OnCompleteListener() {

            @Override
            public void onComplete(Bundle values, FacebookException error) {
                if (error == null) {
                    // When the story is posted, echo success and post Id.
                    final String postId = values.getString("post_id");
                    if (postId != null) {
                        Log.i(TAG, "Shared with facebook via FeedDialog. id=" + postId);
                        Log.e("carles", "Shared with facebook via FeedDialog. id=" + postId);
                        trackSocialInteraction();
                        showShareSuccess();
                    } else {
                        // User clicked the Cancel button
                        Log.i(TAG, "NOT shared with facebook. User cancelled");
                        Log.e("carles", "NOT shared with facebook. User cancelled");
                    }
                } else if (error instanceof FacebookOperationCanceledException) {
                    // User clicked the "x" button
                    Log.i(TAG, "NOT shared with facebook. User closed dialog");
                    Log.e("carles", "NOT shared with facebook. User closed dialog");
                } else {
                    // Generic, ex: network error
                    Log.e(TAG, "NOT shared with facebook. Error:" + error.getMessage());
                    Log.e("carles", "NOT shared with facebook. Error:" + error.getMessage());
                    showShareFailure();
                }
            }
        }).build();
        feedDialog.show();

        Log.e("carles", "... ShareWithFeedDialog");
    }

    private void trackSocialInteraction() {
        EasyTracker.getInstance(this).send(MapBuilder.createSocial("Facebook", "Share",
                PrefUtil.getLoggedUser(ctx).getName()+" running").build());
    }

    private void showShareSuccess() {
        Toast.makeText(ResultDetailActivity.this, R.string.facebook_shared, Toast.LENGTH_LONG).show();
    }

    private void showShareFailure() {
        Toast.makeText(ResultDetailActivity.this, R.string.facebook_not_shared,Toast.LENGTH_LONG).show();
    }

}
