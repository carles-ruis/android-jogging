package com.carles.jogging.last_times;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.carles.jogging.R;
import com.carles.jogging.model.JoggingModel;
import com.carles.jogging.model.JoggingSQLiteHelper;
import com.carles.jogging.model.UserModel;
import com.carles.jogging.util.FormatUtil;

import java.util.List;

/**
 * Created by carles1 on 10/09/14.
 */
//public class LastTimesPagerAdapter extends PagerAdapter {
public class LastTimesPagerAdapter extends FragmentPagerAdapter {

    private Context ctx;
    private String[] titles;


    public LastTimesPagerAdapter(Context ctx, FragmentManager fm) {
        super(fm);
        this.ctx = ctx;
        titles = ctx.getResources().getStringArray(R.array.main_entries_kms);
    }

    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    @Override
    public Fragment getItem(int position) {
        final long meters = FormatUtil.textDistanceToMeters(ctx, titles[position]);
        return LastTimesContentFragment.newInstance(meters);
    }

}
