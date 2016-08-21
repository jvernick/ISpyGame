package com.picspy.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.picspy.adapters.DatabaseHandler;
import com.picspy.firstapp.R;
import com.picspy.models.Friend;
import com.picspy.models.FriendRecord;
import com.picspy.models.UserRecord;
import com.picspy.utils.AppConstants;
import com.picspy.utils.FriendsRequests;
import com.picspy.utils.PrefUtil;
import com.picspy.utils.VolleyRequest;
import com.picspy.views.custom.Circle;
import com.picspy.views.custom.CircleAngleAnimation;

import java.util.ArrayList;

/**
 * Activity that displays friend information
 * Start activity only through one of the following methods:
 * {@link #startActivityForFriend(String, Integer, Context)}
 * {@link #startActivityForUser(UserRecord, Context)}
 * This ensures that the appropriate attributes are set.
 */
public class FriendInfoActivity extends ActionBarActivity {
    private static final String USERNAME = "username";
    private static final String WON = "won";
    private static final String LOST = "lost";
    private static final String L_BOARD = "l_board";
    private static final String FRIEND_ID = "com.picspy.FRIEND_ID";
    private static final String FOR_FRIEND = "com.picspy.FRIEND_REQUEST";
    private static final String TAG = "FriendsInfoActivity";
    private static final String CANCEL_TAG = "deleteFriend";
    private static final int MENU_DELETE_ID = 1;
    public static final int ANIMATION_DURATION = 750;
    // stat views
    private TextView fromFriendWon, fromFriendLost, toFriendWon, toFriendLost;
    private TextView userTotalWon;
    private TextView userTotalLost;
    private TextView userLeaderboard;
    private TextView sentSummary, receivedSummary, friendUsername;
    // Parent cards
    private View fromFriendCard, toFriendCard, userStatsCard, userProfileCard;
    private TextView emptyView;
    private ProgressBar progressSpinner;
    private int userId, myId;
    private String userUsername;
    private boolean requestSuccessful = false;
    private boolean forFriend;
    private ArrayList<AlertDialog> alertDialogs = new ArrayList<>();
    private Menu menu;
    private Circle sentCircle;
    private Circle receivedCircle;
    private View startGameFab, noFriendStatsTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_info);
        Intent intent = getIntent();
        forFriend = intent.getBooleanExtra(FOR_FRIEND, false);
        myId = PrefUtil.getInt(getApplicationContext(), AppConstants.USER_ID);
        initializeViews();

        progressSpinner = (ProgressBar) findViewById(R.id.myProgressBar);

        processIntent();
        if (forFriend) {
            getStats(userId);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.friend_info_toolbar);
        // Setting toolbar as the ActionBar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_chevron_left_white);

        getSupportActionBar().setTitle(getString(R.string.info_page_title));
    }

    /**
     * Configures the appropriate intents and starts this activity for friends
     *
     * @param userName friend's username
     * @param id       friend's userId
     * @param context  Callee context
     */
    public static void startActivityForFriend(String userName, Integer id, Context context) {
        Intent intent = new Intent(context, FriendInfoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(FriendInfoActivity.FOR_FRIEND, true);
        intent.putExtra(FriendInfoActivity.USERNAME, userName);
        intent.putExtra(FriendInfoActivity.FRIEND_ID, id);
        context.startActivity(intent);
    }

    /**
     * Configures the appropriate intents and starts this activity for non-friends
     *
     * @param userRecord UserRecord containing user info
     * @param context    Callee context
     */
    public static void startActivityForUser(UserRecord userRecord, Context context) {
        Intent intent = new Intent(context, FriendInfoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(FriendInfoActivity.FOR_FRIEND, false);
        intent.putExtra(FriendInfoActivity.USERNAME, userRecord.getUsername());
        intent.putExtra(FriendInfoActivity.WON, userRecord.getTotal_won());
        intent.putExtra(FriendInfoActivity.LOST, userRecord.getTotal_lost());
        intent.putExtra(FriendInfoActivity.L_BOARD, userRecord.getLeaderboard());
        context.startActivity(intent);
    }

    /**
     * Gets information about the user to be displayed from the intent bundle
     */
    private void processIntent() {
        Intent intent = getIntent();
        userUsername = intent.getStringExtra(USERNAME);
        if (forFriend) {
            userId = intent.getIntExtra(FRIEND_ID, -1);
        } else {
            UserRecord record = new UserRecord();
            record.setUsername(userUsername);
            record.setTotal_won(intent.getIntExtra(WON, -1));
            record.setTotal_lost(intent.getIntExtra(LOST, -1));
            record.setLeaderboard(intent.getIntExtra(L_BOARD, -1));

            setUserStats(record);
            noFriendStatsTitle.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Initializes all the views in this activity and hides the friends stat depending
     * on whether the user to be displayed is a friend
     */
    private void initializeViews() {
        fromFriendWon = (TextView) findViewById(R.id.sent_won);
        fromFriendLost = (TextView) findViewById(R.id.sent_lost);
        toFriendWon = (TextView) findViewById(R.id.received_won);
        toFriendLost = (TextView) findViewById(R.id.received_lost);
        userTotalWon = (TextView) findViewById(R.id.total_won);
        userTotalLost = (TextView) findViewById(R.id.total_lost);
        userLeaderboard = (TextView) findViewById(R.id.leaderboard);

        // Summary views
        sentSummary = (TextView) findViewById(R.id.sent_summary);
        receivedSummary = (TextView) findViewById(R.id.received_summary);
        friendUsername = (TextView) findViewById(R.id.friend_username);

        // Parent views
        fromFriendCard = findViewById(R.id.from_friend_card);
        toFriendCard = findViewById(R.id.to_friend_card);
        userStatsCard = findViewById(R.id.user_stats_card);
        userProfileCard = findViewById(R.id.user_profile_card);

        // Button
        startGameFab = findViewById(R.id.new_game_fab);

        // Empty views
        emptyView = (TextView) findViewById(R.id.empty_view);
        noFriendStatsTitle = findViewById(R.id.no_friend_stats_title);

        if (forFriend) {
            // init drawing
            sentCircle = (Circle) findViewById(R.id.sent_circle);
            receivedCircle = (Circle) findViewById(R.id.received_circle);

        } else {
            // hide drawing
            if (requestSuccessful) findViewById(R.id.joint_stats).setVisibility(View.GONE);
        }

        userStatsCard.setVisibility(View.GONE);
        userProfileCard.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_friend_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case (android.R.id.home):
                onBackPressed();
                return true;
            case (MENU_DELETE_ID):
                if (!requestSuccessful) { //Do nothing if web request was unsuccessful
                    return false;
                }
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(FriendInfoActivity.this);
                alertDialogs.add(
                        alertDialog.setTitle(getString(R.string.dialog_remove_friend_title))
                                   .setMessage(getString(R.string.dialog_remove_friend_message))
                                   .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                       @Override
                                       public void onClick(DialogInterface dialog, int which) {
                                           deleteFriend(userId);
                                       }
                                   }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                dialog.dismiss();
                            }
                        })
                                   .setIcon(android.R.drawable.ic_dialog_alert)
                                   .show()
                );

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Starts a new game with this user
     *
     * @param v view from button click
     */
    public void startGame(View v) {
        Intent intent = new Intent(this, CameraActivity.class);
        intent.putExtra(SendChallenge.ARG_FRIEND_ID, userId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * Sets the stats for the given Friend
     *
     * @param userRecord Record containing information about the user (games won, lost, leaderboard)
     */
    private void setUserStats(UserRecord userRecord) {
        friendUsername.setText(userUsername);
        userTotalWon.setText(String.valueOf(userRecord.getTotal_won()));
        userTotalLost.setText(String.valueOf(userRecord.getTotal_lost()));
        userLeaderboard.setText(String.valueOf(userRecord.getLeaderboard()));

        userProfileCard.setVisibility(View.VISIBLE);
        userStatsCard.setVisibility(View.VISIBLE);
    }

    /**
     * Calls the setFriendsStats method with the appropriate friends mapped
     *
     * @param friendRecord record containing friend information
     */
    private void setFriendStats(FriendRecord friendRecord) {
        int friend1Won = friendRecord.getFriend_1_won();
        int friend2Won = friendRecord.getFriend_2_won();
        int friend1Lost = friendRecord.getFriend_1_lost();
        int friend2Lost = friendRecord.getFriend_2_lost();

        if (userId < myId) {
            setFriendStats(friend1Won, friend1Lost, friend2Won, friend2Lost);
        } else {
            setFriendStats(friend2Won, friend2Lost, friend1Won, friend1Lost);
        }
    }

    /**
     * Sets all the stats for games played between friends.
     * friend1 and friend2 must be mapped to the appropriate values
     *
     * @param friend1Won  Number of games friend_1 won
     * @param friend1Lost Number of games friend_1 lost
     * @param friend2Won  Number of games friend_2 won
     * @param friend2Lost Number of games friend_2 lost
     */
    private void setFriendStats(int friend1Won, int friend1Lost, int friend2Won, int friend2Lost) {
        int totalTop = friend1Won + friend1Lost;
        int totalBottom = friend2Won + friend2Lost;
        //default values if total1 or total2 are 0
        float topPercentage = 0.5f;
        float bottomPercentage = 0.5f;

        fromFriendWon.setText(String.valueOf(friend1Won));
        fromFriendLost.setText(String.valueOf(friend1Lost));
        toFriendWon.setText(String.valueOf(friend2Won));
        toFriendLost.setText(String.valueOf(friend2Lost));

        if (totalTop != 0) {
            topPercentage = friend1Lost / totalTop;
        }
        if (totalBottom != 0) {
            bottomPercentage = friend2Lost / totalBottom;
        }

        CharSequence sentText = Html.fromHtml(String.format(getString(R.string.sent_summary),
                (int) ((1 - topPercentage) * 100), userUsername));
        CharSequence receivedText = Html.fromHtml(String.format(getString(R.string.received_summary),
                userUsername, (int) ((1 - bottomPercentage) * 100)));
        CharSequence emptyReceivedText = Html.fromHtml(String.format(getString(R.string.empty_received_challenges),
                userUsername));
        CharSequence emptySentText = Html.fromHtml(String.format(getString(R.string.empty_sent_challenges),
                userUsername));

        sentText = totalTop == 0 ? emptyReceivedText : sentText;
        receivedText = (totalBottom == 0) ? emptySentText : receivedText;
        sentSummary.setText(sentText);
        receivedSummary.setText(receivedText);

        CircleAngleAnimation animation1 = new CircleAngleAnimation(sentCircle, topPercentage * 360);
        CircleAngleAnimation animation2 = new CircleAngleAnimation(receivedCircle, bottomPercentage * 360);
        animation1.setDuration(ANIMATION_DURATION);
        animation2.setDuration(ANIMATION_DURATION);

        sentCircle.startAnimation(animation1);
        receivedCircle.startAnimation(animation2);

        fromFriendCard.setVisibility(View.VISIBLE);
        toFriendCard.setVisibility(View.VISIBLE);
        startGameFab.setVisibility(View.VISIBLE);
    }

    /**
     * Gets a user's stats from the server
     *
     * @param friend_id The user whose is to be obtained
     */
    private void getStats(int friend_id) {
        Response.Listener<FriendRecord> response = new Response.Listener<FriendRecord>() {
            @Override
            public void onResponse(FriendRecord response) {
                progressSpinner.setVisibility(View.GONE);
                if (response != null) {
                    menu.add(Menu.NONE, MENU_DELETE_ID, Menu.NONE, R.string.friend_delete_menu)
                        .setIcon(R.drawable.ic_delete)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                    setUserStats(response.getOtherUserRecord(myId));
                    setFriendStats(response);
                    findViewById(R.id.page).setVisibility(View.VISIBLE);
                    requestSuccessful = true;
                } else {
                    progressSpinner.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressSpinner.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
                if (error != null) {
                    String err = (error.getMessage() == null) ? "An error occurred" : error.getMessage();
                    Log.d(TAG, err);
                    //Show toast only if there is no server connection on refresh
                    if (err.matches(AppConstants.CONNECTION_ERROR) || err.matches(AppConstants.TIMEOUT_ERROR)) {
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate(R.layout.view_network_error_toast,
                                (ViewGroup) findViewById(R.id.toast_layout_root));
                        Toast toast = new Toast(getApplicationContext());
                        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                        toast.setDuration(Toast.LENGTH_LONG);
                        toast.setView(layout);
                        toast.show();
                    }
                }
            }
        };

        progressSpinner.setVisibility(View.VISIBLE);

        FriendsRequests statsRequest = FriendsRequests.getStats(this, friend_id, response, errorListener);
        if (statsRequest != null) statsRequest.setTag(CANCEL_TAG);
        VolleyRequest.getInstance(getApplicationContext()).addToRequestQueue(statsRequest);
    }

    /**
     * Delete friend on server
     *
     * @param friend_id friend to be deleted
     */
    private void deleteFriend(final int friend_id) {
        Response.Listener<FriendRecord> responseListener = new Response.Listener<FriendRecord>() {
            @Override
            public void onResponse(FriendRecord response) {
                if (response != null) {
                    DatabaseHandler dbHandler = DatabaseHandler.getInstance((getApplicationContext()));
                    if (dbHandler.getFriend(friend_id) != null) {
                        dbHandler.deleteFriend(new Friend(friend_id, null));
                        PrefUtil.putBoolean(getApplicationContext(), AppConstants.UPDATE_FRIEND_LIST, true);
                    }

                    onBackPressed();
                    Toast.makeText(FriendInfoActivity.this, getString(R.string.toast_friend_removed),
                            Toast.LENGTH_SHORT).show();
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressSpinner.setVisibility(View.GONE);
                if (error != null) {
                    String err = (error.getMessage() == null) ? "An error occurred" : error.getMessage();
                    Log.d(TAG, err);
                    //Show toast only if there is no server connection on refresh
                    if (err.matches(AppConstants.CONNECTION_ERROR) || err.matches(AppConstants.TIMEOUT_ERROR)) {
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate(R.layout.view_network_error_toast,
                                (ViewGroup) findViewById(R.id.toast_layout_root));
                        Toast toast = new Toast(getApplicationContext());
                        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                        toast.setDuration(Toast.LENGTH_LONG);
                        toast.setView(layout);
                        toast.show();
                    }
                }
            }
        };

        FriendsRequests deleteFriedRequest = FriendsRequests.removeFriend(this, friend_id,
                responseListener, errorListener);
        if (deleteFriedRequest != null) deleteFriedRequest.setTag(CANCEL_TAG);
        VolleyRequest.getInstance(getApplicationContext()).addToRequestQueue(deleteFriedRequest);
    }

    @Override
    protected void onStop() {
        super.onStop();
        for (AlertDialog d : alertDialogs) {
            if (d.isShowing()) d.dismiss();
        }
        //cancel all pending volley requests
        VolleyRequest.getInstance(this.getApplication()).getRequestQueue().cancelAll(CANCEL_TAG);
    }
}
