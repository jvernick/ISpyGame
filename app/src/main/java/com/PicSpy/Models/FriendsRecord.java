package com.picspy.models;

import com.dreamfactory.model.Metadata;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Model for data in the friends table. Used as model for returning <b> multible </b>friends from
 * the friends database
 * Created by BrunelAmC on 7/14/2015.
 */
public class FriendsRecord extends DbApiResponse {

    // List of Friend Records from GET call that returns multiple friend records
    @JsonProperty("record")
    private List<FriendRecord> record = new ArrayList<>();
    /* Available metadata for the response. */
    @JsonProperty("meta")
    private Metadata meta = null;


    public Metadata getMeta() {
        return meta;
    }

    public void setMeta(Metadata meta) {
        this.meta = meta;
    }

    public List<FriendRecord> getRecord() {
        return record;
    }

    public void setRecord(List<FriendRecord> record) {
        this.record = record;
    }


    @Override
    public String toString() {
        return "FriendsRecord {" +
                "\n" + "  record: " + record +
                "\n" + "  meta: " + meta +
                "\n}";
    }
}
