package com.picspy;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.TextView;
import android.widget.Toast;

import com.picspy.firstapp.R;


public class FriendInfoActivity extends ActionBarActivity implements SurfaceHolder.Callback {

    private static final String TAG = "FriendsInfoActivity";
    SurfaceView frame1;
    TextView sent_won, sent_lost, recieved_won, getReceived_lost;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_info);

        frame1 = (SurfaceView) findViewById(R.id.surfaceView);
        frame1.getHolder().addCallback(this);

        sent_won = (TextView) findViewById(R.id.sent_won);
        sent_lost = (TextView) findViewById(R.id.sent_lost);
        recieved_won = (TextView) findViewById(R.id.recieved_won);
        getReceived_lost = (TextView) findViewById(R.id.recieved_lost);

        //TODO make string always have length 3
        sent_lost.setText(100 + "");
        sent_won.setText(" " + 0 + " ");
        recieved_won.setText(700 + "");
        //getRecieved_lost.setText(" " + 0 + " ");
        /*
        toolbar = (Toolbar) findViewById(R.id.toolbar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);                   // Setting toolbar as the ActionBar
       */
      /*
        ActionBar actionBar = getActionBar();
        actionBar.setSubtitle("mytest");
        actionBar.setTitle("vogella.com");*/
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(FriendInfoActivity.this);
            alertDialog.setTitle("Remove Friend")
                    .setMessage("Are you sure you want to remove this" + " user?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //TODO delete friend

                            FriendsTableRequests request = new FriendsTableRequests(getApplicationContext());
                            onBackPressed();
                            DeleteFriendTask  deleteFriendTask = new DeleteFriendTask(3);
                            deleteFriendTask.execute();
                            Toast.makeText(FriendInfoActivity.this, "Friend removed",Toast.LENGTH_SHORT).show();
                        }
                    }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
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
        Toast.makeText(FriendInfoActivity.this, "Game started",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        tryDrawing(surfaceHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        tryDrawing(surfaceHolder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    public void tryDrawing(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "Trying to draw...");

        Canvas canvas = surfaceHolder.lockCanvas();
        if (canvas == null) {
            Log.e(TAG, "Cannot draw onto the canvas as it's null");
        } else {
            drawRect(canvas, surfaceHolder.getSurfaceFrame());
            surfaceHolder.unlockCanvasAndPost(canvas);
        }

    }

    private void drawRect(final Canvas canvas, Rect canvasRect) {
        //Rect rect = new RectF(frame1.getLeft(), frame1.getTop(), frame1.getRight(), frame1.getBottom());
        int pad = 1;
        int width1 = canvasRect.right;
        int height1 = canvasRect.bottom/2;
        if (width1 - height1 < 0) {

        }
        Log.d(TAG,width1 + " " + canvasRect.toString());
        Rect temp = new Rect(0 + pad,0 + pad,canvasRect.right - pad, canvasRect.bottom/2 - 1 - pad); //top rectangle
        RectF rect1 = rectToSquare(temp, 2);
        temp = new Rect(0,canvasRect.bottom/2 + 1,canvasRect.right, canvasRect.bottom); //bottom rect
        RectF rect2 = rectToSquare(temp, 2);
        Paint paint = new Paint();
        int percentage1 = 85;
        int percentage2 = 15;

        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setAntiAlias(true);
        //TODO change color so that the background is indistinguishable
        canvas.drawColor(Color.LTGRAY);

        paint.setColor(Color.RED);
        canvas.drawArc(rect1, 0, 360, true, paint);
        canvas.drawArc(rect2, 0, 360, true, paint);
        paint.setColor(Color.GREEN);
        canvas.drawArc(rect1, 0, 360 * percentage1 / 100, true, paint);
        canvas.drawArc(rect2, 0, 360 * percentage2 / 100, true, paint);
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
            top = top + pad + (height - width) / 2;;
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

    class DeleteFriendTask extends AsyncTask<Void, String, String> {
        int friend_id;
        public DeleteFriendTask(int friend_id) {
            super();
            this.friend_id = friend_id;
        }

        @Override
        protected void onPreExecute() {
            //TODO add spinner
        }

        @Override
        protected String doInBackground(Void... params) {
            FriendsTableRequests request = new FriendsTableRequests(getApplicationContext());
            String result = request.removeFriend(friend_id);
            if (result != null && result.equals("SUCCESS")) { //TODO String contains error message on error
                return "SUCCESS";
            } else {
                Log.d(TAG, result.toString());
                return "FAILED";
            }
        }
        @Override
        protected void onPostExecute(String records) {

            if (records.equals("Success")){ // success
                Log.d(TAG,"Success");
            } else{ // some error show dialog
                Log.d(TAG, records);
            }
        }
    }
}
