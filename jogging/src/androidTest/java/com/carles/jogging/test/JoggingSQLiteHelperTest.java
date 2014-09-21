package com.carles.jogging.test;

import android.location.Location;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import com.carles.jogging.jogging.FootingResult;
import com.carles.jogging.model.JoggingModel;
import com.carles.jogging.model.JoggingSQLiteHelper;
import com.carles.jogging.model.UserModel;

/**
 * Created by carles1 on 9/09/14.
 */
public class JoggingSQLiteHelperTest extends AndroidTestCase {

    private static final long HALF_AN_HOUR = 1800000l;
    private static final long TEN_MINUTES = 600000l;
    private static final long FIVE_MINUTES = 300000l;

    private JoggingSQLiteHelper helper;
    private UserModel u1, u2;
    private JoggingModel j11, j12, j13, j14, j21, jError;
    private JoggingModel p111, p112, p113, p114, p211;
    private Location location;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        RenamingDelegatingContext ctx = new RenamingDelegatingContext(getContext(), "test_");
        helper = JoggingSQLiteHelper.getInstance(ctx);
        u1 = newUser("u1", "p1", "e@1");
        u2 = newUser("u2", "p2", "e@2");
        j11 = newJogging(987654321l, u1, HALF_AN_HOUR, 6000f, 0);
        j12 = newJogging(987654320l, u1, HALF_AN_HOUR + 1000, 6000f, 0);
        j13 = newJogging(987654330l, u1, HALF_AN_HOUR - 1000, 6000f, 0);
        j14 = newJogging(987654300l, u1, HALF_AN_HOUR, 8000f, 0);
        j21 = newJogging(987654000l, u2, HALF_AN_HOUR, 6000f, 0);
        p111 = newJogging(987654321001l, u1, FIVE_MINUTES, 1000f, 987654321l);
        p112 = newJogging(987654321004l, u1, FIVE_MINUTES, 1000f, 987654321l);
        p113 = newJogging(987654321003l, u1, TEN_MINUTES, 1500f, 987654321l);
        p114 = newJogging(987654321002l, u1, TEN_MINUTES, 2500f, 987654321l);
        p211 = newJogging(987654000001l, u2, HALF_AN_HOUR, 6000f, 987654000l);
        jError = newJogging(987654000l, u2, HALF_AN_HOUR + 1000, 8000f, 0);
        location = new Location("FUSED");
        location.setAccuracy(48f);
    }

    private UserModel newUser(String name, String password, String email) {
        UserModel u = new UserModel();
        u.setName(name);
        u.setPassword(password);
        u.setEmail(email);
        return u;
    }

    private JoggingModel newJogging(long id, UserModel user, long totalTime, float totalDistance, long parentId) {
        JoggingModel j = new JoggingModel();
        j.setId(id);
        j.setUser(user);
        j.setFootingResult(FootingResult.SUCCESS);
        j.setStart(location);
        j.setEnd(location);
        j.setTotalDistance(totalDistance);
        j.setTotalTime(totalTime);
        j.setParentId(parentId);
        return j;
    }

    @Override
    public void tearDown() throws Exception {
        // no need to clear tables, RenamingDelegatingContext handles ith
        helper.close();
        helper = null;
        super.tearDown();
    }

    // @Test is not necessary. System takes methods starting with "test" automatically.
    public void testInsertUser() {
        assertFalse(helper.insertUser(u1) == -1);
        assertTrue(helper.insertUser(u1) == -1);
        assertFalse(helper.insertUser(u2) == -1);
    }

    public void testQueryUser() {
        insertAll();
        assertEquals("e@1", helper.queryUser("u1", "p1").getEmail());
        assertNull(helper.queryUser("u3", "p2"));
        assertNull(helper.queryUser("u1", "p2"));
    }

    public void testInsertJogging() {
        assertFalse(helper.insertJogging(j11, null) == -1);
        assertFalse(helper.insertJogging(j12, null) == -1);
        assertFalse(helper.insertJogging(j13, null) == -1);
        assertFalse(helper.insertJogging(j21, null) == -1);
        assertTrue(helper.insertJogging(j11, null) == -1);
        assertTrue(helper.insertJogging(jError, null) == -1);
    }

    public void testQueryLastTimes() {
        insertAll();
        assertTrue(helper.queryLastTimes(u1, 5000).isEmpty());
        assertTrue(helper.queryLastTimes(u1, 6000).size() == 3);
        assertTrue(helper.queryLastTimes(u1, 8000).size() == 1);
        assertTrue(helper.queryLastTimes(u2, 6000).size() == 1);
        assertEquals(987654330l, helper.queryLastTimes(u1, 6000).get(0).getId());
        assertEquals(987654321l, helper.queryLastTimes(u1, 6000).get(1).getId());
        assertEquals(987654320l, helper.queryLastTimes(u1, 6000).get(2).getId());
    }

    public void testQueryBestTimes() {
        insertAll();
        assertTrue(helper.queryBestTimes(u2).size() == 1);
        assertTrue(helper.queryBestTimes(u1).size() == 2);
        assertTrue(helper.queryBestTimes(u1).get(0).getTotalDistance() == 8000f);
        assertTrue(helper.queryBestTimes(u1).get(1).getTotalDistance() == 6000f);
        assertTrue(helper.queryBestTimes(u1).get(1).getTotalTime() == HALF_AN_HOUR - 1000);
    }

    public void testQueryBestByDistance() {
        insertAll();
        assertEquals(HALF_AN_HOUR - 1000, helper.queryBestTimeByDistance(u1, 6000));
        assertEquals(HALF_AN_HOUR, helper.queryBestTimeByDistance(u2, 6000));
        assertEquals(HALF_AN_HOUR, helper.queryBestTimeByDistance(u1, 8000));
        assertEquals(0, helper.queryBestTimeByDistance(u1, 5000));
    }

    public void testQueryPartials() {
        insertAll();
        assertTrue(helper.queryPartials(j11).size() == 4);
        assertTrue(helper.queryPartials(j12).isEmpty());
        assertTrue(helper.queryPartials(j21).size() == 1);
        assertEquals(987654321001l, helper.queryPartials(j11).get(0).getId());
        assertEquals(987654321002l, helper.queryPartials(j11).get(1).getId());
        assertEquals(987654321003l, helper.queryPartials(j11).get(2).getId());
        assertEquals(987654321004l, helper.queryPartials(j11).get(3).getId());
    }

    private void insertAll() {
        helper.insertUser(u1);
        helper.insertUser(u2);
        helper.insertJogging(j11, null);
        helper.insertJogging(j12, null);
        helper.insertJogging(j13, null);
        helper.insertJogging(j14, null);
        helper.insertJogging(j21, null);
        helper.insertJogging(p111, null);
        helper.insertJogging(p112, null);
        helper.insertJogging(p113, null);
        helper.insertJogging(p114, null);
        helper.insertJogging(p211, null);
    }

}
