package com.carles.jogging.fragment;

import android.os.Bundle;

import com.carles.jogging.C;

/**
 * Created by carles1 on 20/04/14.
 */
public class BestTimesFragment extends BaseFragment {

    public BestTimesFragment() {}

    public static BestTimesFragment newInstance(String title) {
        BestTimesFragment fragment = new BestTimesFragment();
        Bundle args = new Bundle();
        args.putString(C.ARGS_ACTION_BAR_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }
}
