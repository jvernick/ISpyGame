package com.picspy.models;

import com.dreamfactory.model.Metadata;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to model retrieving <b>multiple</b> challenges from the server
 * Created by BrunelAmC on 8/21/2015.
 */
public class UserChallengesRecord extends DbApiResponse {
    // List of Friend Records from GET call that returns multiple friend records
    @JsonProperty("record")
    private List<UserChallengeRecord> record = new ArrayList<>();
    /* Available metadata for the response. */
    @JsonProperty("meta")
    private Metadata meta = null;

    public Metadata getMeta() {
        return meta;
    }

    public void setMeta(Metadata meta) {
        this.meta = meta;
    }

    public List<UserChallengeRecord> getRecord() {
        return record;
    }

    public void setRecord(List<UserChallengeRecord> record) {
        this.record = record;
    }

    @Override
    public String toString() {
        return "UserChallengesRecord {" +
                "\n" + "  record: " + record +
                "\n" + "  meta: " + meta +
                "\n}";
    }
}
