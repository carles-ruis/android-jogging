package com.carles.jogging.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import com.carles.jogging.R;

/**
 * Created by carles1 on 21/04/14.
 */
public class FirstLocationNotObtainedDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_location_not_obtained, null);

        builder.setView(view);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*- a dialog is dismissed directly from the code ie when the user presses the negative button */
                dismiss();
                getActivity().finish();
            }
        });

        Dialog alert = builder.create();
        alert.setCanceledOnTouchOutside(true);

    /*- Workaround to make animation work in api<11 */
        alert.getWindow().getAttributes().windowAnimations = R.style.Theme_Jogging_ZoomedDialog;

        return alert;

    }

    @Override
    /*- a dialog is cancelled when the user presses the back button*/
    public void onCancel(DialogInterface dialog) {
        getActivity().finish();
    }
}
