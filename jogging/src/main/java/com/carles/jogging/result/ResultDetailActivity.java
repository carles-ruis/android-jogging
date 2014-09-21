package com.carles.jogging.result;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
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
import com.carles.jogging.util.SystemUtil;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.RequestBatch;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionDefaultAudience;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.OpenGraphAction;
import com.facebook.model.OpenGraphObject;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by carles1 on 20/04/14.
 */
public class ResultDetailActivity extends BaseActivity implements ResultDetailFragment.OnLocationClickedListener {

    private static final String TAG = ResultDetailActivity.class.getSimpleName();

    private static final String TAG_FACEBOOK_RESPONSE = "tag_facebook_response";

    private static final Uri M_FACEBOOK_URL = Uri.parse("http://m.facebook.com");
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

    // TODO
    private static final String PERMISSION = "publish_actions";
    private static final int REAUTH_ACTIVITY_CODE = 100;

    private boolean pendingAnnounce;

    private Session.StatusCallback sessionCallback = new Session.StatusCallback() {
        @Override
        public void call(final Session session, final SessionState state, final Exception exception) {
            // onSessionStateChanged()
            Log.e("carles", "statusCallback call , session state changed");
            if (session != null && session.isOpened()) {

                switch (state) {

                    case OPENED_TOKEN_UPDATED:
                        Log.e("carles", "about to handleShareWithFeedDial");
                        handleShareWithFeedDialog();
                        break;
                }
            }
        }
    };

    private FacebookDialog.Callback nativeDialogCallback = new FacebookDialog.Callback() {
        @Override
        public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
            Log.e("carles", "FacebookDialog callback onComplete");
            dismissProgressDialog();

            if (FacebookDialog.getNativeDialogDidComplete(data)) {
                if (FacebookDialog.COMPLETION_GESTURE_CANCEL
                        .equals(FacebookDialog.getNativeDialogCompletionGesture(data))) {
                    // cancelled by the user
                    Log.e("carles", "share with facebook cancelat per l'usuari");

                } else {
                    // shared with facebook
                    trackSocialInteraction();
                    showSuccessResponse();
                    Log.i(TAG, "Shared with facebook via ShareDialog");
                    Log.e("carles", "compartició amb facebook ok");
                }

            } else {
                // dialog cancelled
                Log.i(TAG, "NOT Shared via ShareDialog. FacebookDialog.getNativeDialogDidComplete returned false");
                Log.e("carles", "NOT Shared via ShareDialog. FacebookDialog.getNativeDialogDidComplete returned false");
            }
        }

