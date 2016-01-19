package com.picspy.models;

import java.util.ArrayList;

/**
 * Created by BrunelAmC on 1/15/2016.
 */
public class RecordsRequest<T> extends DbApiRequest {
    private ArrayList<T> resource;
    private ArrayList<Integer> ids;
    private String filter;
    private ArrayList<String> params;

    public RecordsRequest() {
    }

    public void addResource(T resource) {
        if (this.resource == null) {
            this.resource = new ArrayList<>();
        }
        this.resource.add(resource);
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public void setParams(ArrayList<String> params) {
        this.params = params;
    }

    public void setIds(ArrayList<Integer> ids) {
        this.ids = ids;
    }

    @Override
    public String toString() {
        return "RecordsRequest{" +
                "resource=" + resource +
                ", ids=" + ids +
                ", filter='" + filter + '\'' +
                ", params=" + params +
                '}';
    }
}
