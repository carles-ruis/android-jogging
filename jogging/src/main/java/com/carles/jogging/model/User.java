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
}
