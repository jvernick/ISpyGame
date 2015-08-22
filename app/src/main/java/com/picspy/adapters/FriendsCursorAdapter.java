package com.picspy.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.picspy.firstapp.R;
import com.picspy.utils.DbContract;

/**
 * Created by Gordon on 8/22/2015.
 */
public class FriendsCursorAdapter extends ResourceCursorAdapter {

    public FriendsCursorAdapter(Context context, int layout, Cursor cursor, int flags) {
        super(context,layout, cursor, flags);
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor c) {
        // Find fields to populate in inflated template
        TextView username = (TextView) view.findViewById(R.id.username);
//        TextView time_left = (TextView) view.findViewById(R.id.time_left);
//        TextView challenge_time = (TextView) view.findViewById(R.id.challenge_time);
//        TextView guesses = (TextView) view.findViewById(R.id.guesses);
//
//        challenge_time.setText(String.valueOf( c.getInt(
//                c.getColumnIndex(DbContract.GameEntry.COLUMN_NAME_TIME))));
//        guesses.setText(String.valueOf(c.getInt(
//                c.getColumnIndex(DbContract.GameEntry.COLUMN_NAME_GUESS))));
    }

}
