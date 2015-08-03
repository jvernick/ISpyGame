package com.picspy;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
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

import com.picspy.firstapp.R;
import com.picspy.models.FriendRecord;
import com.picspy.utils.AppConstants;
import com.picspy.utils.PrefUtil;
import com.picspy.views.Fragments.FriendsFragment;

import java.util.regex.Pattern;


public class FriendInfoActivity extends ActionBarActivity implements SurfaceHolder.Callback {

    private static final String TAG = "FriendsInfoActivity";
    private TextView sent_won, sent_lost, received_won, received_lost;
    private SurfaceView frame1;
    private ProgressBar spinner;
    private int friend_id;
    private int topPercentage = 50, bottomPercentage = 50;  //default percentage values
    private boolean requestSuccessful = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String friendUsername = intent.getStringExtra(FriendsFragment.FRIEND_USERNAME);
        friend_id = intent.getIntExtra(FriendsFragment.FRIEND_ID, -1);
        new getStats(friend_id).execute();
        setContentView(R.layout.activity_friend_info);
        spinner = (ProgressBar)findViewById(R.id.myProgressBar);
        //TODO Set color to theme color
        spinner.getIndeterminateDrawable().setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN);

        sent_won = (TextView) findViewById(R.id.sent_won);
        sent_lost = (TextView) findViewById(R.id.sent_lost);
        received_won = (TextView) findViewById(R.id.recieved_won);
        received_lost = (TextView) findViewById(R.id.recieved_lost);

        frame1 = (SurfaceView) findViewById(R.id.surfaceView);
        frame1.getHolder().addCallback(this);

        //TODO make string always have length 3. lines below are for testing
        sent_lost.setText(100 + "");
        sent_won.setText("" + 0 + "");
        received_won.setText(700 + "");
        //received_lost.setText(" " + 0 + " ");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        TextView title = (TextView) toolbar.findViewById(R.id.toolbar_title);
        // TODO Should local db be queried here or in parent activity
        title.setText(friendUsername);
        setSupportActionBar(toolbar);                   // Setting toolbar as the ActionBar
        //TODO Verify contrast on back button and possibly change
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_back);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
        if (!requestSuccessful) { //Do nothing if web request was unsuccessful
            return false;
        }
        //noinspection SimplifiableIfStatement
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

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (requestSuccessful) { //draw image only after data is gotten from server
            tryDrawing(surfaceHolder, topPercentage, bottomPercentage);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        if (requestSuccessful) {
            tryDrawing(surfaceHolder, topPercentage, bottomPercentage);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    private void tryDrawing(SurfaceHolder surfaceHolder, int topPercent, int bottomPercent) {
        Canvas canvas = surfaceHolder.lockCanvas();
        if (canvas == null) {
            Log.e(TAG, "Cannot draw onto the canvas as it's null");
        } else {
            drawRect(canvas, surfaceHolder.getSurfaceFrame(), topPercent, bottomPercent);
            surfaceHolder.unlockCanvasAndPost(canvas);
        }

    }

    private void drawRect(final Canvas canvas, Rect canvasRect, int topPercent, int bottomPercent) {
        //Rect rect = new RectF(frame1.getLeft(), frame1.getTop(), frame1.getRight(), frame1.getBottom());
        int pad = 1;

        Rect temp = new Rect(pad, pad,canvasRect.right - pad, canvasRect.bottom/2 - 1 - pad); //top rectangle
        RectF rect1 = rectToSquare(temp, 2);
        temp = new Rect(pad,canvasRect.bottom/2 + 1 + pad,canvasRect.right - pad, canvasRect.bottom - pad); //bottom rect
        RectF rect2 = rectToSquare(temp, 2);
        Paint paint = new Paint();

        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        //paint.setAntiAlias(true);
        //TODO change color so that the background is indistinguishable
        //idealy the same color as backgroud
        //canvas.drawColor(Color.rgb(211,211,211));
        canvas.drawColor(Color.LTGRAY);

        paint.setColor(Color.RED);
        canvas.drawArc(rect1, 0, 360, true, paint);
        canvas.drawArc(rect2, 0, 360, true, paint);
        paint.setColor(Color.GREEN);
        canvas.drawArc(rect1, 0, 360 * topPercent / 100, true, paint);
        canvas.drawArc(rect2, 0, 360 * bottomPercent / 100, true, paint);
    }

    /**
     * Given a rectangle, returns a square within the rectangle with the specified padding
     * @param rect Original rect to modify
     * @param pad padding on all sides of returned square
     * @return Padded square withing the rectangle
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

    private void setStats(FriendRecord record) {
        int total1 = (record.getFriend_1_won() + record.getFriend_1_lost());
        int total2 = (record.getFriend_2_won() + record.getFriend_2_lost());
        total1 = total1==0? 1: total1;
        total2 = total2==0? 1: total2;

        if (friend_id < PrefUtil.getInt(getApplicationContext(), AppConstants.USER_ID)) {
            sent_won.setText(String.valueOf(record.getFriend_1_won()));
            sent_lost.setText(String.valueOf(record.getFriend_1_lost()));
            received_won.setText(String.valueOf(record.getFriend_2_won()));
            received_lost.setText(String.valueOf(record.getFriend_2_lost()));
            topPercentage = 100 *  record.getFriend_1_won()
                    / total1;
            bottomPercentage = 100 *  record.getFriend_2_won()
                    / total2;
        } else {
            sent_won.setText(String.valueOf(record.getFriend_2_won()));
            sent_lost.setText(String.valueOf(record.getFriend_2_lost()));
            received_won.setText(String.valueOf(record.getFriend_1_won()));
            received_lost.setText(String.valueOf(record.getFriend_1_lost()));
            bottomPercentage = 100 *  record.getFriend_1_won()
                    / total1;
            topPercentage = 100 *  record.getFriend_2_won()
                    / total2;
        }

        //Set values to default(50-50) if zero
        if (topPercentage == 0) {
            topPercentage = 50;
        }
        if (bottomPercentage == 0) {
            bottomPercentage = 50;
        }

        requestSuccessful = true;
        View view = findViewById(R.id.page);
        view.setVisibility(View.VISIBLE);
        tryDrawing(frame1.getHolder(), topPercentage, bottomPercentage);
        //TODO set user stats
    }

    private class getStats extends AsyncTask<Void, String, FriendRecord> {
        final int friend_id;
        public getStats(int friend_id) {
            super();
            this.friend_id = friend_id;
        }

        @Override
        protected void onPreExecute() {
           // spinner = (ProgressBar)findViewById(R.id.progressBar);
            //TODO Spinner not neccessary if connecting to server is very fast. Test.
            spinner.setVisibility(View.VISIBLE);
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
                setStats(result);
            } else { // some error show dialog
                Toast.makeText(FriendInfoActivity.this, "Network error",Toast.LENGTH_SHORT).show();
            }
        }
    }

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
                Toast.makeText(FriendInfoActivity.this, "Friend successfully removed",Toast.LENGTH_SHORT).show();
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
