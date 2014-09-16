package com.carles.jogging.main;

import android.content.Context;
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
import com.carles.jogging.util.FormatUtil;
import com.carles.jogging.util.PrefUtil;

/**
 * Created by carles1 on 20/04/14.
 */
public class MainFragment extends BaseFragment {

    private Context ctx;
    private Spinner kmsEdit;

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_main, container, false);
        ctx = getActivity();

        final Button runButton = (Button) view.findViewById(R.id.btn_main_run);
        kmsEdit = (Spinner) view.findViewById(R.id.spin_main_kms);

        runButton.setOnClickListener(new OnRunButtonClickListener());
        kmsEdit.setSelection(PrefUtil.getLastKilometersSelectedPosition(getActivity()));

        return view;
    }

    /*- ********************************************************** */
    /*- ********************************************************** */
    private class OnRunButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String sKilometers = String.valueOf(kmsEdit.getSelectedItem());
            PrefUtil.setLastKilometersSelectedPosition(ctx, kmsEdit.getSelectedItemPosition());

            Intent intent = new Intent(getActivity(), CheckConnectionsActivity.class);
            intent.putExtra(C.EXTRA_DISTANCE_TEXT, sKilometers);
            intent.putExtra(C.EXTRA_DISTANCE_IN_METERS, FormatUtil.textDistanceToMeters(getActivity(), sKilometers));
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.slide_activity_to_left_in, R.anim.slide_activity_to_left_out);
        }
    }

}