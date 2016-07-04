package com.picspy.models;

import java.util.ArrayList;

/**
 * Created by BrunelAmC on 1/15/2016.
 */
public class Metadata {
    private ArrayList<String> schema;
    private Integer count;

    public Metadata() {
    }

    public Integer getCount() {
        return count;
    }

    public ArrayList<String> getSchema() {
        return schema;
    }

    @Override
    public String toString() {
        return "Metadata{" +
                "schema=" + schema +
                ", count=" + count +
                '}';
    }
}