        @Override
        public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
            dismissProgressDialog();
            showFailureResponse(error.getMessage());
            Log.e(TAG, "NOT shared with facebook via ShareDialog. Error:" + error.getMessage());
            Log.e("carles", "NOT shared with facebook via ShareDialog. Error:" + error.getMessage());
        }
    };

    // data obtained from the intent
    private JoggingModel jogging;
    private ArrayList<JoggingModel> partials = new ArrayList<JoggingModel>();

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
        uiHelper = new UiLifecycleHelper(this,sessionCallback);
        uiHelper.onCreate(savedInstanceState);

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

        Log.e("carles", String.format("onActivityResult values. requestCode=%d, resultCode=%d, data=%s",
                requestCode, resultCode, new Gson().toJson(data)));

        // handle facebook's ShareDialog callback
        uiHelper.onActivityResult(requestCode, resultCode, data, nativeDialogCallback);

        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
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
        Session session = Session.getActiveSession();

        Log.e("carles", "session obtained");
        //            if (FacebookDialog.canPresentOpenGraphActionDialog(getApplicationContext(),
        //                    FacebookDialog.OpenGraphActionDialogFeature.OG_ACTION_DIALOG)) {
        //                handleNativeShareAnnounce();
        //            } else {

        if (!session.isOpened()) {
            Log.e("carles", "open active session");
            Session.openActiveSession(this, true, sessionCallback);

        } else {

            List<String> permissions = session.getPermissions();

            Log.e("carles", "permissions=" + permissions);

            if (!permissions.contains(PERMISSION)) {
                requestPublishPermissions(session);
            } else {
                session.openForPublish(new Session.OpenRequest(this).setCallback(sessionCallback));
            }

        }

        //            }
    }


    private void handleGraphApiAnnounce() {

        Log.e("carles","handleGraphApiAnnounce");

        Session session = Session.getActiveSession();

        List<String> permissions = session.getPermissions();

        Log.e("carles", "permissions=" + permissions);

        if (!permissions.contains(PERMISSION)) {
            pendingAnnounce = true;
            requestPublishPermissions(session);
            return;
        }

        // Show a progress dialog because sometimes the requests can take a while.
        showProgressDialog();

        // Run this in a background thread so we can process the list of responses and extract errors.
        AsyncTask<Void, Void, List<Response>> task = new AsyncTask<Void, Void, List<Response>>() {

            @Override
            protected List<Response> doInBackground(Void... voids) {
                GoForAction goForAction = createGoForAction();

                RequestBatch requestBatch = new RequestBatch();

                RunGraphObject run = goForAction.getRun();
                if (run.getCreateObject()) {
                    Request createObjectRequest =
                            Request.newPostOpenGraphObjectRequest(Session.getActiveSession(), run, null);
                    createObjectRequest.setBatchEntryName("createObject");
                    requestBatch.add(createObjectRequest);
                    goForAction.setProperty(FACEBOOK_PROPERTY_NAME, "{result=createObject:$.id}");
                }

                Request request = Request.newPostOpenGraphActionRequest(Session.getActiveSession(), goForAction, null);
                requestBatch.add(request);

                return requestBatch.executeAndWait();
            }

            @Override
            protected void onPostExecute(List<Response> responses) {
                // We only care about the last response, or the first one with an error.
                Response finalResponse = null;
                for (Response response : responses) {
                    finalResponse = response;
                    if (response != null && response.getError() != null) {
                        break;
                    }
                }

                // onPostActionResponse
                dismissProgressDialog();

                PostResponse postResponse = finalResponse.getGraphObjectAs(PostResponse.class);
                if (postResponse != null && postResponse.getId() != null) {
                    trackSocialInteraction();
                    showSuccessResponse(postResponse.getId());
                } else {
                    showFailureResponse(finalResponse.getError());
                }
            }
        };

        task.execute();
    }

    private void handleNativeShareAnnounce() {
        Log.e("carles", "handleNativeShareAnnounce");
        showProgressDialog();

        GoForAction goForAction = createGoForAction();

        FacebookDialog.OpenGraphActionDialogBuilder builder = new FacebookDialog.OpenGraphActionDialogBuilder(this, goForAction, FACEBOOK_PROPERTY_NAME);

        Log.e("carles", "about to trackPendingDialogCall");
        uiHelper.trackPendingDialogCall(builder.build().present());
    }


    private GoForAction createGoForAction() {
        GoForAction ret = OpenGraphAction.Factory.createForPost(GoForAction.class, FACEBOOK_ACTION);

        RunGraphObject runGraphObject = GraphObject.Factory.create(RunGraphObject.class);
        runGraphObject.setTitle(getString(R.string.share_title, (int)jogging.getTotalDistance()));
        runGraphObject.setDescription(getString(R.string.share_time, FormatUtil.time(jogging.getTotalTime())));
        //        runGraphObject.setUrl(getString(R.string.play_store_url));
        //        runGraphObject.setProperty("meters", (int) jogging.getTotalDistance());
        //        runGraphObject.setProperty("time", FormatUtil.time(jogging.getTotalTime()));

        ret.setRun(runGraphObject);
        return ret;
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
        }

        Log.e("carles","before tracking pending dialog");
        uiHelper.trackPendingDialogCall(shareDialog.present());
        showProgressDialog();

        Log.e("carles","pending dialog is being tracked");
    }

    private void handleShareWithFeedDialog() {
        Log.e("carles", "shareWithFeedDialog");
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
                    // When the story is posted, echo success and post Id.
                    final String postId = values.getString("post_id");
                    if (postId != null) {
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

        Log.e("carles", "... ShareWithFeedDialog");
    }

    private void trackSocialInteraction() {
        EasyTracker.getInstance(this).send(MapBuilder.createSocial("Facebook", "Share", PrefUtil.getLoggedUser(ctx).getName() + " running").build());
    }

    private void showSuccessResponse() {
        showSuccessResponse(null);
    }

    private void showSuccessResponse(String response) {
        FacebookCallbackDialog.newInstance(response, false).show(getSupportFragmentManager(), TAG_FACEBOOK_RESPONSE);
    }

    private void showFailureResponse(FacebookRequestError error) {
        DialogInterface.OnClickListener listener = null;
        String errMsg = null;

        if (error == null) {
            errMsg = getString(R.string.facebook_post_failure);
        } else {
            switch (error.getCategory()) {
                case AUTHENTICATION_RETRY:
                    // tell the user what happened by getting the message id, and
                    // retry the operation later
                    String userAction = (error.shouldNotifyUser()) ? "" : getString(error.getUserActionMessageId());
                    errMsg = getString(R.string.facebook_error_authentication_retry, userAction);
                    listener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, M_FACEBOOK_URL);
                            startActivity(intent);
                        }
                    };

                    new AlertDialog.Builder(this).setPositiveButton(R.string.ok, listener).setTitle(R.string.facebook_response_title).setMessage(errMsg).show();
                    return;

                case AUTHENTICATION_REOPEN_SESSION:
                    // close the session and reopen it.
                    errMsg = getString(R.string.facebook_error_authentication_reopen);
                    listener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Session session = Session.getActiveSession();
                            if (session != null && !session.isClosed()) {
                                session.closeAndClearTokenInformation();
                            }
                        }
                    };

                    new AlertDialog.Builder(this).setPositiveButton(R.string.ok, listener).setTitle(R.string.facebook_response_title).setMessage(errMsg).show();
                    return;

                case PERMISSION:
                    // request the publish permission
                    errMsg = getString(R.string.facebook_error_permission);
                    listener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            pendingAnnounce = true;
                            requestPublishPermissions(Session.getActiveSession());
                        }
                    };
                    break;

                case SERVER:
                case THROTTLING:
                    // this is usually temporary, don't clear the fields, and
                    // ask the user to try again
                    errMsg = getString(R.string.facebook_error_server);
                    break;

                case BAD_REQUEST:
                    // this is likely a coding error, ask the user to file a bug
                    errMsg = getString(R.string.facebook_error_bad_request, error.getErrorMessage());
                    break;

                case OTHER:
                case CLIENT:
                default:
                    // an unknown issue occurred, this could be a code error, or
                    // a server side issue, log the issue, and either ask the
                    // user to retry, or file a bug
                    errMsg = getString(R.string.facebook_error_unknown, error.getErrorMessage());
                    break;
            }
        }

        showFailureResponse(errMsg);

    }

    private void showFailureResponse(String errMsg) {
        FacebookCallbackDialog.newInstance(errMsg, true).show(getSupportFragmentManager(), TAG_FACEBOOK_RESPONSE);
    }

    private void requestPublishPermissions(Session session) {

        Log.e("carles","check if we should request publish permissions");

        if (session != null) {

            Log.e("carles", "session is not null: requesting publish permissions");

            Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(this, PERMISSION)
                    // demonstrate how to set an audience for the publish permissions,
                    // if none are set, this defaults to FRIENDS
                    .setDefaultAudience(SessionDefaultAudience.FRIENDS).setRequestCode(REAUTH_ACTIVITY_CODE).setCallback(sessionCallback);
            session.requestNewPublishPermissions(newPermissionsRequest);
        }
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

    /*- ************************************************************* */
    /*- ************************************************************* */

    /**
     * Used to inspect the response from posting an action
     */
    private interface PostResponse extends GraphObject {
        String getId();
    }

    private interface GoForAction extends OpenGraphAction, Serializable {
        public RunGraphObject getRun();
        public void setRun(RunGraphObject run);
    }

    private interface RunGraphObject extends OpenGraphObject, Serializable {
        public String getUrl();
        public void setUrl(String url);

        public String getId();
        public void setId(String id);
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

