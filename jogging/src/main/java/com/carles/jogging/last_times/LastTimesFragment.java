package com.carles.jogging.last_times;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.carles.jogging.R;
import com.carles.jogging.BaseFragment;

import java.lang.reflect.Field;

/**
 * Created by carles1 on 20/04/14.
 */
public class LastTimesFragment extends BaseFragment {

    private Context ctx;

    public static LastTimesFragment newInstance() {
        LastTimesFragment fragment = new LastTimesFragment();
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ctx = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_last_times, container, false);

        final LastTimesPagerAdapter adapter = new LastTimesPagerAdapter(ctx, getChildFragmentManager());
        final ViewPager viewPager = (ViewPager) view.findViewById(R.id.view_pager);
        viewPager.setAdapter(adapter);

        return view;
    }

    @Override
    // workaround to avoid bug for nested fragments
    // the child FragmentManager ends with a broken state when is detached from the activity
    public void onDetach() {
        super.onDetach();
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}

