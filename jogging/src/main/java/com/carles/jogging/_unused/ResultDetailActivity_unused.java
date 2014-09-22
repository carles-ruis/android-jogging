package com.carles.jogging._unused;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.actionbarsherlock.view.MenuItem;
import com.carles.jogging.BaseActivity;
import com.carles.jogging.C;
import com.carles.jogging.R;
import com.carles.jogging.model.JoggingModel;
import com.carles.jogging.result.ResultDetailFragment;
import com.carles.jogging.result.ResultMapFragment;
import com.carles.jogging.util.FormatUtil;
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
import java.util.List;

/**
 * Created by carles1 on 20/04/14.
 */
public class ResultDetailActivity_unused extends BaseActivity implements ResultDetailFragment.OnLocationClickedListener {

    private static final String TAG = ResultDetailActivity_unused.class.getSimpleName();

    private static final String TAG_FACEBOOK_RESPONSE = "tag_facebook_response";

    private static final String FACEBOOK_TYPE = "com_carles_jogging:run";
    private static final String FACEBOOK_ACTION = "com_carles_jogging:go_for";
    private static final String FACEBOOK_PROPERTY_NAME = "run";

    private Context ctx;

    // fragments
    private ResultDetailFragment detailFragment = null;
    private ResultMapFragment mapFragment = null;

    // share with facebook
    private UiLifecycleHelper uiHelper;
    private ProgressDialog progress;

    private JoggingModel jogging;
    private List<JoggingModel> partials = new ArrayList<JoggingModel>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_detail);
        ctx = this;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_result);

         // save results in the local database if required
        partials = getIntent().<JoggingModel>getParcelableArrayListExtra(C.EXTRA_JOGGING_PARTIALS);
        jogging = getIntent().getParcelableExtra(C.EXTRA_JOGGING_TOTAL);

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
                dismissProgressDialog();

                if (FacebookDialog.getNativeDialogDidComplete(data)) {
                    // shared with facebook
                    trackSocialInteraction();
                    showShareSuccess();
                    Log.i(TAG, "Shared with facebook via ShareDialog");
                    Log.e("carles", "compartició amb facebook ok");

                } else {
                    // dialog cancelled
                    Log.i(TAG, "NOT Shared via ShareDialog. FacebookDialog.getNativeDialogDidComplete returned false");
                    Log.e("carles", "NOT Shared via ShareDialog. FacebookDialog.getNativeDialogDidComplete returned false");
                }
            }

            @Override
            public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
                dismissProgressDialog();

                showShareFailure(error.getMessage());
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
        overridePendingTransition(R.anim.slide_activity_to_right_in, R.anim.slide_activity_to_right_out);
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
                    Log.e("carles","action facebook selected");
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

        String filename = "jogging_" + jogging.getId();

        Log.e("carles","filename will be "+filename);

        Bitmap image= SystemUtil.loadBitmap(filename);
        if (image == null) {
            SystemUtil.takeScreenshot(this, filename);
            Log.e("carles","screenshot was taken");
            image = SystemUtil.loadBitmap(filename);
        }

        OpenGraphObject ogo = OpenGraphObject.Factory.createForPost(FACEBOOK_TYPE);
        ogo.setProperty("title",getString(R.string.share_title, (int)jogging.getTotalDistance()));
        ogo.setProperty("description", getString(R.string.share_time, FormatUtil.time(jogging.getTotalTime())));
//        ogo.setProperty("url", getString(R.string.play_store_url));
        ogo.getData().setProperty("meters", (int) jogging.getTotalDistance());
        ogo.getData().setProperty("time", FormatUtil.time(jogging.getTotalTime()));

        OpenGraphAction ogAction = GraphObject.Factory.create(OpenGraphAction.class);
        ogAction.setType(FACEBOOK_ACTION);
        ogAction.setProperty(FACEBOOK_PROPERTY_NAME, ogo);

        FacebookDialog shareDialog;
        if (image == null) {
            Log.e("carles","preparing shareDialog without map attached");
            shareDialog = new FacebookDialog.OpenGraphActionDialogBuilder(this, ogAction, FACEBOOK_PROPERTY_NAME).
                    build();
            Log.i(TAG, "Unable to load the map that should be shared with facebook");
        } else {
            Log.e("carles","preparing shareDialog with map attached");
//            shareDialog = new FacebookDialog.OpenGraphActionDialogBuilder(this, ogAction,
//                    FACEBOOK_OPEN_GRAPH_OBJECT).setImageAttachmentsForObject(FACEBOOK_OPEN_GRAPH_OBJECT,
//                    Arrays.asList(image)).build();

            shareDialog = new
                    FacebookDialog.OpenGraphActionDialogBuilder(this, ogAction, FACEBOOK_PROPERTY_NAME).build();

            // avoid bitmap memory leak
            if (image != null) {
                image.recycle();
                image = null;
            }

        }

        Log.e("carles","before tracking pending dialog");
        uiHelper.trackPendingDialogCall(shareDialog.present());
        showProgressDialog();

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
                dismissProgressDialog();

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
                    showShareFailure(error.getMessage());
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
        FacebookCallbackDialog.newInstance(null).show(getSupportFragmentManager(), TAG_FACEBOOK_RESPONSE);
    }

    private void showShareFailure(String errMsg) {
        FacebookCallbackDialog.newInstance(errMsg).show(getSupportFragmentManager(), TAG_FACEBOOK_RESPONSE);
    }

    private void showProgressDialog() {
        progress = new ProgressDialog(this);
        progress.setTitle(R.string.progress_waiting);
        progress.setMessage(getString(R.string.facebook_connecting));
        progress.setCancelable(true);
        progress.show();
    }

    private void dismissProgressDialog() {
        if (progress != null) {
            progress.dismiss();
            progress = null;
        }
    }
}
    /*- ************************************************************* */
    /*- ************************************************************* */
    class FacebookCallbackDialog extends DialogFragment {

        private static final String ARG_ERROR_MESSAGE = "arg_error_message";

        public static FacebookCallbackDialog newInstance(String errorMessage) {
            FacebookCallbackDialog ret = new FacebookCallbackDialog();
            Bundle args = new Bundle();
            args.putString(ARG_ERROR_MESSAGE, errorMessage);
            ret.setArguments(args);
            return ret;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Dialog dialog = new Dialog(getActivity());

            final LayoutInflater inflater = getActivity().getLayoutInflater();
            final View view = inflater.inflate(R.layout.dialog_custom, null);
            final TextView title = (TextView) view.findViewById(R.id.dlg_title);
            final TextView msg = (TextView) view.findViewById(R.id.dlg_msg);

            title.setText(R.string.facebook_response_title);
            if (getArguments() == null || getArguments().getString(ARG_ERROR_MESSAGE) == null) {
                msg.setText(R.string.facebook_post_success);
            } else {
                msg.setText(R.string.facebook_post_failure);
            }

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });

            dialog.setCanceledOnTouchOutside(true);
            dialog.getWindow().getAttributes().windowAnimations = R.style.Theme_Jogging_ZoomedDialog;
            return dialog;
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            dismiss();
        }
    }

