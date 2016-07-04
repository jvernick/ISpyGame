package com.picspy.models;


import java.util.ArrayList;

/**
 * Class to model <b>multiple</b> game records from get request
 * Created by BrunelAmC on 8/21/2015.
 */
public class GamesRecord extends RecordsResponse<GameRecord> {
    private ArrayList<GameRecord> resource = new ArrayList<>();

    public ArrayList<GameRecord> getResource() {
        return resource;
    }

    @Override
    public String toString() {
        return "GamesRecord {" +
                "\n" + "  resource: " + getResource() +
                "\n" + "  meta: " + getMeta() +
                "\n}";
    }
}
