package com.picspy.adapters;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.picspy.firstapp.R;
import com.picspy.models.Game;

/**
 * Custom adapter for populating leaderboard challenges
 * Created by BrunelAmC on 8/23/2015.
 */
public class CustomArrayAdapter extends ArrayAdapter<Game> {

    private ArrayList<Game> games;

    public CustomArrayAdapter (Context context, int resource, ArrayList<Game> objects) {
        super(context, resource, objects);
        this.games = objects;

    }

    /**
     * Add multiple games to the adapter
     * @param gameList list of games to be added
     */
    public void addGames(ArrayList<Game> gameList) {
        games.addAll(gameList);
    }

    /** replaces the games with the new game list
     *
     * @param gameList new game list
     */
    public void replaceGames(ArrayList<Game> gameList) {
        games = gameList;
    }

    /**
     * Populate new items in the list.
     */
    @Override public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView == null) {
            LayoutInflater inflater =
                    (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_challenge, parent, false);
        } else {
            view = convertView;
        }
        TextView username = (TextView) view.findViewById(R.id.username);
        TextView timeLength = (TextView) view.findViewById(R.id.timeLength);
        TextView challenge_time = (TextView) view.findViewById(R.id.challengeTime);
        TextView guesses = (TextView) view.findViewById(R.id.guesses);

        Game game = games.get(position);
        username.setText(game.getSenderUsername());
        challenge_time.setText(String.valueOf(game.getTime()));
        String created = game.getCreated();
        timeLength.setText(GamesCursorAdapter.processTime(created));
        guesses.setText(String.valueOf(game.getGuess()));

        int senderId = game.getSenderId();

        GamesCursorAdapter.setIcon((ImageView) view.findViewById(R.id.list_icon), senderId);
        /*
        ImageView icon = (ImageView) view.findViewById(R.id.list_icon);
        if (!Arrays.asList(GamesCursorAdapter.ICONS).contains(icon.getDrawable())) {
            GamesCursorAdapter.setIcon((ImageView) view.findViewById(R.id.list_icon));
        }*/

        return view;
    }
}
