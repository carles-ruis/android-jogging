package com.carles.jogging.jogging;

import com.carles.jogging.BaseFragment;

/**
 * Created by carles1 on 27/04/14.
 */
public class JoggingFragment extends BaseFragment {
    public interface Callbacks {
        void startGetLocationsService();
        void cancelRun();
    }

}