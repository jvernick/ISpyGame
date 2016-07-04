package com.picspy.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.picspy.firstapp.R;
import com.picspy.utils.AppConstants;
import com.picspy.utils.DbContract;

import java.util.HashSet;

/**
 * Created by Gordon on 8/22/2015.
 */
public class ChooseFriendsCursorAdapter extends ResourceCursorAdapter {
    private final EmptyCheckedListener emptyCheckedListener;
    private HashSet<Integer> checkedFriends;
    private Context context;

    public ChooseFriendsCursorAdapter(Context context, int layout, Cursor cursor, int flags,
                                      EmptyCheckedListener emptyCheckedListener) {
        super(context, layout, cursor, flags);
        this.emptyCheckedListener = emptyCheckedListener;
        checkedFriends = new HashSet<>();
        this.context = context;
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

            //set name and check value
            viewHolder.username.setText(friendUsername);
            setView(userId, view, viewHolder.checkBox);


            //configure user_icon
            Drawable background = viewHolder.friendIcon.getBackground();
            ((GradientDrawable) background).setColor(
                    AppConstants.COLOR_ARRAY_LIST[userId % AppConstants.COLOR_ARRAY_LIST.length]);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    viewHolder.checkBox.toggle();
                    toggleSelected(userId, view);
                }
            });
        }
    }

    //initialize viewHolder
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = super.newView(context, cursor, parent);
        ViewHolder viewHolder = new ViewHolder();

        viewHolder.username = (TextView) view.findViewById(R.id.friend_username);
        viewHolder.checkBox = (CheckBox) view.findViewById(R.id.checkbox);
        viewHolder.friendIcon = (ImageView) view.findViewById(R.id.friend_icon);

        view.setTag(viewHolder);

        return view;
    }

    /**
     * Gets a set of selected friends
     *
     * @return Set of selected friend IDs
     */
    public HashSet<Integer> getCheckedFriends() {
        return checkedFriends;
    }

    /**
     * Toggles whether or not a friend has been selected
     *
     * @param userId The id of the friend to be toggled
     * @param view   friend item view
     */
    public void toggleSelected(int userId, View view) {
        if (checkedFriends.contains(userId)) {
            checkedFriends.remove(userId);
            if (checkedFriends.isEmpty()) emptyCheckedListener.isEmpty(true);
            view.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        } else {
            checkedFriends.add(userId);
            if (checkedFriends.size() == 1) emptyCheckedListener.isEmpty(false);
            view.setBackgroundColor(context.getResources().getColor(R.color.grey_300));
        }
    }

    /**
     * binds the view with the checkbox
     *
     * @param userId   friend user id
     * @param view     friend item view
     * @param checkBox checkbox to be toggled
     */
    public void setView(int userId, View view, CheckBox checkBox) {
        if (checkedFriends.contains((userId))) {
            checkBox.setChecked(true);
            view.setBackgroundColor(context.getResources()
                    .getColor(R.color.grey_300));
        } else {
            checkBox.setChecked(false);
            view.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        }
    }

    public interface EmptyCheckedListener {
        /**
         * Called when the set of selected friends changes size from 0 to one
         *
         * @param isEmpty true if Set of selected friends is empty, otherwise false
         */
        void isEmpty(Boolean isEmpty);
    }

    private class ViewHolder {
        public TextView username;//username
        public CheckBox checkBox; // new game TODO add this button
        public ImageView friendIcon;
    }
}
