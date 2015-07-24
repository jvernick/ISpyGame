package com.picspy.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**TODO Document
 * Only for my custom stored procedue response
 * Created by BrunelAmC on 7/23/2015.
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
