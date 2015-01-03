package com.carles.jogging.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.carles.jogging.BaseFragment;
import com.carles.jogging.C;
import com.carles.jogging.R;
import com.carles.jogging.jogging.first_location.CheckConnectionsActivity;
import com.carles.jogging.util.PrefUtil;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.mediation.admob.AdMobExtras;

/**
 * Created by carles1 on 20/04/14.
 */
public class MainFragment extends BaseFragment {

    private static final int MIN_WINDOW_VISIBLE_SPACE_TO_SHOW_IMAGE = 600;

    private AdView adView;

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_main, container, false);

        loadViews(view);

        createAdRequest();

        return view;
    }

    private void loadViews(View view) {
        final Button btnRun = (Button) view.findViewById(R.id.btn_main_run);
        final RadioGroup rgKms = (RadioGroup) view.findViewById(R.id.rg_kms);
        final RadioButton rbNoDistance = (RadioButton) view.findViewById(R.id.rb_no_distance);
        final ViewGroup lytKms = (ViewGroup) view.findViewById(R.id.lyt_kms);
        final SeekBar seekKms = (SeekBar) view.findViewById(R.id.seek_kms);
        final TextView txtKms = (TextView) view.findViewById(R.id.txt_kms);
        adView = (AdView) view.findViewById(R.id.adView);

        final Context ctx = getActivity().getApplicationContext();
        // run button
        btnRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // update sharedPreferences with user selection
                int kms = 0;
                if (lytKms.getVisibility() == View.VISIBLE) {
                    kms = seekKms.getProgress() + 1;
                    PrefUtil.setLastKilometersSelected(ctx, kms);
                    PrefUtil.setDistanceSelected(ctx, true);
                } else {
                    kms = C.NO_DISTANCE;
                    PrefUtil.setDistanceSelected(ctx, false);
                }

                // send the intent
                Intent intent = new Intent(getActivity(), CheckConnectionsActivity.class);
                intent.putExtra(C.EXTRA_KILOMETERS, kms);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_activity_to_left_in, R.anim.slide_activity_to_left_out);
            }
        });

        // select distance or not radio buttons
        rgKms.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (checkedId == R.id.rb_distance) {
                    lytKms.setVisibility(View.VISIBLE);
                } else {
                    lytKms.setVisibility(View.INVISIBLE);
                }
            }
        });

        // select distance seekBar
        seekKms.setProgress(PrefUtil.getLastKilometersSelected(ctx) - 1);
        if (!PrefUtil.getDistanceSelected(ctx)) {
            rbNoDistance.setChecked(true);
            lytKms.setVisibility(View.INVISIBLE);
        }
        seekKms.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                if (progress == 0) {
                    txtKms.setText("1 Km");
                } else {
                    txtKms.setText(new StringBuilder().append(progress + 1).append(" Kms").toString());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        int lastKmsSelected = PrefUtil.getLastKilometersSelected(ctx);
        if (lastKmsSelected == 1) {
            txtKms.setText("1 Km");
        } else {
            txtKms.setText(new StringBuilder().append(PrefUtil.getLastKilometersSelected(ctx)).append(" Kms"));
        }
    }

    private void createAdRequest() {
        Bundle bundle = new Bundle();
        bundle.putString("color_bg", getString(R.string.grey_light));
        bundle.putString("color_bg_top", getString(R.string.grey_light));
        bundle.putString("color_border", getString(R.string.grey_light));
        bundle.putString("color_link", getString(R.string.red_dark));
        bundle.putString("color_text", getString(R.string.grey));
        bundle.putString("color_url", getString(R.string.grey));
        AdMobExtras extras = new AdMobExtras(bundle);

        AdRequest adRequest = new AdRequest.Builder().addNetworkExtras(extras).build();

        // Start loading the ad in the background.
        adView.loadAd(adRequest);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
    }

    @Override
    public void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

}