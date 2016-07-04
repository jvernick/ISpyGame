package com.picspy.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.picspy.firstapp.R;
import com.picspy.models.UserRecord;
import com.picspy.utils.AppConstants;

import java.util.List;

/**
 * Created by BrunelAmC on 12/28/2015.
 */
public class FriendRequestsArrayAdapter extends ArrayAdapter<UserRecord> {
    private final LayoutInflater inflater;
    private AdapterRequestListener adapterRequestListener;

    public FriendRequestsArrayAdapter(Context context, int item_friend_request, List<UserRecord> userRecord) {
        super(context, item_friend_request, userRecord);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setAdapterRequestListener(AdapterRequestListener adapterRequestListener) {
        this.adapterRequestListener = adapterRequestListener;
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

        final UserRecord requestor = getItem(position);
        viewHolder.friendUsername.setText(requestor.getUsername());

        viewHolder.acceptFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {//delay for state animation
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                adapterRequestListener.acceptRequest(requestor.getId(), position);
            }
        });

        viewHolder.declineFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("RequestArrayAdapter", "decline");
                try {//delay for state animation
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                adapterRequestListener.declineRequest(requestor.getId(), position);
            }
        });

        viewHolder.friendUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FriendsCursorAdapter.startFriendInfoActivity(view, null, null, requestor);
            }
        });

        Drawable background = viewHolder.friendIcon.getBackground();
        ((GradientDrawable) background).setColor(AppConstants.COLOR_ARRAY_LIST[requestor.getId() %
                AppConstants.COLOR_ARRAY_LIST.length]);
        viewHolder.friendIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FriendsCursorAdapter.startFriendInfoActivity(view, null, null, requestor);
            }
        });

        return convertView;
    }

    public void removeItem(int position) {
        remove(getItem(position));
        notifyDataSetChanged();
    }

    public void setData(List<UserRecord> data) {
        clear();
        if (data != null) {
            addAll(data);
        }
        notifyDataSetChanged();
    }

    public interface AdapterRequestListener {
        void acceptRequest(int friend_id, int position);

        void declineRequest(int friend_id, int position);
    }

    private class ViewHolder {
        public ImageView friendIcon;
        public TextView friendUsername;
        public Button acceptFriend;
        public Button declineFriend;

    }
}
