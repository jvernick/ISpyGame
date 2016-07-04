package com.picspy.models;

import android.content.Context;

import com.picspy.adapters.DatabaseHandler;

import java.util.ArrayList;

/**
 * Model for receiving data from server
 * TODO remove unused getters and setter
 * Created by BrunelAmC on 8/21/2015.
 */
public class GameRecord {
    private String picture_name;
    private String selection;
    private String hint;
    private Integer guess;
    private Integer time;
    private boolean leaderboard;
    private Integer sender;
    private Integer id;
    private String created;
    private ArrayList<UserChallengeRecord> user_challenges_by_challenge_id;
    private UserRecord users_by_sender;


    public GameRecord() {
    }


    public static Game getGame(GameRecord gameRecord, Context applicationContext) {
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
            game.setVote(gameRecord.isLeaderboard());
            game.setSenderId(gameRecord.getSender());
            game.setCreated(gameRecord.getCreated());
            if (gameRecord.users_by_sender != null) { //user not a friend == username in record
                game.setSenderUsername(gameRecord.getUsers_by_sender().getUsername());
            } else {  //user is a friend == username in local database
                DatabaseHandler dbHandler = DatabaseHandler.getInstance((applicationContext));
                Friend friend = dbHandler.getFriend(game.getSenderId());
                if (friend == null) {
                    game.setSenderUsername("New Challenge");
                } else {
                    game.setSenderUsername(dbHandler.getFriend(game.getSenderId()).getUsername());
                }
            }

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
        this.selection = selection;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public int getGuess() {
        return guess;
    }

    public void setGuess(int guess) {
        this.guess = guess;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public boolean isLeaderboard() {
        return leaderboard;
    }

    public void setLeaderboard(boolean leaderboard) {
        this.leaderboard = leaderboard;
    }

    public int getSender() {
        return sender;
    }

    public void setSender(int sender) {
        this.sender = sender;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public UserRecord getUsers_by_sender() {
        return users_by_sender;
    }


    public void setUser_challenges_by_challenge_id(ArrayList<UserChallengeRecord> user_challenges_by_challenge_id) {
        this.user_challenges_by_challenge_id = user_challenges_by_challenge_id;
    }

    @Override
    public String toString() {
        return "GameRecord {" +
                "\n" + "picture_name: " + picture_name +
                "\n" + " selection: " + selection +
                "\n" + " hint: " + hint +
                "\n" + " guess: " + guess +
                "\n" + " time: " + time +
                "\n" + " leaderboard: " + leaderboard +
                "\n" + " sender: " + sender +
                "\n" + " id: " + id +
                "\n" + " created: " + created +
                "\n" + " users_by_sender: " + users_by_sender +
                "\n" + " user_challenges_by_challenge_id: " + user_challenges_by_challenge_id +
                "\n}";
    }
}
