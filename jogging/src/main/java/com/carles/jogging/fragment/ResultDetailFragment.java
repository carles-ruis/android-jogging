package com.carles.jogging.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.carles.jogging.C;
import com.carles.jogging.R;
import com.carles.jogging.activity.ResultActivity;
import com.carles.jogging.helper.LocationHelper;
import com.carles.jogging.model.PartialJogging;

import java.util.ArrayList;

/**
 * Created by carles1 on 20/04/14.
 */
public class ResultDetailFragment extends BaseFragment {

    private ResultDetailFragment() {}

    public static ResultDetailFragment newInstance() {
        ResultDetailFragment detailFragment = new ResultDetailFragment();
        return detailFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_result_detail, container, false);

        Bundle extras = getActivity().getIntent().getExtras();
        StringBuilder results = new StringBuilder();
        results.append("Distance selected=" + extras.getString(C.EXTRA_DISTANCE_TEXT)).append(" ----- ");
        results.append("Distance ran in meters=" + extras.getFloat(C.EXTRA_DISTANCE_IN_METERS)).append(" ----- ");
        results.append("Running time=" + LocationHelper.formatRunningTime(extras.getLong(C.EXTRA_FOOTING_TIME))).append(" ------ ");

        ArrayList<PartialJogging> partials = extras.getParcelableArrayList(C.EXTRA_PARTIALS);
        results.append("Partial times:");
        for (PartialJogging pj : partials) {
            results.append(pj.toString()).append(" - ");
        }

        ((TextView) view.findViewById(R.id.result_resume)).setText(results); // TODO delete. populate the right elements
        ((Button) view.findViewById(R.id.result_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ResultActivity) getActivity()).addResultMapFragment();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
}
