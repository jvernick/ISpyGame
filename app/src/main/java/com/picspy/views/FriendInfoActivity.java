package com.picspy.views;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.picspy.FriendsTableRequests;
import com.picspy.adapters.DatabaseHandler;
import com.picspy.firstapp.R;
import com.picspy.models.Friend;
import com.picspy.models.FriendRecord;
import com.picspy.utils.AppConstants;
import com.picspy.utils.PrefUtil;

import java.util.regex.Pattern;

/**
 * Activity that displays friend information
 * When starting activity, pass the following informtion to the intent:
 *      FRIEND_ID && FRIEND_USERNAME : if this is for a friend
 *      FRIEND_REQUEST : if this for a friend request
 *      Nothing : if this is for self.
 */
public class FriendInfoActivity extends ActionBarActivity implements SurfaceHolder.Callback {
    public final static String FRIEND_USERNAME = "com.picspy.USERNAME";
    public final static String FRIEND_ID = "com.picspy.FRIEND_ID";
    public final static String FRIEND_REQUEST = "com.picspy.FRIEND_REQUEST";
    private static final String TAG = "FriendsInfoActivity";
    private TextView sent_won, sent_lost, received_won, received_lost;
    private TextView total_won, total_lost, leaderboard, toolbarTitle, stats_title;
    private SurfaceView frame1;
    private ProgressBar spinner;
    private int friend_id;
    private int topPercentage = 50, bottomPercentage = 50;  //default percentage values
    private boolean requestSuccessful = false;
    private boolean forFriend = false;
    private boolean forFriendRequest = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        friend_id = intent.getIntExtra(FRIEND_ID, -1);
        //TODO get username form intent
        new getStats(friend_id).execute();
        setContentView(R.layout.activity_friend_info);
        spinner = (ProgressBar)findViewById(R.id.myProgressBar);
        //TODO Set color to theme color
       // spinner.getIndeterminateDrawable().setColorFilter(R.color.accent, PorterDuff.Mode.SRC_IN);

        initializeViews();

        Toolbar toolbar = (Toolbar) findViewById(R.id.friend_info_toolbar);

        toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        stats_title = (TextView) findViewById(R.id.stats_title);
        // TODO Should local db be queried here or in parent activity

