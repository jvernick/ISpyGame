package com.picspy.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class to model a user record from the users database. This is used as a model when
 * returning related related records.
 * Created by BrunelAmC on 8/7/2015.
 */
public class UserRecord extends DbApiResponse {
    //user_id
    private int id;
    //dispaly_name
    private String username;
    //total games won
    private int total_won;
    //total games lost
    private int total_lost;
    //total games featured on leaderboard
    private int leaderboard;

    public Friend getRecordToFriend() {
        return new Friend(id, username);
    }
    public int getTotal_won() {
        return total_won;
    }

    public void setTotal_won(int total_won) {
        this.total_won = total_won;
    }

    public int getTotal_lost() {
        return total_lost;
    }

    public void setTotal_lost(int total_lost) {
        this.total_lost = total_lost;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getLeaderboard() {
        return leaderboard;
    }

    public void setLeaderboard(int leaderboard) {
        this.leaderboard = leaderboard;
    }


    @Override
    public String toString() {
        return "UserRecord {" +
                "\n" + "id: "  + id +
                "\n" + " username: "  + username +
                "\n" + " won: " + total_won  +
                "\n" + " lost: "  + total_lost +
                "\n" + " leaderboard: "  + leaderboard +
                "\n}";
    }
}
