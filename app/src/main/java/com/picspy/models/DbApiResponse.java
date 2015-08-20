package com.picspy.models;

/**
 * Subtypes of this class are models for database records. They have fields consistent with data
 * returned from the database.
 * All the fields must have JSONproperty tags with the same name as the database field
 * @see com.picspy.models.FriendRecord for subclass example
 * toString output should be consistent with example
 *
 * Created by BrunelAmC on 7/14/2015.
 */
public abstract class DbApiResponse {
    public abstract String toString();
}
