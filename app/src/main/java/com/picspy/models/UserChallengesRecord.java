package com.picspy.models;


import java.util.ArrayList;

/**
 * Class to model retrieving <b>multiple</b> challenges from the server
 * Created by BrunelAmC on 8/21/2015.
 */
public class UserChallengesRecord extends RecordsResponse<UserChallengeRecord> {
    // List of Friend Records from GET call that returns multiple friend records
    private ArrayList<UserChallengeRecord> resource;

    public ArrayList<UserChallengeRecord> getResource() {
        return resource;
    }

    @Override
    public String toString() {
        return "UserChallengesRecord {" +
                "\n" + "  resource: " + resource +
                "\n" + "  meta: " + getMeta() +
                "\n}";
    }
}
