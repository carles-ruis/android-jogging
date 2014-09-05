package com.carles.jogging.test;

import com.carles.jogging.common.LocationHelper;

import junit.framework.TestCase;

/**
 * Created by carles1 on 17/05/14.
 */
public class LocationHelperTest extends TestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testFormatRunningTime_() {
        String t1 = LocationHelper.formatRunningTime(3600000l);
        assertEquals("1:00:00", t1);

        String t2 = LocationHelper.formatRunningTime(60000l);
        assertEquals("  01:00", t2);

        String t3 = LocationHelper.formatRunningTime(30000l);
        assertEquals("  00:30", t3);
    }

@Override
    public void tearDown() throws Exception {super.tearDown();}

}
