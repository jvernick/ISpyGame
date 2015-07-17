package com.picspy.models;

/**
 * Created by BrunelAmC on 7/14/2015.
 */

/**
 * Subtypes of this class are models for database records.
 * All the fields must have JSONproperty tags with the same name as the database field
 * See FriendRecord class for subclass example
 * toString output should be consistent with example
 */
public abstract class DbApiResponse {
    public abstract String toString();
}
