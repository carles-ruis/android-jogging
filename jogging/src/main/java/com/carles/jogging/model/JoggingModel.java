package com.carles.jogging.model;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.carles.jogging.jogging.FootingResult;
import com.carles.jogging.util.LocationHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by carles1 on 20/04/14.
 */
public class JoggingModel implements Parcelable {

    private long id;
    private Location start;
    private Location end;
    private UserModel user;
    private FootingResult footingResult;

    private long realTime;
    private long totalTime;
    private float realDistance;
    private float totalDistance;

    // if this objecte represents a "full" run, partial results obtained
    private List<JoggingModel> partials = new ArrayList<JoggingModel>();
    // if this object represents a "partial" run, id of the "full" run this object is part of
    private long parentId;

    public JoggingModel() {}

    public JoggingModel(Location start, Location end, long totalTime, float totalDistance) {
        this.start = start;
        this.end = end;
        this.realTime = end.getTime() - start.getTime();
        this.totalTime = totalTime;
        this.realDistance = end.distanceTo(start);
        this.totalDistance = totalDistance;
    }

    public JoggingModel(Parcel source) {
        this.start = source.readParcelable(Location.class.getClassLoader());
        this.end = source.readParcelable(Location.class.getClassLoader());
        this.realTime = source.readLong();
        this.totalTime = source.readLong();
        this.realDistance = source.readFloat();
        this.totalDistance = source.readFloat();
    }

    /**
     * Creates a JoggingModel from the info of each partial JoggingModel.
     * The partials list should not be empty, check before constructing this object.
     * Truncates totalDistance to the distance expected by the user if footing was successful
     * Calculates totalTime relative to totalDistance if footing was successful
     * @param partials
     */
    public JoggingModel(List<JoggingModel> partials, float goalDistance, FootingResult footingResult, UserModel user) {
        this.id = System.currentTimeMillis();
        this.start = partials.get(0).getStart();
        this.end = partials.get(partials.size() - 1).getEnd();
        this.realTime = getEnd().getTime() - getStart().getTime();
        this.realDistance = partials.get(partials.size() - 1).getTotalDistance();
        this.footingResult = footingResult;
        this.user = user;

        // total distance and time. ReCalculate them if footing was successful
        if (footingResult == FootingResult.SUCCESS) {
            this.totalDistance = goalDistance;
            this.totalTime = (long) ((float) getRealTime() * getTotalDistance() / getRealDistance());
        } else {
            this.totalDistance = this.realDistance;
            this.totalTime = this.realTime;
        }

        // link this JoggingModel with its partials
        this.partials = partials;
        for (int i = 0; i < partials.size(); i++) {
            partials.get(i).setParentId(getId());
            partials.get(i).setId(Long.valueOf(String.format("%d%03d", getId(), i)));
            partials.get(i).setUser(this.user);
        }
    }

    @Override
    public String toString() {
        return "JoggingModel{" +
                "start=" + LocationHelper.toString(start) +
                ", end=" + LocationHelper.toString(end) +
                ", realTime=" + realTime +
                ", realDistance=" + realDistance +
                ", totalTime=" + totalTime +
                ", totalDistance=" + totalDistance +
                '}';
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRealTime() {
        return realTime;
    }

    public void setRealTime(long realTime) {
        this.realTime = realTime;
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

    public float getRealDistance() {
        return realDistance;
    }

    public void setRealDistance(float realDistance) {
        this.realDistance = realDistance;
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

    public FootingResult getFootingResult() {
        return footingResult;
    }

    public void setFootingResult(FootingResult footingResult) {
        this.footingResult = footingResult;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeParcelable(start, flags);
        parcel.writeParcelable(end, flags);
        parcel.writeLong(realTime);
        parcel.writeLong(totalTime);
        parcel.writeFloat(realDistance);
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
