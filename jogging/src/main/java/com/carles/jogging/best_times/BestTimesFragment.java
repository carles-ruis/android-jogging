package com.carles.jogging.best_times;

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
import com.carles.jogging.result.ResultDetailActivity;
import com.carles.jogging.util.PrefUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by carles1 on 20/04/14.
 */
public class BestTimesFragment extends BaseFragment {

    private Context ctx;

    private TextView txtNoResults;
    private ListView list;

    public static BestTimesFragment newInstance() {
        BestTimesFragment fragment = new BestTimesFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_best_times, container, false);
        ctx = getActivity();

        txtNoResults = (TextView) view.findViewById(R.id.txt_no_results);
        list = (ListView) view.findViewById(R.id.list);

        // load best times from database
        loadData();

        return view;
    }

    private void loadData() {
        final List<JoggingModel> bestTimes = JoggingSQLiteHelper.getInstance(ctx).queryBestTimes(PrefUtil.getLoggedUser(ctx));

        if (bestTimes == null || bestTimes.isEmpty()) {
            txtNoResults.setVisibility(View.VISIBLE);

        } else {
            list.addHeaderView(LayoutInflater.from(ctx).inflate(R.layout.header_best_times, list, false), null, false);
            list.setAdapter(new BestTimesAdapter(ctx, bestTimes));
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    JoggingModel joggingSelected = bestTimes.get(position-1);
                    List<JoggingModel> partials = JoggingSQLiteHelper.getInstance(ctx).queryPartials(joggingSelected);

                    Intent intent = new Intent(ctx, ResultDetailActivity.class);
                    intent.putExtra(C.EXTRA_JOGGING_TOTAL, joggingSelected);
                    intent.putParcelableArrayListExtra(C.EXTRA_JOGGING_PARTIALS, (ArrayList) partials);
                    intent.putExtra(C.EXTRA_FOOTING_RESULT, FootingResult.SUCCESS);
                    startActivity(intent);
                }
            });
        }
    }
}