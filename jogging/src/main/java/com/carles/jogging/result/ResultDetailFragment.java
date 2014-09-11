package com.carles.jogging.result;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.carles.jogging.C;
import com.carles.jogging.R;
import com.carles.jogging.BaseFragment;
import com.carles.jogging.jogging.FootingResult;
import com.carles.jogging.model.JoggingSQLiteHelper;
import com.carles.jogging.model.UserModel;
import com.carles.jogging.model.JoggingModel;
import com.carles.jogging.util.FormatUtil;

import java.util.List;
import java.util.concurrent.CancellationException;

/**
 * Created by carles1 on 20/04/14.
 */
public class ResultDetailFragment extends BaseFragment {

    private static final String TAG = ResultDetailFragment.class.getName();

    private Context ctx;
    private OnLocationClickedListener callbacks;
    private boolean hasObtainedLocations;

    public static ResultDetailFragment newInstance() {
        ResultDetailFragment detailFragment = new ResultDetailFragment();
        return detailFragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            callbacks = (OnLocationClickedListener) activity;
            ctx = activity;
        } catch (CancellationException e) {
            Log.e(TAG, "activity must implement OnMapButtonClickListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_result_detail, container, false);

        // allows this fragment to inflate its own menu in the action bar
        setHasOptionsMenu(true);

        // obtain extras to show the results
        Bundle extras = getActivity().getIntent().getExtras();
        List<JoggingModel> partials = extras.<JoggingModel>getParcelableArrayList(C.EXTRA_JOGGING_PARTIALS);
        JoggingModel jogging = extras.getParcelable(C.EXTRA_JOGGING_TOTAL);

        // obtain title and subtitle
        FootingResult footingResult;
        String title;
        String subtitle;
        if (extras.getSerializable(C.EXTRA_FOOTING_RESULT) == null) {
            footingResult = FootingResult.UNKNOWN_ERROR;
            title = getString(R.string.footing_result_failure_title);
            subtitle = getString(R.string.footing_result_unknown_error);

        } else {
            footingResult = (FootingResult) extras.getSerializable(C.EXTRA_FOOTING_RESULT);
            if (footingResult == FootingResult.SUCCESS) {
                title = getString(R.string.footing_result_success_title);
                subtitle = getString(R.string.footing_result_success, (int)jogging.getTotalDistance());
            } else {
                title = getString(R.string.footing_result_failure_title);
                subtitle = getString(getResources().getIdentifier(footingResult.getResourceId(), "string", ctx.getPackageName()));
            }
        }

        hasObtainedLocations = partials != null && !partials.isEmpty() && jogging != null;

        // map views
        final TextView txtTitle = (TextView) view.findViewById(R.id.txt_result_title);
        final TextView txtSubtitle = (TextView) view.findViewById(R.id.txt_result_subtitle);
        final TextView txtNoLocations = (TextView) view.findViewById(R.id.txt_result_no_locations);
        final View lytResultData = view.findViewById(R.id.lyt_result_data);
        final TextView txtTime = (TextView) view.findViewById(R.id.txt_result_time);
        final TextView txtDistance = (TextView) view.findViewById(R.id.txt_result_distance);
        final TextView txtBestTime = (TextView) view.findViewById(R.id.txt_result_best_time);
        final TextView txtRealTime = (TextView) view.findViewById(R.id.txt_result_real_time);
        final TextView txtRealDistance = (TextView) view.findViewById(R.id.txt_result_real_distance);
        final ListView list = (ListView) view.findViewById(R.id.list);
        final TextView txtSaved = (TextView) view.findViewById(R.id.txt_result_saved);

        // populate views
        txtTitle.setText(title);
        txtSubtitle.setText(subtitle);

        if (hasObtainedLocations) {
            txtTime.setText(getString(R.string.result_time, FormatUtil.time(jogging.getTotalTime())));
            txtDistance.setText(getString(R.string.result_distance, (int)jogging.getTotalDistance()));

            if (footingResult == FootingResult.SUCCESS) {
                txtRealTime.setText(getString(R.string.result_real_time, FormatUtil.time(jogging.getRealTime())));
                txtRealDistance.setText(getString(R.string.result_real_distance,(int)jogging.getRealDistance()));

                // TODO delete
                UserModel user = new UserModel();
                user.setName("u1");
                jogging.setUser(user);
                if (jogging.getTotalTime() < JoggingSQLiteHelper.getInstance(ctx).queryBestTimeByDistance(user, jogging.getTotalDistance())) {
                    // runner has achieve his record for this distance
                    txtBestTime.setVisibility(View.VISIBLE);
                }
                if (extras.getBoolean(C.EXTRA_SHOULD_SAVE_RUNNING,false)) {
                    txtSaved.setVisibility(View.VISIBLE);
                }

            } else {
                txtRealTime.setVisibility(View.GONE);
                txtRealDistance.setVisibility(View.GONE);
            }

            final PartialResultsAdapter adapter = new PartialResultsAdapter(ctx, partials);
            // add a header with the isSelectable flag to false
            list.addHeaderView(inflater.inflate(R.layout.header_partial_result, list, false), null, false);
            list.setAdapter(adapter);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    if (callbacks != null) {
                        // -1 position because first position is the list header
                        callbacks.onLocationClicked(position - 1);
                    }
                }
            });

        } else {
            lytResultData.setVisibility(View.GONE);
            txtNoLocations.setVisibility(View.VISIBLE);
        }

        getActivity().getIntent().removeExtra(C.EXTRA_SHOULD_PLAY_SOUND);
        getActivity().getIntent().removeExtra(C.EXTRA_SHOULD_SAVE_RUNNING);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_result_detail, menu);
    }

    public interface OnLocationClickedListener {
        void onLocationClicked(int position);
    }
}
