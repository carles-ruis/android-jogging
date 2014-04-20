package com.carles.jogging.fragment;

import android.os.Bundle;

import com.carles.jogging.C;

/**
 * Created by carles1 on 20/04/14.
 */
public class LastTimesFragment extends BaseFragment {
    public LastTimesFragment() {
    }

    public static LastTimesFragment newInstance(String title) {
        LastTimesFragment fragment = new LastTimesFragment();
        Bundle args = new Bundle();
        args.putString(C.ARGS_ACTION_BAR_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }
}

