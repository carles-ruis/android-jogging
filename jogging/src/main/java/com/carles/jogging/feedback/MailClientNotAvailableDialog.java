package com.carles.jogging.feedback;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.carles.jogging.R;

/**
 * Created by carles on 30/09/2014.
 */
public class MailClientNotAvailableDialog extends DialogFragment {

    public static MailClientNotAvailableDialog newInstance() {
        MailClientNotAvailableDialog instance = new MailClientNotAvailableDialog();
        return instance;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity());

        final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_custom, null);
        final TextView dlgTitle = (TextView) view.findViewById(R.id.dlg_title);
        final TextView dlgMsg = (TextView) view.findViewById(R.id.dlg_msg);

        dlgTitle.setText(R.string.no_mail_client_title);
        dlgMsg.setText(R.string.no_mail_client_msg);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);

        // cancel the dialog if the user touches inside it as well as outside it
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                getActivity().finish();
            }
        });
        dialog.setCanceledOnTouchOutside(true);

        dialog.getWindow().getAttributes().windowAnimations = R.style.Theme_Jogging_ZoomedDialog;
        return dialog;
    }
}


