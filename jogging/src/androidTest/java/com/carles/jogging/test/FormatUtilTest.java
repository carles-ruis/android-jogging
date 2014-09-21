package com.carles.jogging.test;

import com.carles.jogging.util.FormatUtil;

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

    public void testTime_() {
        String t1 = FormatUtil.time(3600000l);
        assertEquals("1:00:00", t1);

        String t2 = FormatUtil.time(60000l);
        assertEquals("0:01:00", t2);

        String t3 = FormatUtil.time(30000l);
        assertEquals("0:00:30", t3);
    }

}
