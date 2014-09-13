package com.carles.jogging;

/**
 * Created by carles1 on 19/04/14.
 */
public class C {

    public static final boolean DEBUG = true;

    public static final int DEFAULT_DISTANCE = 2000;
    public static final Integer HALF_MARATHON_METERS = 21097;
    public static final int COUNTDOWN_STOP_MILLISECONDS = 1500;

    // request codes for startActivityForResult
    public static final int REQ_CODE_GOOGLE_CONNECTION_FAILURE = 1;
    public static final int REQ_CODE_ENABLE_GPS = 2;

    // arguments passed to fragments as bundle
    public static final String ARGS_ACTION_BAR_TITLE = "args_action_bar_title";

    // arguments passes to activities as extras
    public static final String EXTRA_DISTANCE_IN_METERS = "extra_kilometers";
    public static final String EXTRA_DISTANCE_TEXT = "extra_kilometers_text";
    public static final String EXTRA_FIRST_LOCATION = "extra_first_location";
    public static final String EXTRA_FOOTING_RESULT = "extra_footing_result";
    public static final String EXTRA_FOOTING_TIME_TEXT = "extra_footing_time_text";
    public static final String EXTRA_JOGGING_PARTIALS = "extra_partials";
    public static final String EXTRA_JOGGING_TOTAL = "extra_jogging_total";
    public static final String EXTRA_SHOULD_PLAY_SOUND = "extra_should_play_sound";
    public static final String EXTRA_SHOULD_SAVE_RUNNING = "extra_should_save_running";

    // intent filter actions
    public static final String ACTION_UPDATE_KILOMETERS_RUN = "com.carles.jogging.KILOMETERS_RUN";

    // preferences
    public static final String PREF_FILE = "shared_prefs_file_";
    public static final String PREF_LAST_KILOMETERS_SELECTED = "pref_last_kms_selected";
    public static final String PREF_LOGGER_USER = "pref_logged_user";

    // tags for dialogs
    public static final String TAG_CONNECTION_FAILED_DIALOG = "tag_connection_failed_dialog";
    public static final String TAG_GOOGLE_PLAY_SERVICES_ERROR_DIALOG = "tag_google_error_dialog";
    public static final String TAG_ACTIVATE_GPS_DIALOG = "tag_activate_gps_dialog";
    public static final String TAG_FIRST_LOCATION_NOT_OBTAINED = "tag_first_location_dialog";

    // sound configuration
    public static final int MAX_SOUND_STREAMS = 1;
    public static final float VOLUME = 1.0f;

    // google analytics custom dimension indexes
    public static final int GA_DIMENSION_USER = 1;

}
