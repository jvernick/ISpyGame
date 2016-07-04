package com.picspy.models;

/**
 * Model for retrieving data from the friends database. Contains fields for related data
 * and for choosing the related data.
 * TODO remove unused getters and setter
 * Created by BrunelAmC on 7/14/2015.
 */
public class FriendRecord extends DbApiResponse {

    //friend with lower friend_id
    private int friend_1;

    //friend with higher friend_id
    private int friend_2;


    //TODO Date_type in db. type could be int. verify
    //date when record was last updated(does not include create date)
    private String updated;

    /*
        Status of friend request.
        0 => friend request has been accepted
        int => id of user who sent the request.
        -if it is current user, it means they have a pending request
        -if it is not current user id, it means they sent the request
     */
    private int status;

    // The number of games FROM FRIEND_2 that friend_1 has won
    private int friend_1_won;

    //The number of games FROM FRIEND_2 that friend_1 has lost
    private int friend_1_lost;

    //The number of games FROM FRIEND_1 that friend_2 has won
    private int friend_2_won;

    //The number of games FROM FRIEND_1 that friend_2 has lost
    private int friend_2_lost;

    //related record for friend_1. Not added to toString()
    private UserRecord users_by_friend_1;

    //related record for friend_2. Not added to toString()
    private UserRecord users_by_friend_2;

    //record id
    private int id;

    /**
     * Return the other user's record
     *
     * @param myId current userID
     * @return other user id
     */
    public UserRecord getOtherUserRecord(int myId) {
        if (myId != this.friend_1) {
            return users_by_friend_1;
        } else {
            return users_by_friend_2;
        }
    }

    public Friend getFriend(int myId) {
        UserRecord record = getOtherUserRecord(myId);
        if (record != null) {
            Friend friend = new Friend(record.getId(), record.getUsername());
            friend.setRecordId(id);
            return friend;
        }
        return null;
    }

    public UserRecord getUsers_by_friend_1() {
        return users_by_friend_1;
    }

    public void setUsers_by_friend_1(UserRecord users_by_friend_1) {
        this.users_by_friend_1 = users_by_friend_1;
    }

    public UserRecord getUsers_by_friend_2() {
        return users_by_friend_2;
    }

    public void setUsers_by_friend_2(UserRecord users_by_friend_2) {
        this.users_by_friend_2 = users_by_friend_2;
    }

    public int getFriend_1() {
        return friend_1;
    }

    public int getFriend_2() {
        return friend_2;
    }

    public String getUpdated() {
        return updated;
    }

    public int getStatus() {
        return status;
    }

    public int getFriend_1_won() {
        return friend_1_won;
    }

    public void setFriend_1_won(int friend_1_won) {
        this.friend_1_won = friend_1_won;
    }

    public int getFriend_1_lost() {
        return friend_1_lost;
    }

    public int getFriend_2_won() {
        return friend_2_won;
    }

    public int getFriend_2_lost() {
        return friend_2_lost;
    }

    @Override
    public String toString() {
        return "FriendRecord {" +
                "\n" + "friend_1: " + friend_1 +
                "\n" + " friend_2: " + friend_2 +
                "\n" + " updated: " + updated +
                "\n" + " status: " + status +
                "\n" + " friend_1_won: " + friend_1_won +
                "\n" + " friend_1_lost: " + friend_1_lost +
                "\n" + " friend_2_won: " + friend_2_won +
                "\n" + " friend_2_lost: " + friend_2_lost +
                "\n" + " related {" +
                "\n" + " users_by_friend_1: " + users_by_friend_1 +
                "\n" + " users_by_friend_2: " + users_by_friend_2 +
                "\n}";
    }
}
