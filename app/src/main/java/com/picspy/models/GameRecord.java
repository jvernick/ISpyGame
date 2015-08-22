package com.picspy.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model for receiving data from server
 * TODO remove unused getters and setter
 * Created by BrunelAmC on 8/21/2015.
 */
public class GameRecord {
    // picture filename
    @JsonProperty
    private String pictureName;
    //challenge solution
    @JsonProperty
    private String selection;
    @JsonProperty
    private String hint;
    //number of guesses allowed
    @JsonProperty
    private int guess;
    @JsonProperty
    private int time;
    //true if challenge should be on leaderboard
    @JsonProperty
    private boolean l_board;
    //challenge creator/sender
    @JsonProperty 
    private int sender;
    //challenge id
    @JsonProperty
    private int id;
    @JsonProperty
    private String created;

    public String getPictureName() {
        return pictureName;
    }

    public void setPictureName(String pictureName) {
        this.pictureName=  pictureName;
    }

    public String getSelection() {
        return selection;
    }

    public void setSelection(String selection) {
        this.selection=  selection;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint=  hint;
    }

    public int getGuess() {
        return guess;
    }

    public void setGuess(int guess) {
        this.guess=  guess;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time=  time;
    }

    public boolean isL_board() {
        return l_board;
    }

    public void setL_board(boolean l_board) {
        this.l_board=  l_board;
    }

    public int getSender() {
        return sender;
    }

    public void setSender(int sender) {
        this.sender=  sender;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id=  id;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }
    
    @Override
    public String toString() {
        return "GameRecord {" +
                "\n" +  "pictureName: " + pictureName +
                "\n" + " selection: " + selection +
                "\n" + " hint: " + hint +
                "\n" + " guess: " + guess +
                "\n" + " time: " + time +
                "\n" + " l_board: " + l_board +
                "\n" + " sender: " + sender +
                "\n" + " id: " + id +
                "\n" + " created: " + created +
                "\n}";
    }
}
