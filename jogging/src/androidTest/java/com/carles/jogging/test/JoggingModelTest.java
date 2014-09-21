package com.carles.jogging.test;

import android.location.Location;

import com.carles.jogging.jogging.FootingResult;
import com.carles.jogging.model.JoggingModel;

import junit.framework.TestCase;

import java.util.Arrays;

/**
 * Created by carles on 21/09/2014.
 */
public class JoggingModelTest extends TestCase {

    private JoggingModel p0, p1, p2;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        Location l0 = createLocation(0l);
        Location l1 = createLocation(5000l);
        Location l2 = createLocation(50000l);
        Location l3 = createLocation(60000l);

        p0 = new JoggingModel();
        p0.setTotalDistance(10f);
        p0.setTotalTime(5000l);
        p0.setStart(l0);
        p0.setEnd(l1);

        p1 = new JoggingModel();
        p1.setTotalDistance(100f);
        p1.setTotalTime(50000l);
        p1.setStart(l1);
        p1.setEnd(l2);

        p2 = new JoggingModel();
        p2.setTotalDistance(200f);
        p2.setTotalTime(60000l);
        p2.setStart(l2);
        p2.setEnd(l3);
    }

    private Location createLocation(long millis) {
        Location ret = new Location("FUSED");
        ret.setLongitude(41l);
        ret.setLatitude(2l);
        ret.setTime(millis);
        return ret;
    }

    @Override
    public void tearDown() throws Exception {super.tearDown();}

    public void testJoggingModelConstructor_calcTotalDistance() {
        // user ran 5sec for 10m , that's 1sec for 2m
        JoggingModel j = new JoggingModel(Arrays.asList(p0), 2f, FootingResult.SUCCESS, null);
        assertEquals(2f, j.getTotalDistance());
        assertEquals(1000l, j.getTotalTime());

        // user ran 45sec for 90m, that's *5sec* for 10m
        j = new JoggingModel(Arrays.asList(p0, p1), 20f, FootingResult.SUCCESS, null);
        assertEquals(20f, j.getTotalDistance());
        assertEquals(10000l, j.getTotalTime());

        // user ran 10sec for 100m, that's *8sec* for  80m
        j = new JoggingModel(Arrays.asList(p0, p1, p2), 180f, FootingResult.SUCCESS, null);
        assertEquals(180f, j.getTotalDistance());
        assertEquals(58000l, j.getTotalTime());

        // user ran was not successful, don't extrapolate totalTime
        j = new JoggingModel(Arrays.asList(p0, p1, p2), 180f, FootingResult.CANCELLED_BY_USER, null);
        assertEquals(200f, j.getTotalDistance());
        assertEquals(60000l, j.getTotalTime());

    }

}
