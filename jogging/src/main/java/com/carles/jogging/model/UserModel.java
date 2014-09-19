package com.carles.jogging.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by carles1 on 20/04/14.
 */
public class UserModel implements Parcelable {

    private String name;
    private String email;
    private String password;

    public UserModel() {}

    public UserModel(Parcel source) {
        this.name = source.readString();
        this.email = source.readString();
        this.password = source.readString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(name);
        parcel.writeString(email);
        parcel.writeString(password);
    }

    public static final Parcelable.Creator<UserModel> CREATOR = new Creator<UserModel>() {
        public UserModel createFromParcel(Parcel source) {
            return new UserModel(source);
        }
        public UserModel[] newArray(int size) {
            return new UserModel[size];
        }
    };
}
