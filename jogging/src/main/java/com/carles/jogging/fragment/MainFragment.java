package com.carles.jogging.fragment;

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
import com.carles.jogging.activity.MainActivity;
import com.carles.jogging.activity.RunActivity;
import com.carles.jogging.helper.ConversionHelper;

/**
 * Created by carles1 on 20/04/14.
 */
public class MainFragment extends BaseFragment {

    private Spinner kmsEdit;

    public MainFragment() {}

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

        final Button runButton = (Button) view.findViewById(R.id.run_button);
        runButton.setOnClickListener(new OnRunButtonClickListener());

        kmsEdit = (Spinner) view.findViewById(R.id.kilometers_edit);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).setActionBarTitle(getArguments().getString(C.ARGS_ACTION_BAR_TITLE));
    }

    /*- ************************************************************************************************************** */
    /*- ************************************************************************************************************** */
    private class OnRunButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), RunActivity.class);
            String sKilometers = String.valueOf(kmsEdit.getSelectedItem());
            intent.putExtra(C.EXTRA_KILOMETERS_TEXT, sKilometers);
            intent.putExtra(C.EXTRA_METERS, ConversionHelper.textDistanceToMeters(getActivity(), sKilometers));
            startActivity(intent);
          }
    }

}