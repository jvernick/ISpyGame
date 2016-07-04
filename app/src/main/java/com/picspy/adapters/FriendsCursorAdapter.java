package com.picspy.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.picspy.firstapp.R;
import com.picspy.models.UserRecord;
import com.picspy.utils.AppConstants;
import com.picspy.utils.DbContract;
import com.picspy.views.CameraActivity;
import com.picspy.views.FriendInfoActivity;
import com.picspy.views.SendChallenge;

/**
 * Created by Gordon on 8/22/2015.
 */
public class FriendsCursorAdapter extends ResourceCursorAdapter {


    public FriendsCursorAdapter(Context context, int layout, Cursor cursor, int flags) {
        super(context, layout, cursor, flags);
    }

    /**
     * Binds username and user_id to the list element
     *
     * @param view    The view to be populated
     * @param context The calling application context
     * @param c       Cursor containing result of database query
     */
    @Override
    public void bindView(View view, final Context context, Cursor c) {
        if (c != null) {
            // Find fields to populate in inflated template
            final ViewHolder viewHolder = (ViewHolder) view.getTag();
            final String friendUsername = c.getString(c.getColumnIndex(
                    DbContract.FriendEntry.COLUMN_NAME_USERNAME));
            final int userId = c.getInt(c.getColumnIndex(DbContract.FriendEntry._ID));

            viewHolder.unameTextView.setText(friendUsername);
            //Start friendInfoActivity when friend username clicked
            viewHolder.unameTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startFriendInfoActivity(view, friendUsername, userId, null);
                }
            });

            //start a new game with this friend
            viewHolder.newGameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, CameraActivity.class);
                    intent.putExtra(SendChallenge.ARG_FRIEND_ID, userId);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //TODO change intent extra to appropriate value if any
                    view.getContext().startActivity(intent);
                }
            });

            Drawable background = viewHolder.friendIcon.getBackground();
            ((GradientDrawable) background).setColor(
                    AppConstants.COLOR_ARRAY_LIST[userId % AppConstants.COLOR_ARRAY_LIST.length]);
            //Start friendInfoActivity when friend icon clicked
            viewHolder.friendIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startFriendInfoActivity(view, friendUsername, userId, null);
                }
            });
        }
    }

    //initialize viewHolder
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = super.newView(context, cursor, parent);
        ViewHolder viewHolder = new ViewHolder();

        viewHolder.unameTextView = (TextView) view.findViewById(R.id.friend_username);
        viewHolder.newGameButton = (ImageView) view.findViewById(R.id.game_start);
        viewHolder.friendIcon = (ImageView) view.findViewById(R.id.friend_icon);

        view.setTag(viewHolder);

        return view;
    }

    /**
     * Method to start the FriendInfoActivity with appropriate intent bundles.
     *
     * @param view  Context view
     * @param uname friend username
     * @param id    friend id
     */
    public static void startFriendInfoActivity(View view, String uname, Integer id, UserRecord userRecord) {
        Intent intent = new Intent(view.getContext(), FriendInfoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (userRecord == null) {
            intent.putExtra(FriendInfoActivity.FOR_FRIEND, true);
            intent.putExtra(FriendInfoActivity.USERNAME, uname);
            intent.putExtra(FriendInfoActivity.FRIEND_ID, id);
            view.getContext().startActivity(intent);
        } else {
            intent.putExtra(FriendInfoActivity.FOR_FRIEND, false);
            intent.putExtra(FriendInfoActivity.USERNAME, userRecord.getUsername());
            intent.putExtra(FriendInfoActivity.WON, userRecord.getTotal_won());
            intent.putExtra(FriendInfoActivity.LOST, userRecord.getTotal_lost());
            intent.putExtra(FriendInfoActivity.L_BOARD, userRecord.getLeaderboard());
            view.getContext().startActivity(intent);
        }
    }

    private class ViewHolder {
        public TextView unameTextView;//username
        public ImageView newGameButton; // new game TODO add this button
        public ImageView friendIcon;
    }
}
