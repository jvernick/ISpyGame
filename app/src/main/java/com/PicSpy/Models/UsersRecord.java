package com.picspy.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by BrunelAmC on 8/7/2015.
 */
public class UsersRecord extends DbApiResponse {
    //user_id
    @JsonProperty
    private int id;
    //dispaly_name
    @JsonProperty
    private String username;
    //total games won
    @JsonProperty
    private int won;
    //total games lost
    @JsonProperty
    private int lost;
    //total games featured on leaderboard
    @JsonProperty
    private int leaderboard;

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

    public int getWon() {
        return won;
    }

    public void setWon(int won) {
        this.won = won;
    }

    public int getLost() {
        return lost;
    }

    public void setLost(int lost) {
        this.lost = lost;
    }

    public int getLeaderboard() {
        return leaderboard;
    }

    public void setLeaderboard(int leaderboard) {
        this.leaderboard = leaderboard;
    }

    @Override
    public String toString() {
        return "FriendRecord {" +
                "\n" + "id: "  + id +
                "\n" + " username: "  + username +
                "\n" + " won: " + won + '\'' +
                "\n" + " lost: "  + lost +
                "\n" + " leaderboard: "  + leaderboard +
                "\n}";
    }
}
