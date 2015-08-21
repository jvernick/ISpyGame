package com.picspy.models;

import com.dreamfactory.model.Metadata;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to model <b>multiple</b> game records from get request
 * Created by BrunelAmC on 8/21/2015.
 */
public class GamesRecord {
    // List of Game Records from GET call that returns multiple game records
    @JsonProperty("record")
    private List<GameRecord> record = new ArrayList<>();
    /* Available metadata for the response. */
    @JsonProperty("meta")
    private Metadata meta = null;

    public Metadata getMeta() {
        return meta;
    }

    public void setMeta(Metadata meta) {
        this.meta = meta;
    }

    public List<GameRecord> getRecord() {
        return record;
    }

    public void setRecord(List<GameRecord> record) {
        this.record = record;
    }


    @Override
    public String toString() {
        return "GamesRecord {" +
                "\n" + "  record: " + record +
                "\n" + "  meta: " + meta +
                "\n}";
    }
}
