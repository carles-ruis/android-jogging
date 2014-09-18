package com.carles.jogging.jogging;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.view.MenuItem;
import com.carles.jogging.BaseActivity;
import com.carles.jogging.C;
import com.carles.jogging.R;
import com.carles.jogging.model.JoggingModel;
import com.carles.jogging.result.ResultDetailActivity;
import com.carles.jogging.util.FormatUtil;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

/**
 * Created by carles1 on 20/04/14.
 */
public class JoggingActivity extends BaseActivity implements LocationService.Client {

    private static final String TAG = JoggingActivity.class.getSimpleName();
    private static final String TAG_CANCEL_RUN_DIALOG = "tag_cancel_run_dialog";

    private Context ctx;

    // LocationService binding
    private LocationService service;
    private boolean isServiceBound = false;
    private LocationServiceConnection serviceConnection = new LocationServiceConnection();

    private TextView txtImproveTime;
    private TextView txtOnYourMarks;
    private TextView txtGetSet;
    private TextView txtGo;
    private Button btnCancelRun;
    private TextView txtKilometers;
    private TextView txtTime;

//    private boolean isKilometerReceiverRegistered = false;
//    private BroadcastReceiver kilometerReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            int meters = intent.getIntExtra(C.EXTRA_DISTANCE_IN_METERS, 0);
//            String time = intent.getStringExtra(C.EXTRA_FOOTING_TIME_TEXT);
//            txtKilometers.setText(getString(R.string.jogging_kilometers_run, meters));
//            txtTime.setText(getString(R.string.jogging_time, time));
//        };
//    };

    // handler for timing the countdown before start running
    final Handler handler = new Handler();
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
            // Start LocationService and bind to it. Set flag BIND_AUTO_CREATE to avoid leaking the
            // serviceConnection. BIND_AUTO_CREATE don't call the onStartCommand() service method
            Log.e("carles","countdown go");
            Intent intent = new Intent(JoggingActivity.this, LocationService.class);
            intent.putExtras(getIntent());
            // TODO delete this? only invoke with bindService? service is going to keep foreground?
            //startService(intent);
            Log.e("carles","about to bind service");
            bindService(intent, serviceConnection, BIND_AUTO_CREATE);

            // update view
            txtGo.setVisibility(View.VISIBLE);
            btnCancelRun.setVisibility(View.VISIBLE);

            // starting race gunshot shound
            if (startSoundId != -1) {
                soundPool.play(startSoundId, C.VOLUME, C.VOLUME, 1, 0, 1f);
            }

            // register receiver
//            if (!isKilometerReceiverRegistered) {
//                isKilometerReceiverRegistered = true;
//                LocalBroadcastManager.getInstance(ctx).registerReceiver(kilometerReceiver, new IntentFilter(C.ACTION_UPDATE_KILOMETERS_RUN));
//            }
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jogging);
        ctx = this;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_jogging);

        // mapping views
        txtImproveTime = (TextView) findViewById(R.id.txt_jogging_improve_time);
        txtOnYourMarks = (TextView) findViewById(R.id.txt_jogging_on_your_marks);
        txtGetSet = (TextView)findViewById(R.id.txt_jogging_get_set);
        txtGo = (TextView) findViewById(R.id.txt_jogging_go);
        txtKilometers = (TextView) findViewById(R.id.txt_jogging_kilometers_run);
        txtTime = (TextView) findViewById(R.id.txt_jogging_time);

        btnCancelRun = (Button) findViewById(R.id.btn_jogging_cancel);
        btnCancelRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new CancelRunDialog().show(getSupportFragmentManager(), TAG_CANCEL_RUN_DIALOG);
            }
        });

        String distanceInTxt = getIntent().getStringExtra(C.EXTRA_DISTANCE_TEXT);
        txtImproveTime.setText(getString(R.string.jogging_improve_time, distanceInTxt));

        // Load the starting sound
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        soundPool = new SoundPool(C.MAX_SOUND_STREAMS, AudioManager.STREAM_MUSIC, 0);
        startSoundId = soundPool.load(ctx, R.raw.sound_starting_pistol, 1);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!isServiceBound) {
            // restart the countdown if countdown was not completed, restart it
            txtOnYourMarks.setVisibility(View.INVISIBLE);
            txtGetSet.setVisibility(View.INVISIBLE);
            txtGo.setVisibility(View.INVISIBLE);
            btnCancelRun.setVisibility(View.INVISIBLE);
            startCountdown();
        }
    }

    private void startCountdown() {
        txtImproveTime.setVisibility(View.VISIBLE);
        // handler enqueues messages which will be executed in the UI thread
        handler.postDelayed(countdownOnYourMarksThread, C.COUNTDOWN_STOP_MILLISECONDS);
        handler.postDelayed(countdownGetSetThread, C.COUNTDOWN_STOP_MILLISECONDS * 2);
        handler.postDelayed(countdownGoThread, C.COUNTDOWN_STOP_MILLISECONDS * 3);
        handler.postDelayed(countdownDoneUpdateTextsThread, C.COUNTDOWN_STOP_MILLISECONDS * 10);
    }

    @Override
    public void onPause() {
        if (!isServiceBound) {
            handler.removeCallbacks(countdownOnYourMarksThread);
            handler.removeCallbacks(countdownGetSetThread);
            handler.removeCallbacks(countdownGoThread);
            handler.removeCallbacks(countdownDoneUpdateTextsThread);
        }
        super.onPause();
    }

