package com.carles.jogging.result;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.carles.jogging.BaseFragment;
import com.carles.jogging.C;
import com.carles.jogging.R;
import com.carles.jogging.jogging.FootingResult;
import com.carles.jogging.model.JoggingModel;
import com.carles.jogging.util.FormatUtil;

import java.util.concurrent.CancellationException;

/**
 * Created by carles1 on 20/04/14.
 */
public class ResultDetailFragment extends BaseFragment {

    private static final String TAG = ResultDetailFragment.class.getSimpleName();

    private Context ctx;
    private OnLocationClickedListener callbacks;
    private Boolean hasObtainedLocations;
    private boolean hasWhatsApp;

    private FootingResult footingResult = FootingResult.UNKNOWN_ERROR;

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
            throw new ClassCastException("Activity must implement OnLocationClickListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_result_detail, container, false);

        // allows this fragment to inflate its own menu in the action bar
        setHasOptionsMenu(true);

        setHasObtainedLocations();

        loadViews(view);

        getActivity().getIntent().removeExtra(C.EXTRA_RUNNING_SAVED);

        return view;
    }

    private void setHasObtainedLocations() {
        if (hasObtainedLocations == null) {
            JoggingModel jogging = getActivity().getIntent().getExtras().getParcelable(C.EXTRA_JOGGING_TOTAL);
            hasObtainedLocations = jogging != null && jogging.getPartials() != null && !jogging.getPartials().isEmpty();
        }
    }

    private void loadViews(View view) {
        final TextView txtTitle = (TextView) view.findViewById(R.id.txt_result_title);
        final TextView txtSubtitle = (TextView) view.findViewById(R.id.txt_result_subtitle);
        final TextView txtNoLocations = (TextView) view.findViewById(R.id.txt_result_no_locations);
        final View lytResultData = view.findViewById(R.id.lyt_result_data);
        final TextView txtTime = (TextView) view.findViewById(R.id.txt_result_time);
        final TextView txtDistance = (TextView) view.findViewById(R.id.txt_result_distance);
        final TextView txtSpeed = (TextView) view.findViewById(R.id.txt_result_speed);
        final TextView txtBestTime = (TextView) view.findViewById(R.id.txt_result_best_time);
        final ListView list = (ListView) view.findViewById(R.id.list);
        final TextView txtSaved = (TextView) view.findViewById(R.id.txt_result_saved);

        Bundle extras = getActivity().getIntent().getExtras();
        JoggingModel jogging = extras.getParcelable(C.EXTRA_JOGGING_TOTAL);

        // show title and subtitle
        String title = "";
        String subtitle = "";
        if (extras.getSerializable(C.EXTRA_FOOTING_RESULT) == null) {
            footingResult = FootingResult.UNKNOWN_ERROR;
            title = getString(R.string.footing_result_failure_title);
            subtitle = getString(R.string.footing_result_unknown_error);

        } else {
            footingResult = (FootingResult) extras.getSerializable(C.EXTRA_FOOTING_RESULT);
            if (footingResult == FootingResult.SUCCESS) {
                title = getString(R.string.footing_result_success_title);
            } else {
                title = getString(R.string.footing_result_failure_title);
                subtitle = getString(getResources().getIdentifier(footingResult.getResourceId(), "string", ctx.getPackageName()));
            }
        }
        txtTitle.setText(title);
        txtSubtitle.setText(subtitle);

        // show jogging data
        if (hasObtainedLocations) {
            txtTime.setText(getString(R.string.result_time, FormatUtil.time(jogging.getGoalTime())));
            txtDistance.setText(getString(R.string.result_distance, (int)jogging.getGoalDistance()));

            if (footingResult == FootingResult.SUCCESS) {
                txtSubtitle.setVisibility(View.GONE);
                txtSpeed.setText(getString(R.string.result_speed, getSpeed(jogging)));

                if (extras.getBoolean(C.EXTRA_BEST_TIME, false)) {
                    txtBestTime.setVisibility(View.VISIBLE);
                }

                if (extras.getBoolean(C.EXTRA_RUNNING_SAVED, false)) {
                    txtSaved.setVisibility(View.VISIBLE);
                }

            } else {
                txtSpeed.setVisibility(View.GONE);
            }

            final PartialResultsAdapter adapter = new PartialResultsAdapter(ctx, jogging.getPartialsForKilometer());
            // add a header with the isSelectable flag to false
            //            list.addHeaderView(inflater.inflate(R.layout.header_partial_result, list, false), null, false);
            list.setAdapter(adapter);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    if (callbacks != null) {
                        //                        callbacks.onLocationClicked(position - 1);
                        callbacks.onLocationClicked(position);
                    }
                }
            });

        } else {
            lytResultData.setVisibility(View.GONE);
            txtNoLocations.setVisibility(View.VISIBLE);
        }
    }

    private Float getSpeed(JoggingModel jogging) {
        float kms = jogging.getGoalDistance() / 1000f;
        float h = jogging.getGoalTime() / (1000f * 3600f);
        return kms/h;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_result_detail, menu);
        hasWhatsApp = hasWhatsApp();
        setHasObtainedLocations();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_facebook).setVisible(hasObtainedLocations);
        menu.findItem(R.id.action_map).setVisible(hasObtainedLocations);
        menu.findItem(R.id.action_whatsapp).setVisible(hasObtainedLocations && hasWhatsApp);
    }

    private boolean hasWhatsApp() {
        try {
            ctx.getPackageManager().getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public interface OnLocationClickedListener {
        void onLocationClicked(int position);
    }
}
