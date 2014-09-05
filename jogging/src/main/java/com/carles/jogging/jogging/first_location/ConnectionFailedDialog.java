package com.carles.jogging.jogging.first_location;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.carles.jogging.R;

/**
 * Created by carles1 on 21/04/14.
 */
public class ConnectionFailedDialog extends DialogFragment {

    private static final String CONNECTION_TYPE = "connection_type";

    public static ConnectionFailedDialog newInstance(String connectionType) {
        ConnectionFailedDialog ret = new ConnectionFailedDialog();
        Bundle args = new Bundle();
        args.putString(CONNECTION_TYPE, connectionType);
        ret.setArguments(args);
        return ret;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String connectionType = getArguments().getString(CONNECTION_TYPE);

        /*- Use the Builder class for convenient dialog construction */
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_connection_failed, null);

        final TextView msg = (TextView) view.findViewById(R.id.connection_failed_msg);
        msg.setText(getString(R.string.connection_failed_msg, connectionType));

        builder.setView(view);

        /*- cancel the dialog if the user touches inside it as well as outside it */
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                getActivity().finish();
            }
        });

        final Dialog alert = builder.create();
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
