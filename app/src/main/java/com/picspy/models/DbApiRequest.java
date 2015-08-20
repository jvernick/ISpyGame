package com.picspy.models;

/**
 * Subtypes of this class are models for database post records. They have fields that are consistent
 * with data required in the post fields to access the records.
 * All the fields must have JSONproperty tags with the same name as the database field
 * @see com.picspy.models.FriendRecord for subclass example
 * toString output should be consistent with example
 *
 * Created by BrunelAmC on 7/15/2015.
 */
public abstract class DbApiRequest {
    public abstract String toString();
}
