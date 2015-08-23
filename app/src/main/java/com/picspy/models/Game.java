package com.picspy.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Model for accessing game records from the local sqlite database and the server.
 * Created by BrunelAmC on 8/21/2015.
 */
public class Game implements Parcelable {
    private String pictureName;
    private String selection;
    private String hint;
    private int guess;
    private int time;
    private boolean vote;
    private int sender;
    private int id;
    private String created;


    /**
     * Constructor initializes all the challenge parameters. Hence no getter and setters.
     * The values for time, guess are set to the default values of 3 and 5 if out of bounds
     * @param id challenge id
     * @param pictureName The picture file name
     * @param selection A string that represents the correct area of the picture (solution)
     * @param hint A hint for solving the challenge
     * @param guess The number of guesses allowed (1 - 5)
     * @param time The time limit (5 - 30 secs)
     * @param vote whether or not the user can vote this challenge
     * @param sender the sender of the challenge
     */
    public Game(int id, String pictureName, String selection, String hint, int guess, int time,
                boolean vote, int sender) {
        this.pictureName = pictureName;
        this.selection = selection;
        this.hint = hint;
        this.guess = guess;
        this.time = time;
        this.vote = vote;
        this.sender = sender;
        this.id = id;
    }

    /**
     * Constructor from parcel, to be used in receiveing intent
     * @param p Parcel containing game data
     */
    public Game(Parcel p) {
        this.pictureName = p.readString();
        this.selection = p.readString();
        this.hint = p.readString();
        this.guess = p.readInt();
        this.time = p.readInt();
        this.vote = Boolean.parseBoolean(p.readString());
        this.sender = p.readInt();
        this.id = p.readInt();
        this.created = p.readString();
    }

    public Game() {
        super();
    }
    /**
     * Implemented field from parcable
     */
    public static final Creator<Game> CREATOR = new Creator<Game>() {
        @Override
        public Game createFromParcel(Parcel in) {
            return new Game(in);
        }

        @Override
        public Game[] newArray(int size) {
            return new Game[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPictureName() {
        return pictureName;
    }

    public void setPictureName(String picture_name) {
        this.pictureName = picture_name;
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

    public boolean isVote() {
        return vote;
    }

    public void setVote(boolean vote) {
        this.vote = vote;
    }

    public int getSender() {
        return sender;
    }

    public void setSender(int sender) {
        this.sender = sender;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(pictureName);
        parcel.writeString(selection);
        parcel.writeString(hint);
        parcel.writeInt(guess);
        parcel.writeInt(time);
        parcel.writeString(String.valueOf(vote));
        parcel.writeInt(id);
        parcel.writeInt(sender);
        parcel.writeString(created);
    }

    @Override
    public String toString() {
        return "Game{" +
                "pictureName='" + pictureName + '\'' +
                ", selection='" + selection + '\'' +
                ", hint='" + hint + '\'' +
                ", guess=" + guess +
                ", time=" + time +
                ", vote=" + vote +
                ", sender=" + sender +
                ", id=" + id +
                ", created='" + created + '\'' +
                '}';
    }
}
