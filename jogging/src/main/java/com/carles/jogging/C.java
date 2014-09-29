package com.carles.jogging;

/**
 * Created by carles1 on 19/04/14.
 */
public class C {

    public static final boolean DEBUG = false;

    public static final Integer HALF_MARATHON_METERS = 21097;
    public static final int COUNTDOWN_STOP_MILLISECONDS = 1500;
    public static final int NO_DISTANCE = 999999;

    // request codes for startActivityForResult
    public static final int REQ_CODE_GOOGLE_CONNECTION_FAILURE = 1;
    public static final int REQ_CODE_ENABLE_GPS = 2;

    // arguments passes to activities as extras
    public static final String EXTRA_KILOMETERS = "extra_kilometers";
    public static final String EXTRA_FIRST_LOCATION = "extra_first_location";
    public static final String EXTRA_FOOTING_RESULT = "extra_footing_result";
    public static final String EXTRA_JOGGING_TOTAL = "extra_jogging_total";
    public static final String EXTRA_RUNNING_SAVED = "extra_should_save_running";
    public static final String EXTRA_BEST_TIME = "extra_best_time";

    // preferences
    public static final String PREF_FILE = "shared_prefs_file_";
    public static final String PREF_LOGGER_USER = "pref_logged_user";
    public static final String PREF_LAST_KILOMETERS = "pref_last_kilometers";
    public static final String PREF_DISTANCE_SELECTED = "pref_distance_selected";

    // tags for dialogs
    public static final String TAG_CONNECTION_FAILED_DIALOG = "tag_connection_failed_dialog";
    public static final String TAG_GOOGLE_PLAY_SERVICES_ERROR_DIALOG = "tag_google_error_dialog";
    public static final String TAG_ACTIVATE_GPS_DIALOG = "tag_activate_gps_dialog";
    public static final String TAG_FIRST_LOCATION_NOT_OBTAINED = "tag_first_location_dialog";
    public static final String TAG_CONFIRM_DELETE_DIALOG = "tag_confirm_delete_dialog";

    // sound configuration
    public static final int MAX_SOUND_STREAMS = 1;
    public static final float VOLUME = 1.0f;

    // google analytics custom dimension indexes
    public static final int GA_DIMENSION_USER = 1;

    // SQLite database
    public static final String DATABASE_NAME = "jogging.db";
    public static final int DATABASE_VERSION = 1;

}