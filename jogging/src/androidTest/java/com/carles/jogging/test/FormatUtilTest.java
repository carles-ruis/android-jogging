package com.carles.jogging.test;

import com.carles.jogging.util.FormatUtil;
import com.carles.jogging.util.LocationHelper;

import junit.framework.TestCase;

/**
 * Created by carles1 on 17/05/14.
 */
public class FormatUtilTest extends TestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {super.tearDown();}

    public void testFormatRunningTime_() {
        String t1 = FormatUtil.runningTime(3600000l);
        assertEquals("1:00:00", t1);

        String t2 = FormatUtil.runningTime(60000l);
        assertEquals("  01:00", t2);

        String t3 = FormatUtil.runningTime(30000l);
        assertEquals("  00:30", t3);
    }

}
