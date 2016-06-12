package com.picspy.adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.picspy.firstapp.R;
import com.picspy.models.Game;

import java.util.ArrayList;

/**
 * Custom adapter for populating leaderboard challenges
 * Created by BrunelAmC on 8/23/2015.
 */
public class TopFragmentRecyclerAdapter extends RecyclerView.Adapter<TopFragmentRecyclerAdapter.ViewHolder> {
    private ArrayList<Game> mGames;
    private AdapterRequestListener adapterRequestListener;

    public TopFragmentRecyclerAdapter(ArrayList<Game> games) {
        this.mGames = games;
    }

    public void setAdapterRequestListener(AdapterRequestListener adapterRequestListener) {
        this.adapterRequestListener = adapterRequestListener;
    }
    /**
     * Add multiple mGames to the adapter
     * @param gameList list of mGames to be added
     */
    public void addGames(ArrayList<Game> gameList) {
        mGames.addAll(gameList);
    }

    /** replaces  mGames with the new game list
     *
     * @param data new game list
     */
    public void setData(ArrayList<Game> data) {
        mGames = data;
        notifyDataSetChanged();
    }

    /**
    * Returns the total number of items in the data set hold by the adapter.
    *
    * @return The total number of items in this adapter.
    */
    @Override
    public int getItemCount() {
        return mGames.size();
    }

    public Game getItem(int position) {
        return mGames.get(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_challenge, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Game game = mGames.get(position);
        viewHolder.username.setText(game.getSenderUsername());
        viewHolder.challenge_time.setText(String.valueOf(game.getTime()));
        String created = game.getCreated();
        viewHolder.timeLength.setText(GamesCursorAdapter.processTime(created));
        viewHolder.guesses.setText(String.valueOf(game.getGuess()));

        viewHolder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapterRequestListener.onListItemClick(position);
            }
        });

        viewHolder.rootview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapterRequestListener.onListItemClick(position);
            }
        });

        int senderId = game.getSenderId();
        GamesCursorAdapter.setIcon(viewHolder.list_icon, senderId);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView username;
        private TextView timeLength;
        private TextView challenge_time;
        private TextView guesses;
        private ImageView list_icon;
        private View rootview;

        public ViewHolder(View itemView) {
            super(itemView);
            username = (TextView) itemView.findViewById(R.id.sender_username);
            timeLength = (TextView) itemView.findViewById(R.id.timeLength);
            challenge_time = (TextView) itemView.findViewById(R.id.challengeTime);
            guesses = (TextView) itemView.findViewById(R.id.guesses_title);
            list_icon = (ImageView) itemView.findViewById(R.id.list_icon);
            rootview = (View) itemView.findViewById(R.id.challenge_root_view);
        }
    }

    public interface AdapterRequestListener {
        void onListItemClick(int position);
    }
}