//    @Override
//    public void onDetach() {
//        // avoid "fragment not attached to activity IllegalStateException"
//        handler.removeCallbacks(countdownOnYourMarksThread);
//        handler.removeCallbacks(countdownGetSetThread);
//        handler.removeCallbacks(countdownGoThread);
//        handler.removeCallbacks(countdownDoneUpdateTextsThread);
//        super.onDetach();
//    }

    @Override
    protected void onDestroy() {
//        if (isKilometerReceiverRegistered) {
//            isKilometerReceiverRegistered = false;
//            LocalBroadcastManager.getInstance(ctx).unregisterReceiver(kilometerReceiver);
//        }
        unbindService();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // override the back button in order to show confirmation
        new CancelRunDialog().show(getSupportFragmentManager(), TAG_CANCEL_RUN_DIALOG);
    }

    /*- ********************************************************************************* */
    /*- ********************************************************************************* */
    private class CancelRunDialog extends DialogFragment {

        public CancelRunDialog() {}

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Dialog dialog = new Dialog(getActivity());

            final LayoutInflater inflater = getActivity().getLayoutInflater();
            final View view = inflater.inflate(R.layout.dialog_alert_custom, null);
            final TextView title = (TextView) view.findViewById(R.id.dlg_title);
            final TextView msg = (TextView) view.findViewById(R.id.dlg_msg);
            final Button btnOk = (Button) view.findViewById(R.id.btn_yes);
            final Button btnNo = (Button) view.findViewById(R.id.btn_no);

            title.setText(getString(R.string.cancel_run_title));
            msg.setText(getString(R.string.cancel_run_msg));
            btnOk.setText(getString(R.string.cancel_run_button_yes));
            btnNo.setText(getString(R.string.cancel_run_button_no));

            btnOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                    if (checkServiceBound()) {
                        service.cancelRun();
                    }
                }
            });

            btnNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(view);
            dialog.getWindow().getAttributes().windowAnimations = R.style.Theme_Jogging_ZoomedDialog;
            return dialog;
        }
    }
//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//
//        builder.setTitle(getString(R.string.cancel_run_title));
//        builder.setMessage(getString(R.string.cancel_run_msg));
//
//        builder.setPositiveButton(R.string.cancel_run_button_yes, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                if (checkServiceBound()) {
//                    service.cancelRun();
//                }
//            }
//        });
//
//        builder.setNegativeButton(R.string.cancel_run_button_no, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                dismiss();
//            }
//        });
//
//        final Dialog dialog = builder.create();
//        dialog.getWindow().getAttributes().windowAnimations = R.style.Theme_Jogging_ZoomedDialog;
//        return dialog;
//    }
//}

    /*- ********************************************************************************* */
    /*- ********************************************************************************* */
    private class LocationServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.e("carles","service was bound");
            LocationService.LocationServiceBinder binder = (LocationService.LocationServiceBinder) iBinder;
            service = binder.getService();
            service.setClient(JoggingActivity.this);
//            service.start(getIntent());
            isServiceBound = true;
        }

        @Override
        // onServiceDisconnected is only called when a crash cause a service unbind
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e("carles", "service was unbound");
            Log.i(TAG, "LocationServiceConnection onServiceDisconnected");
            isServiceBound = false;
        }
    }

    @Override
    public void onLocationObtained(long time, float meters) {
        txtKilometers.setText(getString(R.string.jogging_kilometers_run, (int) meters));
        txtTime.setText(getString(R.string.jogging_time, FormatUtil.time(time)));
    }

    @Override
    public void onRunningFinished(Bundle extras) {
        Log.e("carles","on running finished");
        if (checkServiceBound()) {
            unbindService();
            Log.e("carles", "creating new intent");
            Intent newIntent = new Intent(this, ResultDetailActivity.class);
            Log.e("carles", "footing result is " + (FootingResult) extras.getSerializable(C.EXTRA_FOOTING_RESULT));
            newIntent.putExtras(extras);
            trackRunningFinished(extras);
            startActivity(newIntent);
            finish();
            overridePendingTransition(R.anim.slide_activity_to_left_in, R.anim.slide_activity_to_left_out);
        }
    }

    private void unbindService() {
        Log.e("carles", "is service bound?" + isServiceBound);
        if (isServiceBound) {
            Log.e("carles","unbind service before destroying the activity");
            unbindService(serviceConnection);
            isServiceBound = false;
        }
    }

    private void trackRunningFinished(Bundle extras) {
        Parcelable parcel = extras.getParcelable(C.EXTRA_JOGGING_TOTAL);
        if (parcel != null) {
            JoggingModel jogging = (JoggingModel) parcel;
            if (jogging.getFootingResult() == FootingResult.SUCCESS) {
                Tracker tracker = EasyTracker.getInstance(ctx);
                Long value = (long) jogging.getTotalDistance();
                MapBuilder builder = MapBuilder.createEvent("jogging", "finish", null, value);
                tracker.send(builder.build());

                Log.i(TAG, "Tracking running finished for FootingResult success. Distance = " + value);
            }
        }
    }

    private boolean checkServiceBound() {
        if (isServiceBound) {
            return true;
        } else {
            Log.e(TAG, "Service was unbound unexpectedly");
            finish();
            return false;
        }
    }
}