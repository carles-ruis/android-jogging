package com.carles.jogging.feedback;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.actionbarsherlock.view.Window;
import com.carles.jogging.BaseActivity;
import com.carles.jogging.R;

import org.apache.commons.lang3.StringUtils;

public class FeedbackActivity extends BaseActivity {

    private static final String TAG = FeedbackActivity.class.getSimpleName();

    private static final String FEEDBACK_EMAIL = "crles.android@gmail.com";
    private static final String FEEDBACK_SUBJECT = "User feedback:";

    private EditText edFeedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // avoid IllegalStateException caused by combining dialog-styled activity with AB sherlock
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_feedback);

        // load views
        edFeedback = (EditText) findViewById(R.id.ed_feedback);
        final ImageView imgCancel = (ImageView) findViewById(R.id.img_cancel);
        final Button btnSend = (Button) findViewById(R.id.btn_send);

        // event listeners
        edFeedback.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void onTextChanged(CharSequence s, int i, int i2, int i3) {
                btnSend.setEnabled(StringUtils.isNotBlank(s));
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        imgCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(0, R.anim.zoom_out);
            }
        });
    }

    public void actionSendFeedback(final View view) {
        Intent intent = new Intent(Intent.ACTION_SEND);

        intent.setData(Uri.parse("mailto:"));
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{FEEDBACK_EMAIL});
        intent.putExtra(Intent.EXTRA_SUBJECT, FEEDBACK_SUBJECT + getString(R.string.app_name));
        intent.putExtra(Intent.EXTRA_TEXT, edFeedback.getText());

        // send mail without chooser, send directly through gmail if device has it
//        final List<ResolveInfo> matches = getPackageManager().queryIntentActivities(intent, 0);
//        if (matches.isEmpty()) {
//            // no mail client installed in the user's device
//            setResult(C.RESULT_NO_MAIL_CLIENT);
//
//        } else {
//            ResolveInfo best = matches.get(0);
//            for (final ResolveInfo info : matches) {
//                if (info.activityInfo.packageName.endsWith(".gm") || info.activityInfo.name.toLowerCase().contains("gmail")) {
//                    best = info;
//                }
//            }
//            Log.i(TAG, "Sending feedback with client:" + best.activityInfo.name);
//            intent.setClassName(best.activityInfo.packageName, best.activityInfo.name);
            startActivity(intent);
            // email supposedly sent, we can't know for sure because ACTION_SEND intent returns anyway
            setResult(RESULT_OK);
//        }
        finish();
        overridePendingTransition(0, R.anim.zoom_out);
    }
}