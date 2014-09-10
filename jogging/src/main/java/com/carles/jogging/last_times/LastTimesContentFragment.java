package com.carles.jogging.last_times;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.carles.jogging.BaseFragment;
import com.carles.jogging.C;
import com.carles.jogging.R;
import com.carles.jogging.jogging.FootingResult;
import com.carles.jogging.model.JoggingModel;
import com.carles.jogging.model.JoggingSQLiteHelper;
import com.carles.jogging.model.UserModel;
import com.carles.jogging.result.ResultDetailActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by carles1 on 10/09/14.
 */
public class LastTimesContentFragment extends BaseFragment {

    private static final String ARGS_METERS = "args_meters";

    private Context ctx;
    private List<JoggingModel> joggings;

    private TextView txtNoResults;
    private ListView list;

    public static LastTimesContentFragment newInstance(long meters) {
        LastTimesContentFragment fragment = new LastTimesContentFragment();
        Bundle args = new Bundle();
        args.putLong(ARGS_METERS, meters);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ctx = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_last_times_content, container, false);

        txtNoResults = (TextView) view.findViewById(R.id.txt_no_results);
        list = (ListView) view.findViewById(R.id.list);

        loadData();

        return view;
    }

    private void loadData() {

        //TODO delete
        UserModel u = new UserModel();
        u.setName("u1");

        final long meters = getArguments().getLong(ARGS_METERS, -1);
        final List<JoggingModel> joggings = JoggingSQLiteHelper.getInstance(ctx).queryLastTimes(u, meters);

        if (joggings == null || joggings.isEmpty()) {
            txtNoResults.setVisibility(View.VISIBLE);

        } else {

            list.setAdapter(new LastTimesAdapter(ctx, joggings));
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    JoggingModel selectedJogging = joggings.get(position);
                    List<JoggingModel> partials = JoggingSQLiteHelper.getInstance(ctx).queryPartials(selectedJogging);

                    Intent intent = new Intent(ctx, ResultDetailActivity.class);
                    intent.putExtra(C.EXTRA_JOGGING_TOTAL, selectedJogging);
                    intent.putParcelableArrayListExtra(C.EXTRA_JOGGING_PARTIALS, (ArrayList) partials);
                    intent.putExtra(C.EXTRA_FOOTING_RESULT, FootingResult.SUCCESS.toString());
                    startActivity(intent);
                }
            });
        }
    }

}
