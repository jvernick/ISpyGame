package com.picspy.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.picspy.firstapp.R;
import com.picspy.models.Friend;

import java.util.List;

/**
 * Created by BrunelAmC on 12/28/2015.
 */
public class FindFriendsArrayAdapter extends ArrayAdapter<Friend>{
    private final LayoutInflater inflater;

    public FindFriendsArrayAdapter(Context context, List<Friend> requests) {
        super(context,0, requests);
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_friend_request, parent, false);
            viewHolder = new ViewHolder();

            viewHolder.friendIcon = (ImageView) convertView.findViewById(R.id.request_icon);
            viewHolder.friendUsername = (TextView) convertView.findViewById(R.id.request_username);
            viewHolder.acceptFriend = (Button) convertView.findViewById(R.id.accept_friend);
            viewHolder.declineFriend = (Button) convertView.findViewById(R.id.decline_friend);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final Friend requestor = getItem(position);
        viewHolder.friendUsername.setText(requestor.getUsername());
        viewHolder.acceptFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {//delay for state animation
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                remove(getItem(position));
                notifyDataSetChanged();
                //TODO add friend to database
                //TODO Send response to server
            }
        });
        viewHolder.declineFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {//delay for state animation
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                remove(getItem(position));
                notifyDataSetChanged();
                //TODO Send response to server
            }
        });

        viewHolder.friendUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FriendsCursorAdapter.startFriendInfoActivity(view);
            }
        });

        viewHolder.friendIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FriendsCursorAdapter.startFriendInfoActivity(view);
            }
        });
        return convertView;
    }

    public void setData(List<Friend> data) {
        clear();
        if (data != null) {
            addAll(data);
        }
        notifyDataSetChanged();
    }

    private class ViewHolder {
        public ImageView friendIcon;
        public TextView friendUsername;
        public Button acceptFriend;
        public Button declineFriend;

    }
}
