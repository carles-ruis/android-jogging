package com.carles.jogging.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.carles.jogging.R;
import com.carles.jogging.fragment.BaseFragment;

/**
 * Created by carles1 on 20/04/14.
 */
public class ResultMapFragment extends BaseFragment {

    private ResultMapFragment() {}

    public static ResultMapFragment newInstance() {
        ResultMapFragment mapFragment = new ResultMapFragment();
        return mapFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_result_map, container, false);

        return view;
    }
}
