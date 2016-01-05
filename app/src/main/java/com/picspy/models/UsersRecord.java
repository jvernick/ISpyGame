package com.picspy.models;

import com.dreamfactory.model.Metadata;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BrunelAmC on 12/30/2015.
 */
public class UsersRecord extends DbApiResponse{
    // List of Friend Records from GET call that returns multiple friend records
    @JsonProperty("record")
    private List<UserRecord> record = new ArrayList<>();
    /* Available metadata for the response. */
    @JsonProperty("meta")
    private Metadata meta = null;

    public Metadata getMeta() {
        return meta;
    }

    public void setMeta(Metadata meta) {
        this.meta = meta;
    }

    public List<UserRecord> getRecord() {
        return record;
    }

    public void setRecord(List<UserRecord> record) {
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
