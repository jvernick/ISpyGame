package com.picspy.views;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
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
import com.picspy.utils.AppConstants;
import com.picspy.utils.FriendsRequests;
import com.picspy.utils.PrefUtil;
import com.picspy.utils.VolleyRequest;

import java.util.ArrayList;

/**
 * Activity that displays friend information
 * TODo revise this documentation
 * When starting activity, pass the following information to the intent:
 *      FRIEND_ID && FRIEND_USERNAME : if this is for a friend
 *      FOR_FRIEND : if this for a friend request
 *      Nothing : if this is for self.
 */
public class FriendInfoActivity extends ActionBarActivity implements SurfaceHolder.Callback {
    public final static String FRIEND_ID = "com.picspy.FRIEND_ID";
    public final static String FOR_FRIEND = "com.picspy.FRIEND_REQUEST";

    private static final String TAG = "FriendsInfoActivity";
    private static final String CANCEL_TAG = "deleteFriend";

    public static final String USERNAME = "username";
    public static final String WON = "won";
    public static final String LOST = "lost";
    public static final String L_BOARD = "l_board";
    private TextView sent_won, sent_lost, received_won, received_lost;
    private TextView total_won, total_lost, leaderboard, stats_title;
    private SurfaceView frame1;
    private ProgressBar progressSpinner;
    private int friend_id;
    private String user_username;
    private int topPercentage = 50, bottomPercentage = 50;  //default percentage values
    private boolean requestSuccessful = false;
    private boolean forFriend;
    private boolean surfaceDrawn = false;
    private ArrayList<AlertDialog> alertDialogs = new ArrayList<>();
    private Menu menu;
    private static final int MENU_DELETE_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_info);
        Intent intent = getIntent();
        forFriend = intent.getBooleanExtra(FOR_FRIEND, false);
        initializeViews();

        progressSpinner = (ProgressBar)findViewById(R.id.myProgressBar);

        processIntent();
        if (forFriend) {
            Log.d(TAG, "getting stats");
            getStats(friend_id);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.friend_info_toolbar);
        // Setting toolbar as the ActionBar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_chevron_left);

        if (user_username != null) {
            stats_title.setText(user_username + "\' all time");
            getSupportActionBar().setTitle(user_username);
        }
    }

    /**
     * Gets information about the user to be displayed from the intent bundle
     */
    private void processIntent() {
        Intent intent = getIntent();
        if (forFriend) {
            friend_id = intent.getIntExtra(FRIEND_ID, -1);
            user_username = intent.getStringExtra(USERNAME);
        } else {
            setUserStats(intent.getStringExtra(USERNAME), intent.getIntExtra(WON, -1),
                    intent.getIntExtra(LOST, -1), intent.getIntExtra(L_BOARD, -1));
            user_username = intent.getStringExtra(USERNAME);
        }
    }

    /**
     * Initializes all the views in this activity and hides the friends start depending
     * on whether the user to be displayed is a friend
     */
    private void initializeViews() {
        sent_won = (TextView) findViewById(R.id.sent_won);
        sent_lost = (TextView) findViewById(R.id.sent_lost);
        received_won = (TextView) findViewById(R.id.recieved_won);
        received_lost = (TextView) findViewById(R.id.recieved_lost);
        total_won = (TextView) findViewById(R.id.total_won);
        total_lost = (TextView) findViewById(R.id.total_lost);
        leaderboard = (TextView) findViewById(R.id.leaderboard);
        stats_title = (TextView) findViewById(R.id.stats_title);

        if (forFriend) {
            frame1 = (SurfaceView) findViewById(R.id.surfaceView);
            frame1.getHolder().addCallback(this);
        } else {
            if (requestSuccessful) findViewById(R.id.joint_stats).setVisibility(View.GONE);
        }
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
                               deleteFriend(friend_id);
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
     * @param v view from button click
     */
    public void startGame(View v) {
        /*TODO Start activity to create new game
        Intent i = new Intent(MainActivity.this,SecondActivity.class);
                startActivity(i);
         */
        /*if (!requestSuccessful) { //Do nothing if web request was unsuccessful
            return;
        }*/
        Toast.makeText(FriendInfoActivity.this, "Game started", Toast.LENGTH_SHORT).show();
    }

    @Override //from implementing surface view
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (requestSuccessful && forFriend) { //draw image only after data is gotten from server
            surfaceDrawn = tryDrawing(surfaceHolder, topPercentage, bottomPercentage);
        }
    }

    @Override //from implementing surface view
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        if (requestSuccessful && forFriend) {
            surfaceDrawn = tryDrawing(surfaceHolder, topPercentage, bottomPercentage);
        }
    }

    @Override //from implementing surface view
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    }

    /**
     * Attempt drawing on surface
     * @param surfaceHolder Surface to use as canvas
     * @param topPercent percentage to fill in the top floating_circle
     * @param bottomPercent percentage to fill in the bottom  floating_circle
     */
    private boolean tryDrawing(SurfaceHolder surfaceHolder, int topPercent, int bottomPercent) {
        Canvas canvas = surfaceHolder.lockCanvas();
        if (canvas == null) {
            Log.e(TAG, "Cannot draw onto the canvas as it's null");
            return false;
        } else {
            if (drawRect(canvas, surfaceHolder.getSurfaceFrame(), topPercent, bottomPercent)) {
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
            return true;
        }

    }

    /**
     * Draws the top and bottom rectangle for each individual floating_circle
     * @param canvas Canvas for drawing
     * @param canvasRect Total rectangle representing canvas size
     * @param topPercent percentage to fill in the top floating_circle
     * @param bottomPercent percentage to fill in the bottom  floating_circle
     */
    private boolean drawRect(final Canvas canvas, Rect canvasRect, int topPercent, int bottomPercent) {
        //Rect rect = new RectF(frame1.getLeft(), frame1.getTop(), frame1.getRight(),
        // frame1.getBottom());
        int pad = 1;

        //top rectangle
        Rect temp = new Rect(pad, pad,canvasRect.right - pad, canvasRect.bottom/2 - 1 - pad);
        RectF rect1 = rectToSquare(temp, 2);
        //bottom rect
        temp = new Rect(pad,canvasRect.bottom/2 + 1 + pad,canvasRect.right - pad,
                canvasRect.bottom - pad);
        RectF rect2 = rectToSquare(temp, 2);
        Paint paint = new Paint();

        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        //paint.setAntiAlias(true);
        //TODO change color so that the background is indistinguishable
        //ideally the same color as background
        //canvas.drawColor(Color.rgb(211,211,211));
        canvas.drawColor(Color.LTGRAY);

        int win_color = getApplicationContext().getResources().getColor(R.color.primary);
        int loss_color = getApplicationContext().getResources().getColor(R.color.red_500);
        paint.setColor(loss_color);
        canvas.drawArc(rect1, 0, 360, true, paint);
        canvas.drawArc(rect2, 0, 360, true, paint);
        paint.setColor(win_color);
        canvas.drawArc(rect1, 0, 360 * topPercent / 100, true, paint);
        canvas.drawArc(rect2, 0, 360 * bottomPercent / 100, true, paint);
        return true;
    }

    /**
     * Given a rectangle, returns a square within the rectangle with the specified padding
     * @param rect Original rect to modify
     * @param pad padding on all sides of returned square
     * @return Square withing the rectangle with some padding
     */
    private RectF rectToSquare(Rect rect, int pad) {
        int left = rect.left, right = rect.right, top = rect.top, bottom = rect.bottom;
        int width = (right - left);
        int height = (bottom - top);

        if (width  < height ) {
            left = left + pad;
            top = top + pad + (height - width) / 2;
            right = right - pad;
            bottom = top + width - pad;
        } else {
            left = left + pad + (width - height) / 2;
            top = top + pad;
            right = left + height - pad;
            bottom = bottom - pad;
        }

        return new RectF(left, top, right, bottom);
    }


    /**
     * Sets the stats for the given User info without making server request
     * @param username User's username
     * @param won   Number of games won
     * @param lost  Number of games lost
     * @param l_board Number of games on leaderboard
     */
    private  void setUserStats( String username, int won, int lost, int l_board) {
        stats_title.setText(username);
        total_won.setText(String.valueOf(won));
        total_lost.setText(String.valueOf(lost));
        leaderboard.setText(String.valueOf(l_board));
        findViewById(R.id.page).setVisibility(View.VISIBLE);
        findViewById(R.id.user_stats).setVisibility(View.VISIBLE);
    }

    /**
     * Sets the stats for the given Friend
     * @param record Record containing information about the friend (games won, lost, leaderboard)
     */
    private void setUserStats(FriendRecord record) {
        //set values depending on who is friend_1 or friend_2
        if (friend_id < PrefUtil.getInt(getApplicationContext(), AppConstants.USER_ID)) {
            stats_title.setText(record.getUsers_by_friend_1().getUsername() + "\' all time");
            total_won.setText(String.valueOf(record.getUsers_by_friend_1().getTotal_won()));
            total_lost.setText(String.valueOf(record.getUsers_by_friend_1().getTotal_lost()));
            leaderboard.setText(String.valueOf(record.getUsers_by_friend_1().getLeaderboard()));
        } else {
            stats_title.setText(record.getUsers_by_friend_2().getUsername() + "\' all time");
            total_won.setText(String.valueOf(record.getUsers_by_friend_2().getTotal_won()));
            total_lost.setText(String.valueOf(record.getUsers_by_friend_2().getTotal_lost()));
            leaderboard.setText(String.valueOf(record.getUsers_by_friend_2().getLeaderboard()));
        }
        findViewById(R.id.user_stats).setVisibility(View.VISIBLE);
    }

    /**
     * Sets all the stat fields in the view
     * @param record friend record from database containing friend stats
     */
    private void setFriendStats(FriendRecord record) {
        int total1 = (record.getFriend_1_won() + record.getFriend_1_lost());
        int total2 = (record.getFriend_2_won() + record.getFriend_2_lost());
        //default values if total1 or total2 are 0
        topPercentage = 50;
        bottomPercentage = 50;

        //set values depending on who is friend_1 or friend_2
        if (friend_id < PrefUtil.getInt(getApplicationContext(), AppConstants.USER_ID)) {
            sent_won.setText(String.valueOf(record.getFriend_1_won()));
            sent_lost.setText(String.valueOf(record.getFriend_1_lost()));
            received_won.setText(String.valueOf(record.getFriend_2_won()));
            received_lost.setText(String.valueOf(record.getFriend_2_lost()));
            if (total1 != 0) {
                topPercentage = 100 * record.getFriend_1_won()
                        / total1;
            }
            if (total2 != 0){
                bottomPercentage = 100 * record.getFriend_2_won()
                        / total2;
            }
        } else {
            sent_won.setText(String.valueOf(record.getFriend_2_won()));
            sent_lost.setText(String.valueOf(record.getFriend_2_lost()));
            received_won.setText(String.valueOf(record.getFriend_1_won()));
            received_lost.setText(String.valueOf(record.getFriend_1_lost()));
            if (total1 != 0) {
                bottomPercentage = 100 * record.getFriend_1_won()
                        / total1;
            }
            if (total2 != 0) {
                topPercentage = 100 * record.getFriend_2_won()
                        / total2;
            }
        }

        findViewById(R.id.joint_stats).setVisibility(View.VISIBLE);
        if (!surfaceDrawn) tryDrawing(frame1.getHolder(), topPercentage, bottomPercentage);
    }

    /**
     * Gets a user's stats from the server
     * @param friend_id The user whose is to be obtained
     */
    private void getStats(int friend_id) {
        Response.Listener<FriendRecord> response = new Response.Listener<FriendRecord>() {
            @Override
            public void onResponse(FriendRecord response) {
                progressSpinner.setVisibility(View.GONE);
                if (response != null) {
                    Log.d(TAG, response.toString());
                    //add delete button to menu
                    if (forFriend) {
                        setFriendStats(response);
                        menu.add(Menu.NONE, MENU_DELETE_ID, Menu.NONE, R.string.friend_delete_menu)
                                .setIcon(R.drawable.ic_delete)
                                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                    }

                    findViewById(R.id.page).setVisibility(View.VISIBLE);
                    setUserStats(response);
                    requestSuccessful = true;
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressSpinner.setVisibility(View.GONE);
                if (error != null ) {
                    String err = (error.getMessage() == null)? "An error occurred": error.getMessage();
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
                        Toast.makeText(FriendInfoActivity.this, "An error occurred",Toast.LENGTH_SHORT).show();
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
     * @param friend_id friend to be deleted
     */
    private void deleteFriend(final int friend_id) {
        Response.Listener<FriendRecord> responseListener = new Response.Listener<FriendRecord>() {
            @Override
            public void onResponse(FriendRecord response) {
                if (response != null ) {
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
            public void onErrorResponse(VolleyError error) { progressSpinner.setVisibility(View.GONE);
                if (error != null ) {
                    String err = (error.getMessage() == null)? "An error occurred": error.getMessage();
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

        FriendsRequests deleteFriedRequest = FriendsRequests.removeFriend(this, friend_id, responseListener, errorListener);
        if (deleteFriedRequest != null) deleteFriedRequest.setTag(CANCEL_TAG);
        VolleyRequest.getInstance(getApplicationContext()).addToRequestQueue(deleteFriedRequest);
    }

    @Override
    protected void onStop() {
        super.onStop();
        for (AlertDialog d: alertDialogs) {
            if (d.isShowing()) d.dismiss();
        }
        //cancel all pending register/login/addUser tasks
        VolleyRequest.getInstance(this.getApplication()).getRequestQueue().cancelAll(CANCEL_TAG);
    }
}
