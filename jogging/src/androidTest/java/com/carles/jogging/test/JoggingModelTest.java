package com.carles.jogging.test;

import android.location.Location;

import com.carles.jogging.jogging.FootingResult;
import com.carles.jogging.model.JoggingModel;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

/**
 * Created by carles on 21/09/2014.
 */
public class JoggingModelTest extends TestCase {

    private JoggingModel p0, p1, p2, p3, p4, p5;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        Location l0 = createLocation(0l);
        Location l1 = createLocation(5000l);
        Location l2 = createLocation(50000l);
        Location l3 = createLocation(60000l);

        p0 = new JoggingModel();
        p0.setGoalDistance(10f);
        p0.setGoalTime(5000l);
        p0.setStart(l0);
        p0.setEnd(l1);

        p1 = new JoggingModel();
        p1.setGoalDistance(100f);
        p1.setGoalTime(50000l);
        p1.setStart(l1);
        p1.setEnd(l2);

        p2 = new JoggingModel();
        p2.setGoalDistance(200f);
        p2.setGoalTime(60000l);
        p2.setStart(l2);
        p2.setEnd(l3);

        p3 = new JoggingModel();
        p3.setGoalDistance(1003f);
        p3.setGoalTime(60001l);
        p3.setStart(l3);
        p3.setEnd(l3);

        p4 = new JoggingModel();
        p4.setGoalDistance(1004f);
        p4.setGoalTime(60002l);
        p4.setStart(l3);
        p4.setEnd(l3);

        p5 = new JoggingModel();
        p5.setGoalDistance(3005f);
        p5.setGoalTime(60003l);
        p5.setStart(l3);
        p5.setEnd(l3);
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
        assertEquals(2f, j.getGoalDistance());
        assertEquals(1000l, j.getGoalTime());

        // user ran 45sec for 90m, that's *5sec* for 10m
        j = new JoggingModel(Arrays.asList(p0, p1), 20f, FootingResult.SUCCESS, null);
        assertEquals(20f, j.getGoalDistance());
        assertEquals(10000l, j.getGoalTime());

        // user ran 10sec for 100m, that's *8sec* for  80m
        j = new JoggingModel(Arrays.asList(p0, p1, p2), 180f, FootingResult.SUCCESS, null);
        assertEquals(180f, j.getGoalDistance());
        assertEquals(58000l, j.getGoalTime());

        // user ran was not successful, don't extrapolate totalTime
        j = new JoggingModel(Arrays.asList(p0, p1, p2), 180f, FootingResult.CANCELLED_BY_USER, null);
        assertEquals(200f, j.getGoalDistance());
        assertEquals(60000l, j.getGoalTime());
    }

    public void testCalcPartialsForKilometer() {
        JoggingModel j = new JoggingModel(Arrays.asList(p0, p1, p2, p3, p4, p5), 3000f, FootingResult.SUCCESS, null);
        List<JoggingModel> pfk = j.getPartialsForKilometer();
        assertEquals(2, pfk.size());
        assertEquals(1003f, pfk.get(0).getGoalDistance());
        assertEquals(3005f, pfk.get(1).getGoalDistance());
    }

}
