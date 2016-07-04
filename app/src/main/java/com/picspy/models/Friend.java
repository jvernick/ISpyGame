package com.picspy.models;

/**
 * Model for accessing friend records from the local sqlite database.
 * Created by BrunelAmC on 8/5/2015.
 */
public class Friend {
    private int id;
    private String _username;
    private int recordId;

    public Friend(int id, String _username, int recordId) {
        this.id = id;
        this._username = _username;
        this.recordId = recordId;
    }

    public Friend() {
        super();
    }

    public Friend(int id, String username) {
        this.id = id;
        _username = username;
    }

    public int getId() {
        return id;
    }

    public void setId(int _id) {
        this.id = _id;
    }

    public String getUsername() {
        return _username;
    }

    public void setUsername(String _username) {
        this._username = _username;
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }
}
