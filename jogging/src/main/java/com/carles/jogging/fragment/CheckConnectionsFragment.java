package com.carles.jogging.fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.carles.jogging.R;

/**
 * Created by carles1 on 27/04/14.
 */
public class CheckConnectionsFragment extends BaseFragment {

    private ProgressDialog dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_check_connections, container, false);

        return view;
    }

    @Override

    public void onResume() {
        super.onResume();

        dialog = new ProgressDialog(getActivity());
        dialog.setTitle(R.string.check_connection_progress_title);
        dialog.setMessage(getString(R.string.check_connection_progress_msg));
        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.check_connection_progress_cancel), new ProgressDialogOnClickListener());
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    public void onPause() {
        super.onPause();

        /*- avoid leaking the dialog when the fragment loses foreground */
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    /*- ************************************************************************************************************** */
    /*- ************************************************************************************************************** */
    private class ProgressDialogOnClickListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            dialogInterface.dismiss();
            getActivity().finish();
        }
    }
}