package com.carles.jogging.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by carles1 on 20/04/14.
 */
public class User {

    private String nickname;
    private String email;
    private List<Jogging> joggings = new ArrayList<Jogging>();

    public User() {
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

    public List<Jogging> getJoggings() {
        return joggings;
    }

    public void setJoggings(List<Jogging> joggings) {
        this.joggings = joggings;
    }
}
