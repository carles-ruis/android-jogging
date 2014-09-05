package com.carles.jogging.model;

import com.carles.jogging.model.JoggingModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by carles1 on 20/04/14.
 */
public class UserModel {

    private String nickname;
    private String email;
    private List<JoggingModel> joggings = new ArrayList<JoggingModel>();

    public UserModel() {
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<JoggingModel> getJoggings() {
        return joggings;
    }

    public void setJoggings(List<JoggingModel> joggings) {
        this.joggings = joggings;
    }
}
