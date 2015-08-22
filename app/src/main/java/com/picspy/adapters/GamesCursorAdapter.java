package com.picspy.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.picspy.firstapp.R;
import com.picspy.utils.DbContract;

/**
 * Created by BrunelAmC on 8/21/2015.
 */
public class GamesCursorAdapter extends ResourceCursorAdapter {
    public GamesCursorAdapter(Context context, int layout, Cursor cursor, int flags) {
        super(context,layout, cursor, flags);
    }
    /*
    Game game = new Game();
                game.setPictureName(c.getString(c.getColumnIndex(GameEntry.COLUMN_NAME_PICTURE)));
                game.setSelection((c.getString(c.getColumnIndex(GameEntry.COLUMN_NAME_SEL))));
                game.setHint((c.getString(c.getColumnIndex(GameEntry.COLUMN_NAME_HINT))));
                game.setGuess((c.getInt(c.getColumnIndex(GameEntry.COLUMN_NAME_GUESS))));
                game.setTime((c.getInt(c.getColumnIndex(GameEntry.COLUMN_NAME_TIME))));
                game.setVote((c.getInt(c.getColumnIndex(GameEntry.COLUMN_NAME_VOTE))) != 0);
                game.setSender((c.getInt(c.getColumnIndex(GameEntry.COLUMN_NAME_SENDER))));
                game.setId(c.getInt(c.getColumnIndex(GameEntry._ID)));
     */
    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor c) {
        // Find fields to populate in inflated template
        TextView username = (TextView) view.findViewById(R.id.username);
        TextView time_left = (TextView) view.findViewById(R.id.time_left);
        TextView challenge_time = (TextView) view.findViewById(R.id.challenge_time);
        TextView guesses = (TextView) view.findViewById(R.id.guesses);

        challenge_time.setText(String.valueOf( c.getInt(
                c.getColumnIndex(DbContract.GameEntry.COLUMN_NAME_TIME))));
        guesses.setText(String.valueOf(c.getInt(
                c.getColumnIndex(DbContract.GameEntry.COLUMN_NAME_GUESS))));
    }
}
