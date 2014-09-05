package com.carles.jogging.jogging;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.carles.jogging.R;
import com.carles.jogging.jogging.JoggingActivity;

/**
 * Created by carles1 on 5/05/14.
 */
public class CancelRunDialog extends DialogFragment {

    public CancelRunDialog() {}

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(getString(R.string.cancel_run_title));
        builder.setMessage(getString(R.string.cancel_run_msg));

        builder.setPositiveButton(R.string.cancel_run_button_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Activity parent = getActivity();
                if (parent instanceof JoggingActivity) {
                    ((JoggingActivity) parent).cancelRun();
                }
            }
        });

        builder.setNegativeButton(R.string.cancel_run_button_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dismiss();
            }
        });

        final Dialog dialog = builder.create();
        return dialog;
    }
}


