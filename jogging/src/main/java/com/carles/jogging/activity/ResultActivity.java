package com.carles.jogging.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import com.carles.jogging.C;
import com.carles.jogging.R;
import com.carles.jogging.util.Log;

/**
 * Created by carles1 on 20/04/14.
 */
public class ResultActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("onCreate RESULT ACTIVITY");

        setContentView(R.layout.activity_result);

        Bundle extras = getIntent().getExtras();
        int minutes = (int) (extras.getLong(C.EXTRA_DISTANCE_IN_METERS) / 1000 / 60);

        StringBuilder results = new StringBuilder();
        results.append(extras.getString(C.EXTRA_DISTANCE_IN_METERS)).append(" - ");
        results.append(extras.getFloat(C.EXTRA_DISTANCE_TEXT)).append(" - ");
        results.append(extras.getLong(C.EXTRA_FOOTING_TIME)).append(" - ");
        results.append(extras.getParcelableArray(C.EXTRA_PARTIALS));
        results.append("" + minutes + "min - ");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(extras.getSerializable(C.EXTRA_FOOTING_RESULT).toString());
        builder.setMessage(results.toString());

        Dialog dialog = builder.create();
        dialog.show();

    }
}
