package com.picspy.models;

import java.util.ArrayList;

/**
 * Model for data in the friends table. Used as model for returning <b> multible </b>friends from
 * the friends database
 * Created by BrunelAmC on 7/14/2015.
 */
public class FriendsRecord extends RecordsResponse<FriendRecord> {
    // List of Friend Records from GET call that returns multiple friend records
    private ArrayList<FriendRecord> resource = new ArrayList<>();

    public ArrayList<FriendRecord> getResource() {
        return resource;
    }

    public void setResource(ArrayList<FriendRecord> resource) {
        this.resource = resource;
    }

    @Override
    public String toString() {
        return "FriendsRecord {" +
                "\n" + "  resource: " + resource +
                "\n" + "  meta: " + getMeta() +
                "\n}";
    }
}
