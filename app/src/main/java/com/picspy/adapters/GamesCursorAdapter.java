package com.picspy.adapters;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.picspy.firstapp.R;
import com.picspy.utils.DbContract;

import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**TODO Document
 * Implements a custom adapter for binding database entries to
 * list elements
 * Created by BrunelAmC on 8/21/2015.
 */
public class GamesCursorAdapter extends ResourceCursorAdapter {
    private static final String TAG = "GamesCursorAdapter";
    //possible challenge icons. Add as necessary.
    private static final int[] ICONS = {R.drawable.ic_challenge_lime, R.drawable.ic_challenge_red,
            R.drawable.ic_challenge_orange, R.drawable.ic_challenge_yellow,
            R.drawable.ic_challenge_purple,};

    //Default inherited constructor
    public GamesCursorAdapter(Context context, int layout, Cursor cursor, int flags) {
        super(context, layout, cursor, flags);
    }
    /* TODO remove
    Game game = new Game();
                game.setPictureName(c.getString(c.getColumnIndex(GameEntry.COLUMN_NAME_PICTURE)));
                game.setSelection((c.getString(c.getColumnIndex(GameEntry.COLUMN_NAME_SEL))));
                game.setHint((c.getString(c.getColumnIndex(GameEntry.COLUMN_NAME_HINT))));
                game.setGuess((c.getInt(c.getColumnIndex(GameEntry.COLUMN_NAME_GUESS))));
                game.setTime((c.getInt(c.getColumnIndex(GameEntry.COLUMN_NAME_TIME))));
                game.setVote((c.getInt(c.getColumnIndex(GameEntry.COLUMN_NAME_VOTE))) != 0);
                game.setSenderId((c.getInt(c.getColumnIndex(GameEntry.COLUMN_NAME_SENDER))));
                game.setId(c.getInt(c.getColumnIndex(GameEntry._ID)));
     */

    /**
     * Binds challenge data to elements of the view
     * such as setting the text on a TextView.
     * @param view The view to be populated
     * @param context The calling application context
     * @param c Cursor containing result of database query
     */
    @Override
    public void bindView(View view, Context context, Cursor c) {
        // Find fields to populate in inflated template
        TextView uname = (TextView) view.findViewById(R.id.username);
        TextView timeLength = (TextView) view.findViewById(R.id.timeLength);
        TextView challenge_time = (TextView) view.findViewById(R.id.challengeTime);
        TextView guesses = (TextView) view.findViewById(R.id.guesses);

        String created = c.getString(c.getColumnIndex(DbContract.GameEntry.COLUMN_NAME_CREATED));
        timeLength.setText(processTime(created));
        challenge_time.setText(String.valueOf(c.getInt(
                c.getColumnIndex(DbContract.GameEntry.COLUMN_NAME_TIME))));
        guesses.setText(String.valueOf(c.getInt(
                c.getColumnIndex(DbContract.GameEntry.COLUMN_NAME_GUESS))));
        uname.setText(c.getString(c.getColumnIndex(DbContract.FriendEntry.COLUMN_NAME_USERNAME)));

        setIcon((ImageView) view.findViewById(R.id.list_icon));
    }

    /**
     * Sets the view to a random color
     * @param view View to be set
     */
    public static void setIcon(ImageView view) {
        Random randomIndex = new Random();
        int index = randomIndex.nextInt(ICONS.length);
        view.setImageResource(ICONS[index]);
    }

    /**
     * Finds time difference and returns a string to display
     * @param created creation timestamp from server
     * @return String that represents time duration
     */
    public static String processTime(String created) {
        Long createdTime = java.sql.Timestamp.valueOf(created).getTime();
        Calendar rightnow = Calendar.getInstance();
        Long currentTime =rightnow.getTimeInMillis();

        Long duration = currentTime - createdTime;
        long diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(duration);
        long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        long diffInHours = TimeUnit.MILLISECONDS.toHours(duration);
        long diffInDays = TimeUnit.MILLISECONDS.toDays(duration);

        if (diffInDays > 30) {
            return "30+ days ago";
        } else if (diffInDays < 30 && diffInDays > 0) {
            return diffInDays + " day" + ((diffInDays == 1)? "":"s") + " ago";
        } else if (diffInHours > 0) {
            return diffInHours + " hour" + ((diffInHours == 1)? "":"s") + " ago";
        } else if (diffInMinutes > 0) {
            return diffInMinutes + " minute" + ((diffInMinutes == 1)? "":"s") + " ago";
        } else {
            //TODO secsonds/moments ago
            return "moments ago";
            //return diffInSeconds + " seconds ago";
        }
    }

    @Override
    protected void onContentChanged(){
        Log.d(TAG, "content changes");
        super.onContentChanged();
        notifyDataSetChanged();
    }
}
