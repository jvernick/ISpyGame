package com.picspy.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.picspy.FriendsTableRequests;
import com.picspy.firstapp.R;
import com.picspy.models.Friend;
import com.picspy.utils.AppConstants;

import java.util.List;

/**
 * Created by BrunelAmC on 12/28/2015.
 */
public class FriendRequestsArrayAdapter extends ArrayAdapter<Friend>{
    private final LayoutInflater inflater;

    public FriendRequestsArrayAdapter(Context context, List<Friend> requests) {
        super(context,0, requests);
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public FriendRequestsArrayAdapter(Context context, int item_friend_request, List<Friend> friends) {
        super(context,item_friend_request,friends);
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
                //TODO verify for success or error
                (new RequestResponse(view.getContext(), requestor.getId(), true)).execute();

                remove(getItem(position));
                notifyDataSetChanged();
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

                (new RequestResponse(view.getContext(), requestor.getId(), false)).execute();
                remove(getItem(position));
                notifyDataSetChanged();
                //TODO Send response to server
            }
        });

        viewHolder.friendUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FriendsCursorAdapter.startFriendInfoActivity(view, false,
                        requestor.getUsername(), requestor.getId());
            }
        });


        Drawable background = viewHolder.friendIcon.getBackground();
        ((GradientDrawable)background).setColor(AppConstants.COLOR_ARRAY_LIST[requestor.getId() %
                AppConstants.COLOR_ARRAY_LIST.length]);
        viewHolder.friendIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FriendsCursorAdapter.startFriendInfoActivity(view, false,
                        requestor.getUsername(), requestor.getId());
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

    private class RequestResponse extends AsyncTask<Void, Void, String> {
        private final Context context;
        private final int id;
        private final boolean accept;

        /**
         *
         * @param context context
         * @param id    friend_id
         * @param accept accept friend if true, otherwise delete request(friend)
         */
        public RequestResponse(Context context, int id, boolean accept) {
            super();
            this.context = context;
            this.id = id;
            this.accept = accept;
        }

        @Override
        protected String doInBackground(Void... voids) {
            if (accept) {
                return (new FriendsTableRequests(context)).acceptFriendRequest(id);
            } else {
                return (new FriendsTableRequests(context)).removeFriend(id);
            }
        }
    }
}
