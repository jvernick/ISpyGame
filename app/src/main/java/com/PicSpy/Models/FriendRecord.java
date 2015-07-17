package com.picspy.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by BrunelAmC on 7/14/2015.
 */
public class FriendRecord extends DbApiResponse {

    //friend with lower friend_id
    @JsonProperty("friend_1")
    private int friend_1;

    //friend with higher friend_id
    @JsonProperty("friend_2")
    private int friend_2;


    //TODO Date_type in db. type could be int. verify
    //date when record was last updated(does not include create date)
    @JsonProperty("updated")
    private String updated;

    /*
        Status of friend request.
        0 => friend request has been accepted
        int => id of user who sent the request.
        -if it is current user, it means they have a pending request
        -if it is not current user id, it means they sent the request
     */
    @JsonProperty("status")
    private int status;

    /*
        The number of games FROM FRIEND_2 that friend_1 has won
     */
    @JsonProperty("friend_1_won")
    private int friend_1_won;

    /*
        The number of games FROM FRIEND_2 that friend_1 has lost
     */
    @JsonProperty("friend_1_lost")
    private int friend_1_lost;

    /*
        The number of games FROM FRIEND_1 that friend_2 has won
     */
    @JsonProperty("friend_2_won")
    private int friend_2_won;

    /*
      The number of games FROM FRIEND_1 that friend_2 has lost
   */
    @JsonProperty("friend_2_lost")
    private int friend_2_lost;

    public int getFriend_1() {
        return friend_1;
    }

    public void setFriend_1(int friend_1) {
        this.friend_1 = friend_1;
    }

    public int getFriend_2() {
        return friend_2;
    }

    public void setFriend_2(int friend_2) {
        this.friend_2 = friend_2;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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

    public void setFriend_1_lost(int friend_1_lost) {
        this.friend_1_lost = friend_1_lost;
    }

    public int getFriend_2_won() {
        return friend_2_won;
    }

    public void setFriend_2_won(int friend_2_won) {
        this.friend_2_won = friend_2_won;
    }

    public int getFriend_2_lost() {
        return friend_2_lost;
    }

    public void setFriend_2_lost(int friend_2_lost) {
        this.friend_2_lost = friend_2_lost;
    }

    @Override

    public String toString() {
        return "FriendRecord {" +
                "\n" + "friend_1: "  + friend_1 +
                "\n" + " friend_2: "  + friend_2 +
                "\n" + " updated: " + updated + '\'' +
                "\n" + " status: "  + status +
                "\n" + " friend_1_won: "  + friend_1_won +
                "\n" + " friend_1_lost: "  + friend_1_lost +
                "\n" + " friend_2_won: "  + friend_2_won +
                "\n" + " friend_2_lost: "  + friend_2_lost +
                "\n}";
    }
}
