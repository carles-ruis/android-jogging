package com.carles.jogging.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;

import com.carles.jogging.C;
import com.carles.jogging.R;
import com.carles.jogging.BaseFragment;
import com.carles.jogging.jogging.first_location.CheckConnectionsActivity;
import com.carles.jogging.common.LocationHelper;

/**
 * Created by carles1 on 20/04/14.
 */
public class MainFragment extends BaseFragment {

    private Spinner kmsEdit;

    public static MainFragment newInstance(String title) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString(C.ARGS_ACTION_BAR_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_main, container, false);

        final Button runButton = (Button) view.findViewById(R.id.main_button_run);
        runButton.setOnClickListener(new OnRunButtonClickListener());

        kmsEdit = (Spinner) view.findViewById(R.id.main_spinner_kms);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).setActionBarTitle(getArguments().getString(C.ARGS_ACTION_BAR_TITLE));
    }

    /*- ********************************************************** */
    /*- ********************************************************** */
    private class OnRunButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String sKilometers = String.valueOf(kmsEdit.getSelectedItem());

            Intent intent = new Intent(getActivity(), CheckConnectionsActivity.class);
            intent.putExtra(C.EXTRA_DISTANCE_TEXT, sKilometers);
            intent.putExtra(C.EXTRA_DISTANCE_IN_METERS, LocationHelper.textDistanceToMeters(getActivity(), sKilometers));
            startActivity(intent);
          }
    }

}