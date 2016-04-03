package com.picspy.models;

import android.content.Context;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model for retrieving challenges from the server
 * TODO remove id field and unused methods
 * Created by BrunelAmC on 8/21/2015.
 */
public class UserChallengeRecord {
    private Integer user_id;
    private Integer challenge_id;
    private Integer id;
    //Related record to retrieve challenge
    private GameRecord challenges_by_challenge_id;
    //Related record to retrieve sender
    private UserRecord users_by_user_id;

    public UserChallengeRecord(Integer user_id) {
        this.user_id = user_id;
    }

    public UserRecord getUsers_by_user_id() {
        return users_by_user_id;
    }

    public void setUsers_by_user_id(UserRecord users_by_user_id) {
        this.users_by_user_id = users_by_user_id;
    }

    public GameRecord getChallenges_by_challenge_id() {
        return challenges_by_challenge_id;
    }

    public void setChallenges_by_challenge_id(GameRecord challenges_by_challenge_id) {
        this.challenges_by_challenge_id = challenges_by_challenge_id;
    }

    /**
     * returns a game object constructed from the Game record
     * @return Constructed game object. Returns null is there is
     * no related data(challenges_by_challenge_id)
     */
    public Game getGame(Context applicationContext) {
        Game temp = GameRecord.getGame(challenges_by_challenge_id, applicationContext);
        //TODO deprecate the line below. username already set in above function
        //temp.setSenderUsername(users_by_user_id.getUsername());
        return temp;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getChallenge_id() {
        return challenge_id;
    }

    public void setChallenge_id(int challenge_id) {
        this.challenge_id = challenge_id;
    }

    @Override
    public String toString() {
        return "UserChallengeRequest {" +
                "\n" + " challenge_id: " + challenge_id +
                "\n" + " user_id: " + user_id +
                "\n" + " GameRecord: {" + challenges_by_challenge_id +
                "\n" + " UserRecord: {" + users_by_user_id +
                "\n} } }";
    }
}


