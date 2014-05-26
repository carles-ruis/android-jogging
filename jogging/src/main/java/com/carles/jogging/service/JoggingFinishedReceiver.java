package com.carles.jogging.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.carles.jogging.activity.ResultActivity;

/**
 * Created by carles1 on 3/05/14.
 */
public class JoggingFinishedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent activityIntent = new Intent(intent);
        activityIntent.setClass(context, ResultActivity.class);
        context.startActivity(activityIntent);
    }
}
