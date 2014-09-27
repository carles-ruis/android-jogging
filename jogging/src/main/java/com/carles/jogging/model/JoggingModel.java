package com.carles.jogging.model;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.carles.jogging.jogging.FootingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by carles1 on 20/04/14.
 */
public class JoggingModel implements Parcelable {

    private long id;
    // if this object represents a "partial" run, id of the "full" run this object is part of
    private long parentId;

    private Location start;
    private Location end;
    private UserModel user;
    private FootingResult footingResult;

    private long realTime;
    private long goalTime;
    private float realDistance;
    private float goalDistance;

    // if this objecte represents a "full" run, partial results obtained
    private List<JoggingModel> partials = new ArrayList<JoggingModel>();

    // partials results for each kilometer. Showing and saving full partial list was too much
    private List<JoggingModel> partialsForKilometer = new ArrayList<JoggingModel>();

    public JoggingModel() {}

    public JoggingModel(Location start, Location end, long goalTime, float goalDistance, UserModel user) {
        this.id = System.currentTimeMillis();
        this.start = start;
        this.end = end;
        this.user = user;
        this.realTime = end.getTime() - start.getTime();
        this.goalTime = goalTime;
        this.realDistance = end.distanceTo(start);
        this.goalDistance = goalDistance;
    }

    public JoggingModel(Parcel source) {
        this.id = source.readLong();
        this.parentId = source.readLong();
        this.start = source.readParcelable(Location.class.getClassLoader());
        this.end = source.readParcelable(Location.class.getClassLoader());
        this.realTime = source.readLong();
        this.goalTime = source.readLong();
        this.realDistance = source.readFloat();
        this.goalDistance = source.readFloat();
        this.user = source.readParcelable(UserModel.class.getClassLoader());
        this.partials = Arrays.asList(source.createTypedArray(JoggingModel.CREATOR));
        this.partialsForKilometer = Arrays.asList(source.createTypedArray(JoggingModel.CREATOR));
    }

    /**
     * Creates a JoggingModel from the info of each partial JoggingModel.
     * The partials list should not be empty, check before constructing this object.
     * Truncates goalDistance to the distance expected by the user if footing was successful
     * Calculates goalTime relative to goalDistance if footing was successful
     * @param partials
     */
    public JoggingModel(List<JoggingModel> partials, float goalDistance, FootingResult footingResult, UserModel user) {
        this.id = System.currentTimeMillis() + 1; // add one to difference it from last partial id
        this.start = partials.get(0).getStart();
        this.end = partials.get(partials.size() - 1).getEnd();
        this.user = user;
        this.realTime = getEnd().getTime() - getStart().getTime();
        this.realDistance = partials.get(partials.size() - 1).getGoalDistance();
        this.footingResult = footingResult;

        // Recalculate total distance and time if footing was successful
        if (footingResult == FootingResult.SUCCESS) {
            this.goalDistance = goalDistance;

            // extrapolate the two last locations obtained to set goalTime as accurate as possible
            float nextToLastDistance = 0f;
            long nextToLastTime = 0l;
            if (partials.size() > 1) {
                nextToLastDistance = partials.get(partials.size() - 2).getGoalDistance();
                nextToLastTime = partials.get(partials.size() - 2).getGoalTime();
            }
            float lastDistancesDifference = realDistance - nextToLastDistance;
            long lastTimesDifference = realTime - nextToLastTime;
            float goalDistanceDifference = goalDistance - nextToLastDistance;
            long goalTimeDifference = (long)(goalDistanceDifference * (float) lastTimesDifference / lastDistancesDifference);
            this.goalTime = + nextToLastTime + goalTimeDifference;

        } else {
            this.goalDistance = this.realDistance;
            this.goalTime = this.realTime;
        }

        // link this JoggingModel with its partials
        this.partials = partials;
        for (int i = 0; i < partials.size(); i++) {
            partials.get(i).setParentId(getId());
        }

        this.partialsForKilometer = calcPartialsForKilometer();
    }

    /**
     * Gets this jogging' list of kilometer partials
     * @return
     */
    public List<JoggingModel> calcPartialsForKilometer() {
        List<JoggingModel> ret = new ArrayList<JoggingModel>();
        int previous = 0;
        int current = 0;

        for (JoggingModel partial : getPartials()) {
            current = (int) partial.getGoalDistance();
            if (current / 1000 > previous / 1000) {
                ret.add(partial);
            }
            previous = current;
        }
        return ret;
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

    public float getGoalDistance() {
        return goalDistance;
    }

    public void setGoalDistance(float goalDistance) {
        this.goalDistance = goalDistance;
    }

    public float getAccuracy() {
        return end.getAccuracy();
    }

    public long getGoalTime() {
        return goalTime;
    }

    public void setGoalTime(long goalTime) {
        this.goalTime = goalTime;
    }

    public FootingResult getFootingResult() {
        return footingResult;
    }

    public void setFootingResult(FootingResult footingResult) {
        this.footingResult = footingResult;
    }

    public List<JoggingModel> getPartialsForKilometer() {
        return partialsForKilometer;
    }

    public void setPartialsForKilometer(List<JoggingModel> partialsForKilometer) {
        this.partialsForKilometer = partialsForKilometer;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(id);
        parcel.writeLong(parentId);
        parcel.writeParcelable(start, flags);
        parcel.writeParcelable(end, flags);
        parcel.writeLong(realTime);
        parcel.writeLong(goalTime);
        parcel.writeFloat(realDistance);
        parcel.writeFloat(goalDistance);
        parcel.writeParcelable(user, flags);
        parcel.writeTypedArray(partials.toArray(new JoggingModel[partials.size()]), flags);
        parcel.writeTypedArray(partialsForKilometer.toArray
                (new JoggingModel[partialsForKilometer.size()]), flags);
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