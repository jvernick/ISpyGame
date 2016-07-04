package com.picspy.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides a model for data returned from a custom stored procedure on the database
 * Only for a custom stored procedure response
 * The model is ass follows
 * {
 * [
 * return_val = 0/1
 * ]
 * }
 *
 * @see com.dreamfactory.model.StoredProcResponse
 */
public class StoredProcResponse extends DbApiResponse {
    @JsonProperty("record")
    private List<ReturnVal> record = new ArrayList<>();

    public void setRecord(List<ReturnVal> record) {
        this.record = record;
    }

    public String getReturn_val() {
        return record.get(0).toString();
    }


    @Override
    public String toString() {
        return "return_val: " + record.get(0);
    }

    //inner class to model the returrn_val string
    private static class ReturnVal {
        @JsonProperty
        private String return_val;

        public void setReturn_val(String return_val) {
            this.return_val = return_val;
        }

        @Override
        public String toString() {
            return return_val;
        }
    }
}
