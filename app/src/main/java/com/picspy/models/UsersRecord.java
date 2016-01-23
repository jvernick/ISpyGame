package com.picspy.models;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by BrunelAmC on 12/30/2015.
 */
public class UsersRecord extends RecordsResponse<UserRecord> {
    // List of Friend Records from GET call that returns multiple friend records
    private ArrayList<UserRecord> resource = new ArrayList<>();

    public ArrayList<UserRecord> getResource() {
        return resource;
    }

    public void setResource(ArrayList<UserRecord> resource) {
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
