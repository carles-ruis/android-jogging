package com.carles.jogging;

/**
 * Created by carles1 on 19/04/14.
 */
public class C {

    public static final boolean DEBUG = true;
    public static final String LOG_TAG = "JoggingApp";

    /*- arguments passed to fragments as bundle */
    public static final String ARGS_ACTION_BAR_TITLE = "args_action_bar_title";


    /*- arguments passes to activities as extras */
    public static final String EXTRA_METERS = "extra_kilometers";
    public static final String EXTRA_STRAIGHT_LINE = "extra_straight_line";
    public static final String EXTRA_KILOMETERS_TEXT = "extra_kilometers_text";

    /*- preferences keys */
    public static final String PREF_IS_STRAIGHT_CIRCUIT = "pref_is_straight_circuit";
    public static final String PREF_LAST_KILOMETERS_SELECTED = "pref_last_kms_selected";

    /*- real world constants */
    public static final Integer HALF_MARATHON_METERS = 21097;

}
