package com.picspy.models;

/**
 * Created by BrunelAmC on 8/5/2015.
 */
public class Friend {
    private int _id;
    private String _username;

    public Friend(){

    }
    public Friend(int id, String username) {
        _id = id;
        _username = username;
    }
    public int getId() {
        return _id;
    }

    public void setId(int _id) {
        this._id = _id;
    }

    public String getUsername() {
        return _username;
    }

    public void setUsername(String _username) {
        this._username = _username;
    }
}
