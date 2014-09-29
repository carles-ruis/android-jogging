package com.carles.jogging.last_times;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.carles.jogging.BaseFragment;
import com.carles.jogging.C;
import com.carles.jogging.R;
import com.carles.jogging.jogging.FootingResult;
import com.carles.jogging.model.JoggingModel;
import com.carles.jogging.model.JoggingSQLiteHelper;
import com.carles.jogging.result.ResultDetailActivity;
import com.carles.jogging.util.PrefUtil;

import java.util.List;

/**
 * Created by carles1 on 10/09/14.
 */
public class LastTimesContentFragment extends BaseFragment {

    private Context ctx;

    private TextView txtNoResults;
    private ListView list;
    private ProgressBar progress;

    public static LastTimesContentFragment newInstance() {
        LastTimesContentFragment fragment = new LastTimesContentFragment();
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
        progress = (ProgressBar) view.findViewById(R.id.progress);

        loadData();

        return view;
    }

    private void loadData() {
        // obtain the selected distance by the user in the actionBar navigation list
        ((SherlockFragmentActivity) getActivity()).getSupportActionBar().
                setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        int position = ((SherlockFragmentActivity) getActivity()).getSupportActionBar().
                getSelectedNavigationIndex();
        if (position == -1) {
            position = 0;
        }
        final int meters = (position + 1)*1000;

        new QueryLastTimesAsyncTask().execute(meters);
    }

    private void updateView(final List<JoggingModel> joggings) {
        if (joggings == null || joggings.isEmpty()) {
            txtNoResults.setVisibility(View.VISIBLE);

        } else {
            list.setAdapter(new LastTimesAdapter(ctx, joggings));
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    JoggingModel selectedJogging = joggings.get(position);
                    List<JoggingModel> partials = JoggingSQLiteHelper.getInstance(ctx).queryPartials(selectedJogging);
                    selectedJogging.setPartialsForKilometer(partials);
                    selectedJogging.setPartials(partials);

                    Intent intent = new Intent(ctx, ResultDetailActivity.class);
                    intent.putExtra(C.EXTRA_JOGGING_TOTAL, selectedJogging);
                    intent.putExtra(C.EXTRA_FOOTING_RESULT, FootingResult.SUCCESS);
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.slide_activity_to_left_in, R.anim.slide_activity_to_left_out);
                }
            });
        }
    }

    /*- ********************************************************************************* */
    /*- ********************************************************************************* */
    private class QueryLastTimesAsyncTask extends AsyncTask<Integer, Void, List<JoggingModel>> {

        @Override
        protected void onPreExecute() {
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<JoggingModel> doInBackground(Integer... params) {
            return JoggingSQLiteHelper.getInstance(ctx).queryLastTimes(PrefUtil.getLoggedUser(ctx), params[0]);
        }

        @Override
        protected void onPostExecute(List<JoggingModel> joggings) {
            progress.setVisibility(View.GONE);
            updateView(joggings);
        }
    }
}
