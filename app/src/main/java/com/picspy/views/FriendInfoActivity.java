package com.picspy.views;

import android.app.AlertDialog;
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
 * TODo revise this documentation
 * When starting activity, pass the following information to the intent:
 * FRIEND_ID && FRIEND_USERNAME : if this is for a friend
 * FOR_FRIEND : if this for a friend request
 * Nothing : if this is for self.
 */
public class FriendInfoActivity extends ActionBarActivity {
    public static final String USERNAME = "username";
    public static final String WON = "won";
    public static final String LOST = "lost";
    public static final String L_BOARD = "l_board";
    public static final String FRIEND_ID = "com.picspy.FRIEND_ID";
    public static final String FOR_FRIEND = "com.picspy.FRIEND_REQUEST";

    private static final String TAG = "FriendsInfoActivity";
    private static final String CANCEL_TAG = "deleteFriend";
    private static final int MENU_DELETE_ID = 1;
    // stat views
    private TextView fromFriendWon, fromFriendLost, toFriendWon, toFriendLost;
    private TextView userTotalWon, userTotalLost, userLeaderboard, userStatsTitle;
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
        toolbar.setNavigationIcon(R.drawable.ic_chevron_left);

        if (userUsername != null) {
            getSupportActionBar().setTitle("Profile"); //TODO change to resource
        }
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
     * Initializes all the views in this activity and hides the friends start depending
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
        userStatsTitle = (TextView) findViewById(R.id.stats_title);

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
        noFriendStatsTitle = (TextView) findViewById(R.id.no_friend_stats_title);

        if (forFriend) {
            // init drawing
            sentCircle = (Circle) findViewById(R.id.sent_circle);
            receivedCircle = (Circle) findViewById(R.id.received_circle);

        } else {
            // hide drawing
            if (requestSuccessful) findViewById(R.id.joint_stats).setVisibility(View.GONE);
        }

        //TODO move to xml
        fromFriendCard.setVisibility(View.GONE);
        toFriendCard.setVisibility(View.GONE);
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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
                        alertDialog.setTitle("Remove Friend")
                                .setMessage("Are you sure you want to remove this" + " user?")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //TODO ensure that on return to parent, friends list is reloaded in on resume
                                        //one way will be to return boolean to parent or always refresh
                                        deleteFriend(userId);
                                    }
                                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                dialog.dismiss();
                            }
                        })
                                .setIcon(android.R.drawable.ic_dialog_alert) //TODO change alert color to red?
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
        //TODO Start activity to create new game
        Toast.makeText(FriendInfoActivity.this, "Game started", Toast.LENGTH_SHORT).show();
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
        //findViewById(R.id.page).setVisibility(View.VISIBLE); //TODO cleanup
        userProfileCard.setVisibility(View.VISIBLE);
        userStatsCard.setVisibility(View.VISIBLE);
    }

    /**
     * Calls the setFriendsStats method with the apprpriate friends mapped
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

        Log.d(TAG, "  " + topPercentage + " " + bottomPercentage);
        CharSequence text1 = Html.fromHtml(String.format(getString(R.string.sent_summary),
                (int) ((1 - topPercentage) * 100), userUsername));
        CharSequence text2 = Html.fromHtml(String.format(getString(R.string.received_summary),
                userUsername, (int) ((1 - bottomPercentage) * 100)));
        text1 = totalTop == 0 ? getString(R.string.empty_received_challenges) : text1;
        sentSummary.setText(text1);
        text2 = (totalBottom == 0) ? getString(R.string.empty_sent_challenges) : text2;
        receivedSummary.setText(text2);

        sentSummary.setText(text1);
        receivedSummary.setText(text2);

        CircleAngleAnimation animation1 = new CircleAngleAnimation(sentCircle, topPercentage * 360);
        CircleAngleAnimation animation2 = new CircleAngleAnimation(receivedCircle, bottomPercentage * 360);
        animation1.setDuration(750);
        animation2.setDuration(750);

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
                    error.printStackTrace();
                    Log.d(TAG, err);
                    //Show toast only if there is no server connection on refresh
                    if (err.matches(AppConstants.CONNECTION_ERROR) || err.matches(AppConstants.TIMEOUT_ERROR)) {
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate(R.layout.custom_toast,
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
                    }

                    onBackPressed();
                    Toast.makeText(FriendInfoActivity.this, "Friend successfully removed",
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
                    error.printStackTrace();
                    Log.d(TAG, err);
                    //Show toast only if there is no server connection on refresh
                    if (err.matches(AppConstants.CONNECTION_ERROR) || err.matches(AppConstants.TIMEOUT_ERROR)) {
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate(R.layout.custom_toast,
                                (ViewGroup) findViewById(R.id.toast_layout_root));
                        Toast toast = new Toast(getApplicationContext());
                        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                        toast.setDuration(Toast.LENGTH_LONG);
                        toast.setView(layout);
                        toast.show();
                    } else { //TODO for debugging, remove
                        Toast.makeText(FriendInfoActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
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
        //cancel all pending register/login/addUser tasks
        VolleyRequest.getInstance(this.getApplication()).getRequestQueue().cancelAll(CANCEL_TAG);
    }
}
