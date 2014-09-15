package com.carles.jogging.jogging.first_location;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;

import com.carles.jogging.C;
import com.carles.jogging.R;

/**
 * Created by carles1 on 4/09/14.
 */
public class ActivateGpsDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.activate_gps_title));
            builder.setMessage(getString(R.string.activate_gps_msg));

            builder.setPositiveButton(getString(R.string.activate_gps_button_yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {

                    // Show location settings when the user acknowledges the alert dialog
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);

                    // Don't invoke directly from the fragment, use getActivity()
                    // If not, requestCode is wrong when onActivityResult() is called
                    getActivity().startActivityForResult(intent, C.REQ_CODE_ENABLE_GPS);
                }
            });

            builder.setNegativeButton(getString(R.string.activate_gps_button_no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // user doesn't want to activate gps
                    ((CheckConnectionsActivity)getActivity()).showConnectionFailedDialog(getString(R.string.connection_failed_gps));
                }
            });

            // should invoke setCancelable in the DialogFragment directly, not in the inner Dialog
            // in order to avoid dismissing the dialog when user presses back button
            this.setCancelable(false);

            final Dialog dialog = builder.create();
            dialog.getWindow().getAttributes().windowAnimations = R.style.Theme_Jogging_ZoomedDialog;
            return dialog;
        }
}
