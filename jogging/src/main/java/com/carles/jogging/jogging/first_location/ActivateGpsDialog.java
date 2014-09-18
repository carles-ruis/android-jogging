package com.carles.jogging.jogging.first_location;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.carles.jogging.C;
import com.carles.jogging.R;

/**
 * Created by carles1 on 4/09/14.
 */
public class ActivateGpsDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity());

        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_alert_custom, null);
        final TextView title = (TextView) view.findViewById(R.id.dlg_title);
        final TextView msg = (TextView) view.findViewById(R.id.dlg_msg);
        final Button btnOk = (Button)view.findViewById(R.id.btn_yes);
        final Button btnNo = (Button) view.findViewById(R.id.btn_no);

        title.setText(getString(R.string.activate_gps_title));
        msg.setText(getString(R.string.activate_gps_msg));
        btnOk.setText(getString(R.string.activate_gps_button_yes));
        btnNo.setText(getString(R.string.activate_gps_button_no));

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                // Show location settings when the user acknowledges the alert dialog
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);

                // Don't invoke directly from the fragment, use getActivity()
                // If not, requestCode is wrong when onActivityResult() is called
                getActivity().startActivityForResult(intent, C.REQ_CODE_ENABLE_GPS);
            }
        });

        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                // user doesn't want to activate gps
                FirstLocationFailedDialog.newInstance(Error.GPS_DISABLED).show(getActivity().getSupportFragmentManager(), C.TAG_CONNECTION_FAILED_DIALOG);
            }
        });

        // should invoke setCancelable in the DialogFragment directly, not in the inner Dialog
        // in order to avoid dismissing the dialog when user presses back button
        this.setCancelable(false);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);
        dialog.getWindow().getAttributes().windowAnimations = R.style.Theme_Jogging_ZoomedDialog;
        return dialog;
    }

}
