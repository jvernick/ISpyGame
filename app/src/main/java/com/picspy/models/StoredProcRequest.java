package com.picspy.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to model post data for the stored procedure call
 * Model is as follows:
 * {
 * params: [
 * key: value
 * ]
 * wrapper: value
 * }
 *
 * @see com.dreamfactory.model.StoredProcRequest
 */
public class StoredProcRequest extends DbApiRequest {
    /* Optional array of input and output parameters. */
    @JsonProperty("params")
    private List<StoredProcParam> params = new ArrayList<StoredProcParam>();
    @JsonProperty
    private String wrapper = "record";      //set by Json to object

    public void setParams(List<StoredProcParam> params) {
        this.params = params;
    }

    public void addParam(StoredProcParam param) {
        this.params.add(param);
    }

    @Override
    public String toString() {
        return "FriendsRecord {" +
                "\n" + "  record: " + params +
                "\n}";
    }
}

