package com.picspy.adapters;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.picspy.firstapp.R;
import com.picspy.models.UserRecord;
import com.picspy.utils.AppConstants;
import com.picspy.views.FriendInfoActivity;

import java.util.ArrayList;

/**
 * RecyclerAdapter for the FriendRequests fragment
 */
public class FriendRequestsRecyclerAdapter extends RecyclerView.Adapter<FriendRequestsRecyclerAdapter.ViewHolder> {
    ArrayList<UserRecord> mDataset;
    private AdapterRequestListener adapterRequestListener;

    public FriendRequestsRecyclerAdapter(ArrayList<UserRecord> mDataset) {
        this.mDataset = mDataset;
    }

    public void setAdapterRequestListener(AdapterRequestListener adapterRequestListener) {
        this.adapterRequestListener = adapterRequestListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_friend_request, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        final UserRecord requestor = mDataset.get(position);
        viewHolder.friendUsername.setText(requestor.getUsername());
        viewHolder.acceptFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {//delay for state animation
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                adapterRequestListener.acceptRequest(requestor, position);
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

                adapterRequestListener.declineRequest(requestor, position);
            }
        });

        viewHolder.friendUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FriendInfoActivity.startActivityForUser(requestor, view.getContext());
            }
        });

        Drawable background = viewHolder.friendIcon.getBackground();
        ((GradientDrawable) background).setColor(AppConstants.COLOR_ARRAY_LIST[requestor.getId() %
                AppConstants.COLOR_ARRAY_LIST.length]);
        viewHolder.friendIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FriendInfoActivity.startActivityForUser(requestor, view.getContext());
            }
        });

        viewHolder.iconAndName.setAddStatesFromChildren(true);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void add(int position, UserRecord item) {
        mDataset.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(UserRecord item) {
        int position = mDataset.indexOf(item);
        mDataset.remove(position);
        notifyItemRemoved(position);
    }

    public void setData(ArrayList<UserRecord> data) {
        mDataset = data;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        mDataset.remove(position);
        notifyItemRemoved(position);
    }

    public interface AdapterRequestListener {
        void acceptRequest(UserRecord userRecord, int position);

        void declineRequest(UserRecord userRecord, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView friendIcon;
        public TextView friendUsername;
        public Button acceptFriend;
        public Button declineFriend;
        public ViewGroup iconAndName;

        public ViewHolder(View itemView) {
            super(itemView);
            friendIcon = (ImageView) itemView.findViewById(R.id.request_icon);
            friendUsername = (TextView) itemView.findViewById(R.id.request_username);
            acceptFriend = (Button) itemView.findViewById(R.id.accept_friend);
            declineFriend = (Button) itemView.findViewById(R.id.decline_friend);
            iconAndName = (ViewGroup) itemView.findViewById(R.id.icon_and_name);
        }
    }
}
