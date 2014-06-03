package com.carles.jogging.fragment;

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
import com.carles.jogging.activity.JoggingActivity;
import com.carles.jogging.dialog.CancelRunDialog;
import com.carles.jogging.util.Log;

/**
 * Created by carles1 on 27/04/14.
 */
public class JoggingFragment extends BaseFragment {

    public static final float VOLUME = 1.0f;
    private static final int MAX_SOUND_STREAMS = 1;

    /*- handle for timing the countdown before start running */
    final Handler handler = new Handler();
    private boolean isRunning = false;
    private SoundPool soundPool;
    private int startSoundId = -1;

    private Runnable countdownGoThread = new Runnable() {
        @Override
        public void run() {
            getView().findViewById(R.id.jogging_go).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.jogging_button_cancel).setVisibility(View.VISIBLE);

            if (startSoundId != -1) {
                soundPool.play(startSoundId, VOLUME, VOLUME, 1, 0, 1f);
            }

            ((JoggingActivity) getActivity()).startGetLocationsService();
            isRunning = true;

        }
    };

    private Button cancelRunButton;

    private Runnable countdownOnYourMarksThread = new Runnable() {
        @Override
        public void run() {
            getView().findViewById(R.id.jogging_on_your_marks).setVisibility(View.VISIBLE);
        }
    };

    private Runnable countdownGetSetThread = new Runnable() {
        @Override
        public void run() {
            getView().findViewById(R.id.jogging_get_set).setVisibility(View.VISIBLE);
        }
    };

    /*- ************************************************************************************************************** */
    /*- ************************************************************************************************************** */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_jogging, container, false);

        String distanceInTxt = getActivity().getIntent().getStringExtra(C.EXTRA_DISTANCE_TEXT);
        final TextView improveText = (TextView) view.findViewById(R.id.jogging_improve_time);
        improveText.setText(getString(R.string.jogging_improve_time, distanceInTxt));

        cancelRunButton = (Button) view.findViewById(R.id.jogging_button_cancel);
        cancelRunButton.findViewById(R.id.jogging_button_cancel);
        cancelRunButton.setOnClickListener(new CancelRunButtonOnClickListener());

       /*- Load the starting sound */
        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
        soundPool = new SoundPool(C.MAX_SOUND_STREAMS, AudioManager.STREAM_MUSIC, 0);
        startSoundId = soundPool.load(getActivity(), R.raw.sound_starting_pistol, 1);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isRunning == false) {
            View view = getView();
            view.findViewById(R.id.jogging_improve_time).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.jogging_on_your_marks).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.jogging_get_set).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.jogging_go).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.jogging_button_cancel).setVisibility(View.INVISIBLE);

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

    private void startCountdown() {
        getView().findViewById(R.id.jogging_improve_time).setVisibility(View.VISIBLE);

        /*- handler enqueues messages which will be executed in the UI thread */
        handler.postDelayed(countdownOnYourMarksThread, C.COUNTDOWN_STOP_MILLISECONDS);
        handler.postDelayed(countdownGetSetThread, C.COUNTDOWN_STOP_MILLISECONDS * 2);
        handler.postDelayed(countdownGoThread, C.COUNTDOWN_STOP_MILLISECONDS * 3);

    }

    /*- ************************************************************************************************************** */
    /*- ************************************************************************************************************** */
    private class CancelRunButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            CancelRunDialog dialog = new CancelRunDialog();
            dialog.show(getFragmentManager(), C.TAG_CANCEL_RUN_DIALOG);
        }
    }

}