package com.carles.jogging.model;

import android.location.Location;

import java.util.Date;
import java.util.List;

/**
 * Created by carles1 on 20/04/14.
 */
public class Jogging {

    private long timestamp;
    private Integer meters;
    private long time;
    private Location start;
    private Location end;
    private List<PartialJogging> partials;
    private User user;

    public Jogging() {
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getMeters() {
        return meters;
    }

    public void setMeters(Integer meters) {
        this.meters = meters;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Location getStart() {
        return start;
    }

    public void setStart(Location start) {
        this.start = start;
    }

    public Location getEnd() {
        return end;
    }

    public void setEnd(Location end) {
        this.end = end;
    }

    public List<PartialJogging> getPartials() {
        return partials;
    }

    public void setPartials(List<PartialJogging> partials) {
        this.partials = partials;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
