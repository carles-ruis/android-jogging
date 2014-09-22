package com.carles.jogging.result;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import com.carles.jogging.util.FormatUtil;
import com.carles.jogging.util.PrefUtil;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.SessionLoginBehavior;
import com.facebook.SessionState;
import com.facebook.widget.WebDialog;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by carles1 on 20/04/14.
 */
public class ResultDetailActivity extends BaseActivity implements ResultDetailFragment.OnLocationClickedListener {

    private static final String TAG = ResultDetailActivity.class.getSimpleName();
    private static final String TAG_FACEBOOK_RESPONSE = "tag_facebook_response";
    private static final Uri M_FACEBOOK_URL = Uri.parse("http://m.facebook.com");
    private static final String PERMISSION = "publish_actions";
    private static final int REAUTH_ACTIVITY_CODE = 100;

    private Context ctx;
    private ResultDetailFragment detailFragment = null;
    private ResultMapFragment mapFragment = null;
    private ProgressDialog progress;

    // data obtained from the intent
    private JoggingModel jogging;
    private ArrayList<JoggingModel> partials = new ArrayList<JoggingModel>();

    // share with facebook
    private Session.StatusCallback sessionCallback = new Session.StatusCallback() {
        @Override
        public void call(final Session session, final SessionState state, final Exception exception) {

            Log.e("carles", "statusCallback, state=" + state.name());
            if (session != null && session.isOpened()) {
                switch (state) {
                    case CREATED_TOKEN_LOADED:
                        Log.e("carles", "CALLBACK: now we will openForPublish");
                        openForPublish_(session);
                        break;

                    case OPENED:
                    case OPENED_TOKEN_UPDATED:
                        Log.e("carles", "CALLBACK: now we will publishFeedDialog");
                        publishFeedDialog();
                        break;
                }
            }
        }
    };

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

        // show detail fragment as is the ResultDetailActivity's initial fragment
        if (detailFragment == null) {
            detailFragment = ResultDetailFragment.newInstance();
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, detailFragment).commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (Session.getActiveSession() != null) {
            Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
        }
    }

    @Override
    public void onLocationClicked(int position) {
        addResultMapFragment(position);
    }

    private void addResultMapFragment(int position) {
        if (mapFragment == null) {
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

    private void shareWithFacebook() {
        // safe check
        if (jogging == null) {
            return;
        }

        Session session = Session.getActiveSession();
        if (session == null) {
            session = new Session(this);
            Session.setActiveSession(session);
        }

        Log.e("carles", "session state=" + session.getState().name());

        if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
            openForPublish_(session);
        } else {
            Session.openActiveSession(this, true, sessionCallback);
        }
    }

    private void openForPublish_(Session session) {
        Log.e("carles","open for publish_");
        if (session != null) {
            List<String> permissions = new ArrayList<String>();
            permissions.add(PERMISSION);

            Session.OpenRequest openRequest = new Session.OpenRequest(this).setCallback(sessionCallback);
            openRequest.setLoginBehavior(SessionLoginBehavior.SUPPRESS_SSO);
            openRequest.setRequestCode(Session.DEFAULT_AUTHORIZE_ACTIVITY_CODE);
            openRequest.setPermissions(permissions);
            session.openForPublish(openRequest);
        }
    }

    private void publishFeedDialog() {
        Log.e("carles", "show publishFeedDialog");
        if (Session.getActiveSession() == null) {
            return;
        }

        showProgressDialog();

        Bundle params = new Bundle();
        String user = PrefUtil.getLoggedUser(ctx).getName();
        String appName = getString(R.string.app_name);
        params.putString("name", getString(R.string.share_feed_dialog_name, user, appName));
        params.putString("caption", getString(R.string.app_name));
        params.putString("description", getString(R.string.share_feed_dialog_desc,
                (int) jogging.getTotalDistance(), FormatUtil.time(jogging.getTotalTime())));
        params.putString("link", getString(R.string.play_store_url));

        WebDialog feedDialog = (new WebDialog.FeedDialogBuilder(ctx, Session.getActiveSession(), params)).
                setOnCompleteListener(new WebDialog.OnCompleteListener() {

                    @Override
                    public void onComplete(Bundle values, FacebookException error) {
                        dismissProgressDialog();

                        if (error == null) {

                            final String postId = values.getString("post_id");
                            if (postId != null) {
                                // When the story is posted
                                Log.i(TAG, "Shared with facebook via FeedDialog. id=" + postId);
                                Log.e("carles", "Shared with facebook via FeedDialog. id=" + postId);
                                trackSocialInteraction();
                                showSuccessResponse();

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
                            showFailureResponse(error.getMessage());
                        }
                    }
                }).build();
        feedDialog.show();
    }

    private void trackSocialInteraction() {
        EasyTracker.getInstance(this).send(MapBuilder.createSocial("Facebook", "Share",
                PrefUtil.getLoggedUser(ctx).getName() + " running").build());
    }

    private void showSuccessResponse() {
        showSuccessResponse(null);
    }

    private void showSuccessResponse(String response) {
        FacebookCallbackDialog.newInstance(response, false).show(getSupportFragmentManager(), TAG_FACEBOOK_RESPONSE);
    }

    private void showFailureResponse(String errMsg) {
        FacebookCallbackDialog.newInstance(errMsg, true).show(getSupportFragmentManager(), TAG_FACEBOOK_RESPONSE);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.action_facebook:
                Log.e("carles", "action facebook selected");
                shareWithFacebook();
                return true;

            case R.id.action_map:
                addResultMapFragment(-1);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
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

}

/*- ************************************************************* */
    /*- ************************************************************* */
class FacebookCallbackDialog extends DialogFragment {

    private static final String ARG_RESPONSE = "arg_response";
    private static final String ARG_IS_ERROR = "arg_is_error";

    public static FacebookCallbackDialog newInstance(String response, boolean isError) {
        FacebookCallbackDialog ret = new FacebookCallbackDialog();
        Bundle args = new Bundle();
        args.putString(ARG_RESPONSE, response);
        args.putBoolean(ARG_IS_ERROR, isError);
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
        if (getArguments() == null || getArguments().getString(ARG_RESPONSE) == null) {
            msg.setText(R.string.facebook_post_success);
        } else if (getArguments().getBoolean(ARG_IS_ERROR, false)) {
            msg.setText(R.string.facebook_post_failure);
        } else {
            msg.setText(getArguments().getString(ARG_RESPONSE));
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

