package com.picspy.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model for retrieving challenges from the server
 * TODO remove id field and unused methods
 * Created by BrunelAmC on 8/21/2015.
 */
public class UserChallengeRecord extends DbApiResponse{
    @JsonProperty
    private int user_id;
    @JsonProperty
    private int challenge_id;
    @JsonProperty
    private int id;
    //Related record to retrieve challenge
    @JsonProperty
    private GameRecord challenges_by_challenge_id;

    public GameRecord getChallenges_by_challenge_id() {
        return challenges_by_challenge_id;
    }

    public void setChallenges_by_challenge_id(GameRecord challenges_by_challenge_id) {
        this.challenges_by_challenge_id = challenges_by_challenge_id;
    }

    /**
     * Creates a game object from the obtained related data for storage in local db
     * @return Constructed game object. Returns null is there is no related data
     */
    public Game getGame() {
        if (challenges_by_challenge_id == null) {
            return null;
        } else {
            Game game = new Game();
            GameRecord gr = challenges_by_challenge_id;

            game.setId(gr.getId());
            game.setPictureName(gr.getPictureName());
            game.setSelection(gr.getSelection());
            game.setHint(gr.getHint());
            game.setGuess(gr.getGuess());
            game.setTime(gr.getTime());
            game.setVote(gr.isL_board());
            game.setSender(gr.getSender());
            game.setCreated(gr.getCreated());

            return game;
        }
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
        return "UserChallengesRequest {" +
                "\n" + " challenge_id: " + challenge_id +
                "\n" + " user_id: " + user_id +
                "\n" + " GameRecord: {" +
                "}\n}";
    }
}


