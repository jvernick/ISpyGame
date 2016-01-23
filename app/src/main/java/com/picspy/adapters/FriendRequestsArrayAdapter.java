package com.picspy.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.picspy.FriendsTableRequests;
import com.picspy.firstapp.R;
import com.picspy.models.Friend;
import com.picspy.models.FriendRecord;
import com.picspy.utils.AppConstants;
import com.picspy.utils.FriendsRequests;
import com.picspy.utils.PrefUtil;
import com.picspy.utils.VolleyRequest;
import com.picspy.views.FindFriendsActivity;

import java.util.List;
import java.util.regex.Pattern;

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
                Log.d("RequestArrayAdapter", requestor.getRecordId() + "");
                try {//delay for state animation
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                acceptRequest(requestor.getId());
                remove(getItem(position));
                notifyDataSetChanged();
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

                deleteRequest(requestor.getId());
                remove(getItem(position));
                notifyDataSetChanged();
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

    private void deleteRequest(final int friend_id) {
        Response.Listener<FriendRecord> responseListener = new Response.Listener<FriendRecord>() {
            @Override
            public void onResponse(FriendRecord response) {
                if (response != null ) {
                    Toast.makeText(getContext(), "Friend request removed",
                            Toast.LENGTH_SHORT).show();
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //ToDO  parse and notify
                Pattern p1 = Pattern.compile(".*[cC]onnection.*[rR]efused.*", Pattern.DOTALL);
                Pattern p2 = Pattern.compile(".*timed out", Pattern.DOTALL);
                Log.d("Delete Friend Request", error.getMessage());
                Toast.makeText(getContext(), "An error occurred",
                        Toast.LENGTH_SHORT).show();
            }
        };

        FriendsRequests deleteFriedRequest = FriendsRequests.removeFriend(getContext(), friend_id, responseListener, errorListener);
        if (deleteFriedRequest != null) deleteFriedRequest.setTag(FindFriendsActivity.CANCEL_TAG);
        VolleyRequest.getInstance(getContext().getApplicationContext()).addToRequestQueue(deleteFriedRequest);
    }

    private void acceptRequest(int friend_id) {
        Response.Listener<FriendRecord> responseListener = new Response.Listener<FriendRecord>() {
            @Override
            public void onResponse(FriendRecord response) {
                if (response != null ) {
                    Toast.makeText(getContext(), "Friend request accepted",
                            Toast.LENGTH_SHORT).show();
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //ToDO  parse and notify
                Pattern p1 = Pattern.compile(".*[cC]onnection.*[rR]efused.*", Pattern.DOTALL);
                Pattern p2 = Pattern.compile(".*timed out", Pattern.DOTALL);
                Log.d("Delete Friend Request", error.getMessage());
                Toast.makeText(getContext(), "An error occurred",
                        Toast.LENGTH_SHORT).show();
            }
        };

        FriendsRequests deleteFriedRequest = FriendsRequests.acceptFriendRequest(getContext(), friend_id, responseListener, errorListener);
        if (deleteFriedRequest != null) deleteFriedRequest.setTag(FindFriendsActivity.CANCEL_TAG);
        VolleyRequest.getInstance(getContext().getApplicationContext()).addToRequestQueue(deleteFriedRequest);
    }
}
