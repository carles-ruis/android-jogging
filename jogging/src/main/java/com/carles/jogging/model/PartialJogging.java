package com.carles.jogging.model;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.carles.jogging.helper.LocationHelper;
import com.carles.jogging.util.FormatUtil;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Created by carles1 on 20/04/14.
 */
public class PartialJogging implements Parcelable {

    public static final Parcelable.Creator<PartialJogging> CREATOR = new Creator<PartialJogging>() {
        public PartialJogging createFromParcel(Parcel source) {
            return new PartialJogging(source);
        }

        public PartialJogging[] newArray(int size) {
            return new PartialJogging[size];
        }
    };
    private Location location;
    private float meters;
    private long time;
    private long timestamp;

    public PartialJogging() {
    }

    public PartialJogging(Location location, float meters, long time, long timestamp) {
        this.location = location;
        this.meters = meters;
        this.time = time;
        this.timestamp = timestamp;
    }

    private PartialJogging(Parcel in) {
        location = in.readParcelable(Location.class.getClassLoader());
        meters = in.readFloat();
        time = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(location, 0);
        parcel.writeFloat(meters);
        parcel.writeLong(time);
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public float getMeters() {
        return meters;
    }

    public void setMeters(float meters) {
        this.meters = meters;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        String sLocation = LocationHelper.toString(location);
         String sTime = LocationHelper.formatRunningTime(time);
        String sTimeTwo = FormatUtil.time(time);
         StringBuilder sb = new StringBuilder().append("PARTIAL JOGGING -> ").append(sLocation).append(" -> meters=").append(meters).append(" ---- time=").append(sTime);
        return sb.toString();
    }
}
