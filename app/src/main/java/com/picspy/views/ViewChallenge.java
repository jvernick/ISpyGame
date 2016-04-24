package com.picspy.views;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.picspy.firstapp.R;
import com.picspy.models.Game;
import com.picspy.utils.AppConstants;
import com.picspy.utils.GameImageRequest;
import com.picspy.utils.ImageDownloader;
import com.picspy.utils.VolleyRequest;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Justin12 on 1/5/2016.
 */
public class ViewChallenge extends Activity {
    private static final String TAG = "ViewChallenge";
    private static final Object CANCEL_TAG = "cancel_game_request";
    private static final int HINT_DELAY = 5000;
    private static final long EXIT_DELAY = 3000;
    private ImageView pic;
    private Matrix matrix;
    private ArrayList<float[]> selection;
    private int canvasWidth, canvasHeight;
    private int time;
    private int guessesRemaining;
    private String hint;
    private TextView timerText;
    private TextView hintText;
    private TextView swipeText;
    private TextView guessesText;
    private ImageView incorrectImg;
    private RelativeLayout xMarkLayout;
    private RelativeLayout selectionLayout;
    private DrawingView mDrawingView;
    private DrawerLayout mDrawerLayout;
    private LinearLayout initialHintLayout;
    boolean gameSetup;
    boolean imageDownloaded;
    Animation animHintLeft;
    int trueWidth;
    int trueHeight;
    Game game;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_challenge);

        gameSetup = false;
        imageDownloaded = false;

        Intent intent = getIntent();
        game = intent.getParcelableExtra(ChallengesActivity.GAME_EXTRA);
        hint  = game.getHint();
        time = game.getTime();
        guessesRemaining = game.getGuess();
        selection = getSelection(game.getSelection());
        String fileName = game.getPictureName();

        // first display the hint initially on its own
        initialHintLayout = (LinearLayout) findViewById(R.id.initial_hint_layout);
        ((TextView) initialHintLayout.getChildAt(1)).setText(hint);

        pic = (ImageView) findViewById(R.id.image_view);
        hintText = (TextView) findViewById(R.id.hint_provided);
        timerText = (TextView) findViewById(R.id.timer);
        swipeText = (TextView) findViewById(R.id.swipe_text);
        guessesText = (TextView) findViewById(R.id.guesses_remaining);

        guessesText.setText("Guesses left: " + guessesRemaining);
        timerText.setText("ime left: " + time);
        hintText.setText(hint);

        incorrectImg = (ImageView) findViewById(R.id.x_mark);
        xMarkLayout = (RelativeLayout) findViewById(R.id.x_mark_holder);
        selectionLayout = (RelativeLayout) findViewById(R.id.selection_layout);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        // disable drawer at start
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);


        // download image
        downloadImage(fileName);
        // TODo after removing unneeded code, move to inside image download
        matrix = new Matrix();
        // TODO: Ensure that this rotation value applies for all devices
        //set image rotation value to 90 degrees in matrix.
        matrix.postRotate(90);


        animHintLeft = AnimationUtils.loadAnimation(this, R.anim.slide_hint_left);

        // Start a timerText to display solely the hint for 5 seconds
        new CountDownTimer(HINT_DELAY, HINT_DELAY) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                // Create the drawingView which will draw the selection after guessing is done
                setupDrawingView();
                selectionLayout.addView(mDrawingView);

                gameSetup = true;
                if (imageDownloaded) {
                    startGame();
                }
            }
        }.start();
    }

    /**
     * Hides hint layout show remaining views
     * @param initialHintLayout Hint layout to hide
     */
    private void updateVisibilities(LinearLayout initialHintLayout) {
        initialHintLayout.bringToFront();
        initialHintLayout.startAnimation(animHintLeft);
        initialHintLayout.setVisibility(View.GONE);
        timerText.setVisibility(View.VISIBLE);
        pic.setVisibility(View.VISIBLE);
        swipeText.setVisibility(View.VISIBLE);
        guessesText.setVisibility(View.VISIBLE);
        selectionLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Scale selection appropriately and starts the game (game timer)
     */
    private void startGame() {
        //Scale the selection based on the canvas size on the device
        canvasWidth = pic.getWidth();
        canvasHeight = pic.getHeight();
        float scalingX = (float) canvasWidth / trueWidth;
        float scalingY = (float) canvasHeight / trueHeight;
        Log.d(TAG, trueWidth + "x" + trueHeight + "  scaling: " + scalingX + "x" + scalingY);
        selection = applyScalingToSelection(selection, scalingX, scalingY);

        updateVisibilities(initialHintLayout);      // display the image and guessing timerText.
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);       //unlock drawer
        startGameTimer();                           // start the game timer
    }

    /**
     * Starts a timerText for the game that updates the time left
     */
    private void startGameTimer() {
        new CountDownTimer(time * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                timerText.setText("Time left: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                timerText.setText("Time's up!");
                exitGuessing(false, false);
            }
        }.start();
    }

    /**
     * Sets up the View for recording and processing user touches
     */
    private void setupDrawingView() {
        mDrawingView = new DrawingView(getApplicationContext());
        mDrawingView.setBrushColor(Color.RED);
        mDrawingView.setOnTouchListener(new View.OnTouchListener() {
            // Do not allow for touch events
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Only register their touch when lifting the finger off the screen
                if (event.getAction() == MotionEvent.ACTION_UP && guessesRemaining >= 1) {
                    guessesRemaining--;
                    float x = event.getX();
                    float y = event.getY();
                    float[] click = {x, y};
                    if (isInsideSelection(click, selection)) {
                        Log.d("click", "Right");
                        // Display the selection that the challenger drew
                        mDrawingView.drawSelection(selection);
                        Toast toast = Toast.makeText(getApplicationContext(), "You guessed right!", Toast.LENGTH_SHORT);
                        toast.show();
                        // open the challengesActivity once the guessing is done
                        exitGuessing(true, false);

                    } else {
                        Log.d("click", "Wrong");
                        guessesText.setText("Guesses left: " + guessesRemaining);
                        int xMarkHeight = incorrectImg.getHeight();
                        int xMarkWidth = incorrectImg.getWidth();
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(xMarkWidth, xMarkHeight);
                        params.leftMargin = ((int) x) - xMarkWidth / 2;
                        params.topMargin = ((int) y) - xMarkHeight / 2;
                        xMarkLayout.setLayoutParams(params);
                        xMarkLayout.setVisibility(View.VISIBLE);
                        // set up an animation to have the x flash twice
                        Animation mAnimation = new AlphaAnimation(1, 0);
                        // Change alpha from fully visible to invisible
                        mAnimation.setDuration(300); // duration - half a second
                        mAnimation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
                        mAnimation.setRepeatCount(2); // Repeat animation infinitely
                        // Reverse animation at the end so the layout will fade back in
                        mAnimation.setRepeatMode(Animation.REVERSE);
                        xMarkLayout.startAnimation(mAnimation);
                        mAnimation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                // once the animation is done, hide the x mark
                                xMarkLayout.setVisibility(View.INVISIBLE);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                        });
                        if (guessesRemaining == 0) {
                            // end the guessing as the user was wrong
                            Toast toast = Toast.makeText(getApplicationContext(), "You didn't guess right :(", Toast.LENGTH_SHORT);
                            toast.show();
                            exitGuessing(false, false);
                        }
                    }
                }
                return true;
            }
        });
    }

    /**
     * Downloads file with the specified filename from the server.
     * @param fileName filename to download
     */
    private void downloadImage(String fileName) {
        Response.Listener<Bitmap> responseListener = new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap bitmap) {
                Bitmap gameImage =  Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                        bitmap.getHeight(), matrix, true);
                pic.setImageBitmap(gameImage);
                trueHeight = gameImage.getHeight();
                trueWidth = gameImage.getWidth();
                imageDownloaded = true;
                if (gameSetup) {
                    startGame();
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error != null) {
                    //TODO handle error when file not found.
                    String err = (error.getMessage() == null)? "error message null": error.getMessage();
                    error.printStackTrace();
                    Log.d(TAG, err);
                    //Show toast only if there is no server connection on refresh
                    if (err.matches(AppConstants.CONNECTION_ERROR)) {
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate(R.layout.custom_toast,
                                (ViewGroup) findViewById(R.id.toast_layout_root));
                        Toast toast = new Toast(getApplicationContext());
                        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                        toast.setDuration(Toast.LENGTH_LONG);
                        toast.setView(layout);
                        toast.show();
                        exitGuessing(false, true);
                    }
                }
                // TODO: what should happen if an error occurred
                pic.setImageResource(R.drawable.x_mark);
            }
        };

        // Retrieves an image specified by the URL, displays it in the UI.
        GameImageRequest imageRequest = GameImageRequest.getImage(getApplicationContext(), fileName,
                responseListener, 0, 0, ImageView.ScaleType.CENTER, null, errorListener);

        if (imageRequest != null) imageRequest.setTag(CANCEL_TAG);
        // Access the RequestQueue through the ImageDownloader singleton class.
        ImageDownloader.getInstance(getApplicationContext()).addToRequestQueue(imageRequest);
    }

    /**
     * Returns a selection array from a selection string.
     * @param selection Selection  as string
     * @return Selection as array
     */
    private ArrayList<float[]> getSelection(String selection) {
        String regex = "\\[-?\\d*,-?\\d*\\]";
        Pattern p = Pattern.compile(regex, Pattern.DOTALL);
        Matcher m = p.matcher(selection);

        ArrayList<float[]> sel = new ArrayList<>();
        while (m.find()) {
            String temp = m.group();
            String[] tempStr = temp.replaceAll("\\[", "").replaceAll("\\]", "").split(",");
            float[] point = new float[2];
            point[0] = Integer.parseInt(tempStr[0]);
            point[1] = Integer.parseInt(tempStr[1]);

            sel.add(point);
        }

        Log.d(TAG, "compressed: " + CameraActivity.selectionToString(sel));
        //find cumulative sum
        for (int i = 1; i < sel.size(); i ++) {
            float[] curr = sel.get(i);
            float[] prev = sel.get(i - 1);

            sel.set(i, new float[]{prev[0] + curr[0], prev[1] + curr[1]});
        }
        Log.d(TAG, "unCompressed: " + CameraActivity.selectionToString(sel));

        return sel;
    }

    /**
     * Delay for exitDelay time and exit to previous activity (challengesActivity|topFragment)
     */
    private void exitGuessing(boolean gameResult, boolean error) {
        //TODO return to parent activity, not ChallengesActivity
        Intent intent = new Intent();
        intent.putExtra(ChallengesActivity.GAME_RESULT_VALUE, gameResult);
        intent.putExtra(ChallengesActivity.GAME_RESULT_ERROR, error);
        intent.putExtra(ChallengesActivity.GAME_RESULT_SENDER, game.getSenderId());
        intent.putExtra(ChallengesActivity.GAME_RESULT_CHALLENGE, game.getId());
        intent.putExtra(ChallengesActivity.GAME_RESULT_RECORD, game.getUserChallengeId());

        if (! error) {
            setResult(RESULT_OK, intent);
        } else {
            setResult(RESULT_CANCELED, intent);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, EXIT_DELAY);
    }

    // Helper method which checks if a given (x,y) coordinate as a float[] is inside the list of
    // coordinates making up the given selection.
    private boolean isInsideSelection(float[] click, ArrayList<float[]> selection) {
        int counter = 0;
        float xinters;
        float[] p1,p2;
        int numPoints = selection.size();

        //TODO check IndexOutOfBoundsException
        p1 = selection.get(0);
        for(int i = 1; i <= numPoints; i++) {
            p2 = selection.get(i % numPoints);
            if (click[1] > Math.min(p1[1],p2[1])) {
                if (click[1] <= Math.max(p1[1],p2[1])) {
                    if (click[0] <= Math.max(p1[0],p2[0])) {
                        if (p1[1] != p2[1]) {
                            xinters = (click[1]-p1[1])*(p2[0]-p1[0])/(p2[1]-p1[1])+p1[0];
                            if (p1[0] == p2[0] || click[0] <= xinters)
                                counter++;
                        }
                    }
                }
            }
            p1 = p2;
        }

        return counter % 2 != 0;
    }

    /**
     * Applies the x and y scaling factors to each coordinate pair in the selection
     * in order to allow portability across different device screen sizes.
     * @param selection Selection from server
     * @param scalingFactorX xScaling factor
     * @param scalingFactorY yScaling factor
     * @return scaled selection to match device
     */
    public static ArrayList<float[]> applyScalingToSelection(ArrayList<float[]> selection, float scalingFactorX, float scalingFactorY) {
        float[] temp;
        int size = selection.size();
        ArrayList<float[]> scaledResult = new ArrayList<>(size);

        for(int i = 0; i < size-1; i++) {
            float[] coord = new float[2];
            temp = selection.get(i);
            coord[0] = temp[0] * scalingFactorX;
            coord[1] = temp[1] * scalingFactorY;
            scaledResult.add(i, coord);
        }

        return scaledResult;
    }

    @Override
    protected void onStop() {
        super.onStop();
        //cancel pending login task
        VolleyRequest.getInstance(this.getApplication()).getRequestQueue().cancelAll(CANCEL_TAG);
    }
}
