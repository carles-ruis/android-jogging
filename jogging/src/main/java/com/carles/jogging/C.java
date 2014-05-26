package com.carles.jogging;

/**
 * Created by carles1 on 19/04/14.
 */
public class C {

    public static final boolean DEBUG = true;
    public static final String LOG_TAG = "JoggingApp";
    public static final int NOT_USED = -999;

    public static final int DEFAULT_DISTANCE = 2000;
    public static final Integer HALF_MARATHON_METERS = 21097;
    public static final int COUNTDOWN_STOP_MILLISECONDS = 1500;
    public static final long MAX_LOCATION_NOT_UPDATED_TIME = 60000;

    /*- unique identifiers */
    public static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    public static final int ONGOING_NOTIFICATION_IS_RUNNING = 8000;
    public static final String GET_LOCATIONS_SERVICE_NAME = "GetLocationsService";

    /*- arguments passed to fragments as bundle */
    public static final String ARGS_ACTION_BAR_TITLE = "args_action_bar_title";

    /*- arguments passes to activities as extras */
    public static final String EXTRA_DISTANCE_IN_METERS = "extra_kilometers";
    public static final String EXTRA_DISTANCE_TEXT = "extra_kilometers_text";
    public static final String EXTRA_FIRST_LOCATION = "extra_first_location";
    public static final String EXTRA_FOOTING_RESULT = "extra_footing_result";
    public static final String EXTRA_FOOTING_TIME = "extra_footing_time";
    public static final String EXTRA_PARTIALS = "extra_partials";

    /*- intent filter actions */
    public static final String ACTION_JOGGING_FINISHED = "com.carles.jogging.action_jogging_finished";

    /*- preferences keys */
    public static final String PREF_LAST_KILOMETERS_SELECTED = "pref_last_kms_selected";

    /*- tags for dialogs */
    public static final String TAG_CONNECTION_FAILED_DIALOG = "tag_connection_failed_dialog";
    public static final String TAG_GOOGLE_PLAY_SERVICES_ERROR_DIALOG = "tag_google_error_dialog";
    public static final String TAG_ACTIVATE_GPS_DIALOG = "tag_activate_gps_dialog";
    public static final String TAG_CANCEL_RUN_DIALOG = "tag_cancel_run_dialog";
    public static final String TAG_FIRST_LOCATION_NOT_OBTAINED = "tag_first_location_dialog";

    /*- sound configuration */
    public static final float VOLUME = 1.0f;
    public static final int MAX_SOUND_STREAMS = 1;
}