        // Setting toolbar as the ActionBar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
    }

    private void initializeViews() {
        sent_won = (TextView) findViewById(R.id.sent_won);
        sent_lost = (TextView) findViewById(R.id.sent_lost);
        received_won = (TextView) findViewById(R.id.recieved_won);
        received_lost = (TextView) findViewById(R.id.recieved_lost);
        total_won = (TextView) findViewById(R.id.total_won);
        total_lost = (TextView) findViewById(R.id.total_lost);
        leaderboard = (TextView) findViewById(R.id.leaderboard);

        if (forFriend) {
            frame1 = (SurfaceView) findViewById(R.id.surfaceView);
            frame1.getHolder().addCallback(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_friend_info, menu);
        return true;
    }

    @Override
    // menu dialog for deleting friends
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (!requestSuccessful) { //Do nothing if web request was unsuccessful
            return false;
        }

        if (id == R.id.action_settings) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(FriendInfoActivity.this);
            alertDialog.setTitle("Remove Friend")
                    .setMessage("Are you sure you want to remove this" + " user?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //TODO ensure that on return to parent, friends list is reloaded
                            //one way will be to return boolean to parent or always refresh
                            DeleteFriendTask  deleteFriendTask = new DeleteFriendTask(friend_id);
                            deleteFriendTask.execute();
                        }
                    }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert) //TODO change alert color to red?
                    .show();
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

    @Override //from implenting surface view
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (requestSuccessful) { //draw image only after data is gotten from server
            tryDrawing(surfaceHolder, topPercentage, bottomPercentage);
        }
    }

    @Override //from implenting surface view
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        if (requestSuccessful) {
            tryDrawing(surfaceHolder, topPercentage, bottomPercentage);
        }
    }

    @Override //from implenting surface view
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    /**
     * Attempt drawing on surface
     * @param surfaceHolder Surface to use as canvas
     * @param topPercent percentage to fill in the top floating_circle
     * @param bottomPercent percentage to fill in the bottom  floating_circle
     */
    private void tryDrawing(SurfaceHolder surfaceHolder, int topPercent, int bottomPercent) {
        Canvas canvas = surfaceHolder.lockCanvas();
        if (canvas == null) {
            Log.e(TAG, "Cannot draw onto the canvas as it's null");
        } else {
            drawRect(canvas, surfaceHolder.getSurfaceFrame(), topPercent, bottomPercent);
            surfaceHolder.unlockCanvasAndPost(canvas);
        }

    }

    /**
     * Draws the top and bottom rectangle for each individual floating_circle
     * @param canvas Canvas for drawing
     * @param canvasRect Total rectangle representing canvas size
     * @param topPercent percentage to fill in the top floating_circle
     * @param bottomPercent percentage to fill in the bottom  floating_circle
     */
    private void drawRect(final Canvas canvas, Rect canvasRect, int topPercent, int bottomPercent) {
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
        //idealy the same color as backgroud
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

    private void setUserStats(FriendRecord record) {
        //set values depending on who is friend_1 or friend_2
        if (friend_id < PrefUtil.getInt(getApplicationContext(), AppConstants.USER_ID)) {
            stats_title.setText(record.getUsers_by_friend_1().getUsername() + "\' all time");
            toolbarTitle.setText(record.getUsers_by_friend_1().getUsername());
            total_won.setText(String.valueOf(record.getUsers_by_friend_1().getTotal_won()));
            total_lost.setText(String.valueOf(record.getUsers_by_friend_1().getTotal_lost()));
            leaderboard.setText(String.valueOf(record.getUsers_by_friend_1().getLeaderboard()));
        } else {
            stats_title.setText(record.getUsers_by_friend_2().getUsername() + "\' all time");
            toolbarTitle.setText(record.getUsers_by_friend_2().getUsername());
            total_won.setText(String.valueOf(record.getUsers_by_friend_2().getTotal_won()));
            total_lost.setText(String.valueOf(record.getUsers_by_friend_2().getTotal_lost()));
            leaderboard.setText(String.valueOf(record.getUsers_by_friend_2().getLeaderboard()));
        }
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

        requestSuccessful = true;
        View view = findViewById(R.id.page);
        view.setVisibility(View.VISIBLE);
        tryDrawing(frame1.getHolder(), topPercentage, bottomPercentage);
        //TODO set user stats
    }

    /**
     * Class to get stats from database
     */
    private class getStats extends AsyncTask<Void, String, FriendRecord> {
        final int friend_id;

        public getStats(int friend_id) {
            super();
            this.friend_id = friend_id;
        }

        @Override
        protected void onPreExecute() {
            //TODO Spinner not neccessary if connecting to server is very fast. Test.
            if (spinner != null) {
                spinner.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected FriendRecord doInBackground(Void... params) {
            FriendsTableRequests request = new FriendsTableRequests(getApplicationContext());
            return request.getStats(friend_id);
        }

        @Override
        protected void onPostExecute(FriendRecord result) {
            spinner.setVisibility(View.GONE);
            if (result != null){
                setFriendStats(result);
                setUserStats(result);
            } else { // some error, show dialog
                Toast.makeText(FriendInfoActivity.this, "Network error",Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Class to delete friend from database
     */
    private class DeleteFriendTask extends AsyncTask<Void, String, String> {
        final int friend_id;
        public DeleteFriendTask(int friend_id) {
            super();
            this.friend_id = friend_id;
        }

        @Override
        protected String doInBackground(Void... params) {
            FriendsTableRequests request = new FriendsTableRequests(getApplicationContext());
            String result = request.removeFriend(friend_id);
            if (result != null && result.equals("SUCCESS")) { //TODO String contains error message on error
                DatabaseHandler dbHandler = DatabaseHandler.getInstance((getApplicationContext()));
                dbHandler.deleteFriend(new Friend(friend_id, null));
                return "SUCCESS";
            } else {
                Pattern p1 = Pattern.compile(".*[cC]onnection.*[rR]efused.*", Pattern.DOTALL);
                Pattern p2 = Pattern.compile(".*timed out", Pattern.DOTALL);
                if (result != null &&  p1.matcher(result).matches()) {
                    return "No Connection";
                } else if (result != null && p2.matcher(result).matches()) { //Should never timeout
                   return "Timed out";
                }
                return "FAILED";
            }
        }
        @Override
        protected void onPostExecute(String result) {
            if (result.equals("SUCCESS")){
                onBackPressed();
                Toast.makeText(FriendInfoActivity.this, "Friend successfully removed",
                        Toast.LENGTH_SHORT).show();
            } else { // some error show dialog
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(FriendInfoActivity.this);
                //TODO modify error presentation format and possibly the error message
                alertDialog.setTitle("Error").setMessage(result).setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                alertDialog.show();
            }
        }
    }
}
