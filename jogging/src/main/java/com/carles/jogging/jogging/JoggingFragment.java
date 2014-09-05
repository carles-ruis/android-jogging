package com.carles.jogging.jogging;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
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
            txtOnYourMarks.setVisibility(View.GONE);
            txtGetSet.setVisibility(View.GONE);
            txtGo.setVisibility(View.GONE);
            String meters = intent.getStringExtra(C.EXTRA_DISTANCE_TEXT);
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

    private Runnable countdownGoThread = new Runnable() {
        @Override
        public void run() {
            txtGo.setVisibility(View.VISIBLE);
            btnCancelRun.setVisibility(View.VISIBLE);

            if (startSoundId != -1) {
                soundPool.play(startSoundId, C.VOLUME, C.VOLUME, 1, 0, 1f);
            }

            getActivity().registerReceiver(kilometerReceiver, new IntentFilter(C.ACTION_UPDATE_KILOMETERS_RUN));
            isKilometerReceiverRegistered = true;
            txtKilometers.setText(getString(R.string.jogging_kilometers_run, "0"));
            txtKilometers.setVisibility(View.VISIBLE);
            txtTime.setText(getString(R.string.jogging_time, "0"));
            txtTime.setVisibility(View.VISIBLE);

            ((JoggingActivity) getActivity()).startGetLocationsService();
            isRunning = true;

        }
    };

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_jogging, container, false);

        txtImproveTime = (TextView) view.findViewById(R.id.jogging_improve_time);
        txtOnYourMarks = (TextView) view.findViewById(R.id.jogging_on_your_marks);
        txtGetSet = (TextView)view.findViewById(R.id.jogging_get_set);
        txtGo = (TextView) view.findViewById(R.id.jogging_go);
        txtKilometers = (TextView) view.findViewById(R.id.jogging_kilometers_run);
        txtTime = (TextView) view.findViewById(R.id.jogging_time);
        btnCancelRun = (Button) view.findViewById(R.id.jogging_button_cancel);
        btnCancelRun.setOnClickListener(new CancelRunButtonOnClickListener());

        String distanceInTxt = getActivity().getIntent().getStringExtra(C.EXTRA_DISTANCE_TEXT);
        txtImproveTime.setText(getString(R.string.jogging_improve_time, distanceInTxt));

       // Load the starting sound
        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
        soundPool = new SoundPool(C.MAX_SOUND_STREAMS, AudioManager.STREAM_MUSIC, 0);
        startSoundId = soundPool.load(getActivity(), R.raw.sound_starting_pistol, 1);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isRunning == false) {
            txtImproveTime.setVisibility(View.INVISIBLE);
            txtOnYourMarks.setVisibility(View.INVISIBLE);
            txtGetSet.setVisibility(View.INVISIBLE);
            txtGo.setVisibility(View.INVISIBLE);
            btnCancelRun.setVisibility(View.INVISIBLE);

            startCountdown();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (isRunning == false) {
            handler.removeCallbacks(countdownOnYourMarksThread);
            handler.removeCallbacks(countdownGetSetThread);
            handler.removeCallbacks(countdownGoThread);
        }
    }

    @Override
    public void onDestroy() {
        if (isKilometerReceiverRegistered) {
            getActivity().unregisterReceiver(kilometerReceiver);
        }
        super.onDestroy();
    }

    private void startCountdown() {
        getView().findViewById(R.id.jogging_improve_time).setVisibility(View.VISIBLE);

        /*- handler enqueues messages which will be executed in the UI thread */
        handler.postDelayed(countdownOnYourMarksThread, C.COUNTDOWN_STOP_MILLISECONDS);
        handler.postDelayed(countdownGetSetThread, C.COUNTDOWN_STOP_MILLISECONDS * 2);
        handler.postDelayed(countdownGoThread, C.COUNTDOWN_STOP_MILLISECONDS * 3);

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