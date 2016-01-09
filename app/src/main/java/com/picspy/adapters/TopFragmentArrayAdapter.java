package com.picspy.adapters;


import java.util.ArrayList;

import android.content.Context;
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
public class TopFragmentArrayAdapter extends ArrayAdapter<Game> {
    private final Context context;
    private ArrayList<Game> games;

    public TopFragmentArrayAdapter(Context context, int resource, ArrayList<Game> games) {
        super(context, resource, games);
        this.games = games;
        this.context = context;
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
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(this.context);
            convertView = inflater.inflate(R.layout.item_challenge, parent, false);
            viewHolder = new ViewHolder();

            viewHolder.username = (TextView) convertView.findViewById(R.id.sender_username);
            viewHolder.timeLength = (TextView) convertView.findViewById(R.id.timeLength);
            viewHolder.challenge_time = (TextView) convertView.findViewById(R.id.challengeTime);
            viewHolder.guesses = (TextView) convertView.findViewById(R.id.guesses_title);
            viewHolder.list_icon = (ImageView) convertView.findViewById(R.id.list_icon);
            
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Game game = games.get(position);
        viewHolder.username.setText(game.getSenderUsername());
        viewHolder.challenge_time.setText(String.valueOf(game.getTime()));
        String created = game.getCreated();
        viewHolder.timeLength.setText(GamesCursorAdapter.processTime(created));
        viewHolder.guesses.setText(String.valueOf(game.getGuess()));

        int senderId = game.getSenderId();
        GamesCursorAdapter.setIcon(viewHolder.list_icon, senderId);

        return convertView;
    }


    private class ViewHolder {
        public TextView username;
        public TextView timeLength;
        public TextView challenge_time;
        public TextView guesses;
        public ImageView list_icon;
    }
}
