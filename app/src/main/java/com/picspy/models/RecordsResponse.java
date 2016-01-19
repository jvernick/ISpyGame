package com.picspy.models;

import java.util.ArrayList;

/**
 * Created by BrunelAmC on 1/15/2016.
 */
public abstract class RecordsResponse<T> extends  DbApiResponse{
    private Metadata meta;

    public RecordsResponse() {
    }

    abstract ArrayList<T> getResource();

    public Metadata getMeta() {
        return meta;
    }

    public int getCount() {
        return (getResource() == null)? 0 : getResource().size();
    }

    @Override
    public String toString() {
        return "RecordsResponse{" +
                "\n" + "resource=" + getResource() +
                "\n" + ", meta=" + meta +
                "\n}";
    }

    public T getOnlyResource() {
        if (getCount() == 1) return getResource().get(0);
        return null;
    }
}

