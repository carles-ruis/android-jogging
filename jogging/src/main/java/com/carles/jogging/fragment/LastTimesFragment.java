package com.carles.jogging.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.carles.jogging.C;
import com.carles.jogging.R;
import com.carles.jogging.activity.MainActivity;

/**
 * Created by carles1 on 20/04/14.
 */
public class LastTimesFragment extends BaseFragment {

    public LastTimesFragment() {}

    public static LastTimesFragment newInstance(String title) {
        LastTimesFragment fragment = new LastTimesFragment();
        Bundle args = new Bundle();
        args.putString(C.ARGS_ACTION_BAR_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_best_times, container, false);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).setActionBarTitle(getArguments().getString(C.ARGS_ACTION_BAR_TITLE));
    }
}

