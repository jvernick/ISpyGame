package com.picspy.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.picspy.firstapp.R;
import com.picspy.models.UserRecord;
import com.picspy.utils.AppConstants;

import java.util.List;

/**
 * Created by BrunelAmC on 12/28/2015.
 */
public class FindFriendArrayAdapter extends ArrayAdapter<UserRecord>{
    private final LayoutInflater inflater;

    public  FindFriendArrayAdapter(Context context, int resource) {
        super(context, resource);
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_addfriend, parent, false);
            viewHolder = new ViewHolder();

            viewHolder.userIcon = (ImageView) convertView.findViewById(R.id.user_icon);
            viewHolder.userUsername = (TextView) convertView.findViewById(R.id.user_username);
            //viewHolder.acceptFriend = (Button) convertView.findViewById(R.id.accept_friend);
            //viewHolder.declineFriend = (Button) convertView.findViewById(R.id.decline_friend);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final UserRecord userRecord = getItem(position);

        Log.d("arrayAdapter", getCount() + "   " + position);
        viewHolder.userUsername.setText(userRecord.getUsername());
        /*viewHolder.acceptFriend.setOnClickListener(new View.OnClickListener() {
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
        });*/

        viewHolder.userUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FriendsCursorAdapter.startFriendInfoActivity(view, false,
                        userRecord.getUsername(), userRecord.getId());
            }
        });


        Drawable background = viewHolder.userIcon.getBackground();
        ((GradientDrawable)background).setColor(AppConstants.COLOR_ARRAY_LIST[userRecord.getId() %
                AppConstants.COLOR_ARRAY_LIST.length]);
        viewHolder.userIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FriendsCursorAdapter.startFriendInfoActivity(view, false,
                        userRecord.getUsername(), userRecord.getId());
            }
        });
        return convertView;
    }

    public void setData(List<UserRecord> data) {
        clear();
        if (data != null) {
            addAll(data);
        }
        notifyDataSetChanged();
    }

    private class ViewHolder {
        public ImageView userIcon;
        public TextView userUsername;
        //public Button acceptFriend;
       // public Button declineFriend;

    }
}
