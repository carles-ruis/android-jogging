package com.carles.jogging.model;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.carles.jogging.common.LocationHelper;

import java.util.List;

/**
 * Created by carles1 on 20/04/14.
 */
public class JoggingModel implements Parcelable {

    private long id;
    private Location start;
    private Location end;
    private UserModel user;

    // Partial running time in milliseconds
    private long time;
    private long totalTime;
    // Distances run in meters
    private float partialDistance;
    private float totalDistance;

    // if this objecte represents a "full" run, partial results obtained
    private List<JoggingModel> partials;
    // if this object represents a "partial" run, id of the "full" run this object is part of
    private long parentId;

    public JoggingModel() {}

    public JoggingModel(Location start, Location end, long totalTime, float totalDistance) {
        this.time = end.getTime() - start.getTime();
        this.totalTime = totalTime;
        this.start = start;
        this.end = end;
        this.partialDistance = end.distanceTo(start);
        this.totalDistance = totalDistance;
    }

    public JoggingModel(Parcel source) {
        this.time = source.readLong();
        this.totalTime = source.readLong();
        this.start = source.readParcelable(Location.class.getClassLoader());
        this.end = source.readParcelable(Location.class.getClassLoader());
        this.partialDistance = source.readFloat();
        this.totalDistance = source.readFloat();
    }

    @Override
    public String toString() {
        return "JoggingModel{" +
                "start=" + LocationHelper.toString(start) +
                ", end=" + LocationHelper.toString(end) +
                ", time=" + time +
                ", partialDistance=" + partialDistance +
                ", totalTime" + totalTime +
                ", totalDistance=" + totalDistance +
                '}';
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTimestamp() {
        return end.getTime();
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

    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }

    public List<JoggingModel> getPartials() {
        return partials;
    }

    public void setPartials(List<JoggingModel> partials) {
        this.partials = partials;
    }

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public float getPartialDistance() {
        return partialDistance;
    }

    public void setPartialDistance(float partialDistance) {
        this.partialDistance = partialDistance;
    }

    public float getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(float totalDistance) {
        this.totalDistance = totalDistance;
    }

    public float getAccuracy() {
        return end.getAccuracy();
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(time);
        parcel.writeLong(totalTime);
        parcel.writeParcelable(start, flags);
        parcel.writeParcelable(end, flags);
        parcel.writeFloat(partialDistance);
        parcel.writeFloat(totalDistance);
    }

    public static final Parcelable.Creator<JoggingModel> CREATOR = new Creator<JoggingModel>() {
        public JoggingModel createFromParcel(Parcel source) {
            return new JoggingModel(source);
        }
        public JoggingModel[] newArray(int size) {
            return new JoggingModel[size];
        }
    };
}
