package com.picspy.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model for receiving data from server
 * TODO remove unused getters and setter
 * Created by BrunelAmC on 8/21/2015.
 */
public class GameRecord {
    private String picture_name;
    private String selection;
    private String hint;
    private int guess;
    private int time;
    private boolean l_board;
    private int sender;
    private int id;
    private String created;

    public GameRecord() {}

    private UserRecord users_by_sender;

    public static Game getGame(GameRecord gameRecord) {
        if (gameRecord == null) {
            return null;
        } else {
            Game game = new Game();

            game.setId(gameRecord.getId());
            game.setPictureName(gameRecord.getPicture_name());
            game.setSelection(gameRecord.getSelection());
            game.setHint(gameRecord.getHint());
            game.setGuess(gameRecord.getGuess());
            game.setTime(gameRecord.getTime());
            game.setVote(gameRecord.isL_board());
            game.setSenderId(gameRecord.getSender());
            game.setCreated(gameRecord.getCreated());
            if (gameRecord.users_by_sender != null)
            game.setSenderUsername(gameRecord.getUsers_by_sender().getUsername());

            return game;
        }
    }

    public String getPicture_name() {
        return picture_name;
    }

    public void setPicture_name(String picture_name) {
        this.picture_name = picture_name;
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
                "\n" +  "picture_name: " + picture_name +
                "\n" + " selection: " + selection +
                "\n" + " hint: " + hint +
                "\n" + " guess: " + guess +
                "\n" + " time: " + time +
                "\n" + " l_board: " + l_board +
                "\n" + " sender: " + sender +
                "\n" + " id: " + id +
                "\n" + " created: " + created +
                "\n" + " users_by_sender: " + users_by_sender +
                "\n}";
    }

    public UserRecord getUsers_by_sender() {
        return users_by_sender;
    }
}
