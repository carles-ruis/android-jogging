package com.carles.jogging.model;

import android.location.Location;

import java.util.Date;
import java.util.List;

/**
 * Created by carles1 on 20/04/14.
 */
public class Jogging {

    private Date date;
    private Integer meters;
    private long time;
    private Location start;
    private Location end;
    private List<PartialJogging> partials;
    private User user;

    public Jogging() {
    }
}
