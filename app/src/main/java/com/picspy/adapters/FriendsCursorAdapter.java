package com.picspy.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.picspy.firstapp.R;
import com.picspy.utils.DbContract;
import com.picspy.views.CameraActivity;
import com.picspy.views.FriendInfoActivity;

/**
 * Created by Gordon on 8/22/2015.
 */
public class FriendsCursorAdapter extends ResourceCursorAdapter {


    public FriendsCursorAdapter(Context context, int layout, Cursor cursor, int flags) {
        super(context,layout, cursor, flags);
    }

    /**
     * Binds username and user_id to the list element
     * @param view The view to be populated
     * @param context The calling application context
     * @param c Cursor containing result of database query
     */
    @Override
    public void bindView(View view, final Context context, Cursor c) {
        // Find fields to populate in inflated template
        final ViewHolder viewHolder = (ViewHolder) view.getTag();

        viewHolder.unameTextView.setText(
                c.getString(c.getColumnIndex(DbContract.FriendEntry.COLUMN_NAME_USERNAME)));
        viewHolder.unameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, FriendInfoActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(FriendInfoActivity.FRIEND_USERNAME,
                        ((TextView) view).getText().toString());
                view.getContext().startActivity(intent);
            }
        });

       /* //start a new game with this friend
        viewHolder.newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, CameraActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //TODO change intent extra to appropriate value if any
                view.getContext().startActivity(intent);
            }
        });*/
    }

    //initialize viewHolder
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = super.newView(context, cursor, parent);
        ViewHolder viewHolder = new ViewHolder();

        viewHolder.unameTextView = (TextView) view.findViewById(R.id.friend_username);
        //viewHolder.newGameButton = (Button) view.findViewById(R.id.startGame);

        view.setTag(viewHolder);

        return view;
    }


    private class ViewHolder {
        TextView unameTextView;//username
        Button newGameButton; // new game TODO add this button
    }
}
