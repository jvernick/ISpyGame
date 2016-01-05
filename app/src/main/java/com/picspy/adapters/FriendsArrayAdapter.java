package com.picspy.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.picspy.firstapp.R;
import com.picspy.models.Friend;
import com.picspy.utils.AppConstants;
import com.picspy.utils.DbContract;
import com.picspy.views.CameraActivity;

/**
 * Created by BrunelAmC on 1/3/2016.
 */
public class FriendsArrayAdapter extends ArrayAdapter<Friend> {
    private final LayoutInflater inflater;

    public FriendsArrayAdapter(Context context, int resource) {
        super(context, resource);
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_friends, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.unameTextView = (TextView) convertView.findViewById(R.id.friend_username);
            viewHolder.newGameButton = (ImageView) convertView.findViewById(R.id.game_start);
            viewHolder.friendIcon = (ImageView) convertView.findViewById(R.id.friend_icon);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Find fields to populate in inflated template
        final String friendUsername = getItem(position).getUsername();
        final int userId = getItem(position).getId();

        viewHolder.unameTextView.setText(friendUsername);

        //Start friendInfoActivity when friend username clicked
        viewHolder.unameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FriendsCursorAdapter.startFriendInfoActivity(view, true, friendUsername, userId);
            }
        });

        //start a new game with this friend
        viewHolder.newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), CameraActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //TODO change intent extra to appropriate value if any
                view.getContext().startActivity(intent);
            }
        });

        Drawable background = viewHolder.friendIcon.getBackground();
        ((GradientDrawable)background).setColor(
                AppConstants.COLOR_ARRAY_LIST[userId % AppConstants.COLOR_ARRAY_LIST.length]);
        //Start friendInfoActivity when friend icon clicked
        viewHolder.friendIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FriendsCursorAdapter.startFriendInfoActivity(view, true, friendUsername, userId);
            }
        });
        return super.getView(position, convertView, parent);
    }

    private class ViewHolder {
        public TextView unameTextView;//username
        public ImageView newGameButton; // new game TODO add this button
        public ImageView friendIcon;
    }
}
