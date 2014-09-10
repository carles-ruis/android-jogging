package com.carles.jogging.jogging;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.carles.jogging.C;
import com.carles.jogging.R;
import com.carles.jogging.BaseFragment;

/**
 * Created by carles1 on 27/04/14.
 */
public class JoggingFragment extends BaseFragment {

    private static final String TAG = JoggingFragment.class.getName();

    private Context ctx;
    private OnCountdownFinishedListener callbacks;

    private TextView txtImproveTime;
    private TextView txtOnYourMarks;
    private TextView txtGetSet;
    private TextView txtGo;
    private Button btnCancelRun;
    /*- update view when runner has got over a kilometer */
    private TextView txtKilometers;
    private TextView txtTime;

    private boolean isKilometerReceiverRegistered = false;
    private BroadcastReceiver kilometerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int meters = intent.getIntExtra(C.EXTRA_DISTANCE_IN_METERS, 0);
            String time = intent.getStringExtra(C.EXTRA_FOOTING_TIME_TEXT);
            txtKilometers.setText(getString(R.string.jogging_kilometers_run, meters));
            txtTime.setText(getString(R.string.jogging_time, time));
        };
    };

    /*- handle for timing the countdown before start running */
    final Handler handler = new Handler();
    private boolean isRunning = false;
    private SoundPool soundPool;
    private int startSoundId = -1;

    private Runnable countdownOnYourMarksThread = new Runnable() {
        @Override
        public void run() {
            txtOnYourMarks.setVisibility(View.VISIBLE);
        }
    };

    private Runnable countdownGetSetThread = new Runnable() {
        @Override
        public void run() {
            txtGetSet.setVisibility(View.VISIBLE);
        }
    };

    private Runnable countdownGoThread = new Runnable() {
        @Override
        public void run() {
            txtGo.setVisibility(View.VISIBLE);
            btnCancelRun.setVisibility(View.VISIBLE);

            // starting race gunshot shound
            if (startSoundId != -1) {
                soundPool.play(startSoundId, C.VOLUME, C.VOLUME, 1, 0, 1f);
            }

            // register receiver
            if (!isKilometerReceiverRegistered) {
                isKilometerReceiverRegistered = true;
                LocalBroadcastManager.getInstance(ctx).registerReceiver(kilometerReceiver, new IntentFilter(C.ACTION_UPDATE_KILOMETERS_RUN));
            }

            // start updating locations
            callbacks.startGetLocationsService();
            isRunning = true;
        }
    };

    private Runnable countdownDoneUpdateTextsThread = new Runnable() {
        @Override
        public void run() {
            txtOnYourMarks.setVisibility(View.GONE);
            txtGetSet.setVisibility(View.GONE);
            txtGo.setVisibility(View.GONE);
            txtKilometers.setText(getString(R.string.jogging_kilometers_run, 0));
            txtKilometers.setVisibility(View.VISIBLE);
            txtTime.setText(getString(R.string.jogging_time, " 0:00:00"));
            txtTime.setVisibility(View.VISIBLE);
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            callbacks = (OnCountdownFinishedListener) activity;
            ctx = activity;
        } catch (ClassCastException e) {
            Log.e(TAG, "Error: activity must implement OnCountDownFinishedListeners");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_jogging, container, false);

        txtImproveTime = (TextView) view.findViewById(R.id.txt_jogging_improve_time);
        txtOnYourMarks = (TextView) view.findViewById(R.id.txt_jogging_on_your_marks);
        txtGetSet = (TextView)view.findViewById(R.id.txt_jogging_get_set);
        txtGo = (TextView) view.findViewById(R.id.txt_jogging_go);
        txtKilometers = (TextView) view.findViewById(R.id.txt_jogging_kilometers_run);
        txtTime = (TextView) view.findViewById(R.id.txt_jogging_time);
        btnCancelRun = (Button) view.findViewById(R.id.btn_jogging_cancel);
        btnCancelRun.setOnClickListener(new CancelRunButtonOnClickListener());

        String distanceInTxt = getActivity().getIntent().getStringExtra(C.EXTRA_DISTANCE_TEXT);
        txtImproveTime.setText(getString(R.string.jogging_improve_time, distanceInTxt));

       // Load the starting sound
        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
        soundPool = new SoundPool(C.MAX_SOUND_STREAMS, AudioManager.STREAM_MUSIC, 0);
        startSoundId = soundPool.load(ctx, R.raw.sound_starting_pistol, 1);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isRunning == false) {
            txtOnYourMarks.setVisibility(View.INVISIBLE);
            txtGetSet.setVisibility(View.INVISIBLE);
            txtGo.setVisibility(View.INVISIBLE);
            btnCancelRun.setVisibility(View.INVISIBLE);

            startCountdown();
        }
    }

    private void startCountdown() {
        getView().findViewById(R.id.txt_jogging_improve_time).setVisibility(View.VISIBLE);
        /*- handler enqueues messages which will be executed in the UI thread */
        handler.postDelayed(countdownOnYourMarksThread, C.COUNTDOWN_STOP_MILLISECONDS);
        handler.postDelayed(countdownGetSetThread, C.COUNTDOWN_STOP_MILLISECONDS * 2);
        handler.postDelayed(countdownGoThread, C.COUNTDOWN_STOP_MILLISECONDS * 3);
        handler.postDelayed(countdownDoneUpdateTextsThread, C.COUNTDOWN_STOP_MILLISECONDS * 10);
    }

    @Override
    public void onPause() {
        if (isRunning == false) {
            handler.removeCallbacks(countdownOnYourMarksThread);
            handler.removeCallbacks(countdownGetSetThread);
            handler.removeCallbacks(countdownGoThread);
            handler.removeCallbacks(countdownDoneUpdateTextsThread);
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (isKilometerReceiverRegistered) {
            isKilometerReceiverRegistered = false;
            LocalBroadcastManager.getInstance(ctx).unregisterReceiver(kilometerReceiver);
        }
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        // avoid fragment not attached to activity IllegalStateException
        handler.removeCallbacks(countdownOnYourMarksThread);
        handler.removeCallbacks(countdownGetSetThread);
        handler.removeCallbacks(countdownGoThread);
        handler.removeCallbacks(countdownDoneUpdateTextsThread);
        super.onDetach();
    }

    public interface OnCountdownFinishedListener {
        void startGetLocationsService();
    }

    /*- *********************************************************************************** */
    /*- *********************************************************************************** */
    private class CancelRunButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            CancelRunDialog dialog = new CancelRunDialog();
            dialog.show(getFragmentManager(), C.TAG_CANCEL_RUN_DIALOG);
        }
    }

}