package com.picspy.views;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.picspy.firstapp.R;
import com.picspy.utils.AppConstants;
import com.picspy.utils.ImageDownloader;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by Justin12 on 1/5/2016.
 */
public class ImageViewer extends Activity {

    ImageView pic;
    Matrix matrix;
    String imgPath;
    ArrayList<float[]> selection;
    int width, height;
    int time;
    int guessesRemaining;
    String hint;
    TextView timer;
    TextView hintText;
    TextView swipeText;
    TextView guessesText;
    ImageView incorrectImg;
    RelativeLayout xMarkLayout;
    RelativeLayout selectionLayout;
    DrawingView mDrawingView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_viewer_layout);

        // TODO: retrieve image, hint, selection, time, number of guesses from server
        Intent intent = getIntent();
        imgPath = intent.getStringExtra(CameraActivity.IMAGE_PATH);
        selection =  (ArrayList<float[]>) intent.getSerializableExtra(CameraActivity.SELECTION);
        guessesRemaining = 5;
        time = 20;
        hint = "This will be the hint that was associated with this challenge.";

        // first display the hint initially on its own
        final LinearLayout initialHintLayout = (LinearLayout) findViewById(R.id.initial_hint_layout);
        ((TextView) initialHintLayout.getChildAt(1)).setText(hint);

        pic = (ImageView) findViewById(R.id.image_view);
        hintText = (TextView) findViewById(R.id.hint_provided);
        timer = (TextView) findViewById(R.id.timer);
        swipeText = (TextView) findViewById(R.id.swipe_text);
        guessesText = (TextView) findViewById(R.id.guesses_remaining);
        guessesText.setText("Guesses left: " + guessesRemaining);

        incorrectImg = (ImageView) findViewById(R.id.x_mark);
        xMarkLayout = (RelativeLayout) findViewById(R.id.x_mark_holder);
        selectionLayout = (RelativeLayout) findViewById(R.id.selection_layout);

        hintText.setText(hint);

        // Start a timer to display solely the hint for 5 seconds
        new CountDownTimer(5000, 5000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                // Now that the initial timer is finished, hide the initial hint view and
                // display the image and guessing timer.
                initialHintLayout.setVisibility(View.GONE);
                timer.setVisibility(View.VISIBLE);
                pic.setVisibility(View.VISIBLE);
                swipeText.setVisibility(View.VISIBLE);
                guessesText.setVisibility(View.VISIBLE);
                width = pic.getWidth();
                height = pic.getHeight();
                matrix = new Matrix();

                // TODO: Ensure that this rotation value applies for all devices
                //set image rotation value to 90 degrees in matrix.
                matrix.postRotate(90);

                //loadBitmap(imgPath, pic);

                String url = AppConstants.DSP_URL_2 + "files/check.png?api_key=" + AppConstants.PICSPY_API_KEY;

                // Retrieves an image specified by the URL, displays it in the UI.
                ImageRequest request = new ImageRequest(url,
                        new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap bitmap) {
                                pic.setImageBitmap(bitmap);
                            }
                        }, 0, 0, null,
                        new Response.ErrorListener() {
                            public void onErrorResponse(VolleyError error) {
                                // TODO: what should happen if an error occurred
                                pic.setImageResource(R.drawable.x_mark);
                            }
                        });
                // Access the RequestQueue through the ImageDownloader singleton class.
                ImageDownloader.getInstance(getApplicationContext()).addToRequestQueue(request);

                // Scale the selection based on the imageview's size on the device
//                ExifInterface exif = null;
//                try {
//                    exif = new ExifInterface(imgPath);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                int widthTrue = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, -1);
//                int heightTrue = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, -1);
//                float scalingX = (float) width / widthTrue;
//                float scalingY = (float) height / heightTrue;
//                selection = applyScalingToSelection(selection, scalingX, scalingY);

                // Create the drawingView which will draw the selection after guessing is done
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
                                exitGuessing();

                            } else {
                                Log.d("click", "Wrong");
                                guessesText.setText("Guesses left: " + guessesRemaining);
                                int xMarkHeight = incorrectImg.getHeight();
                                int xMarkWidth = incorrectImg.getWidth();
                                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(xMarkWidth, xMarkHeight);
                                params.leftMargin = ((int) x) - xMarkWidth/2;
                                params.topMargin = ((int) y) - xMarkHeight/2;
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
                                    public void onAnimationStart(Animation animation) { }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        // once the animation is done, hide the x mark
                                        xMarkLayout.setVisibility(View.INVISIBLE);
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) { }
                                });
                                if (guessesRemaining == 0) {
                                    // end the guessing as the user was wrong
                                    Toast toast = Toast.makeText(getApplicationContext(), "You didn't guess right :(", Toast.LENGTH_SHORT);
                                    toast.show();
                                    exitGuessing();
                                }
                            }
                        }
                        return true;
                    }
                });

                selectionLayout.addView(mDrawingView);

                new CountDownTimer(time*1000, 1000) {

                    public void onTick(long millisUntilFinished) {
                        timer.setText("Time left: " + millisUntilFinished / 1000);
                    }

                    public void onFinish() {
                        timer.setText("Time's up!");
                        exitGuessing();
                    }
                }.start();
            }
        }.start();

    }

    // Helper method to pause 3 seconds then open the challengesActivity since the guessing has finished
    private void exitGuessing() {
        final Intent intent = new Intent(getApplicationContext(), ChallengesActivity.class);
        new CountDownTimer(3000, 3000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                startActivity(intent);
            }
        }.start();
    }

    /* Notes about AsyncTask: (http://developer.android.com/reference/android/os/AsyncTask.html)
     * Three generic types are: Params, Progress, Result
     * 4 steps in order are: onPreExecute, doInBackground, onProgressUpdate, onPostExecute
     */
    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;

        public BitmapWorkerTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {

            return decodeSampledBitmapFromFile(params[0], width, height);
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    public void loadBitmap(String filepath, ImageView imageView) {
        BitmapWorkerTask task = new BitmapWorkerTask(imageView);
        task.execute(filepath);
    }

    public Bitmap decodeSampledBitmapFromFile(String filepath, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filepath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap bmImg = BitmapFactory.decodeFile(filepath, options);
        return Bitmap.createBitmap(bmImg, 0, 0, bmImg.getWidth(), bmImg.getHeight(), matrix, true);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    // Helper method which checks if a given (x,y) coordinate as a float[] is inside the list of
    // coordinates making up the given selection.
    private boolean isInsideSelection(float[] click, ArrayList<float[]> selection) {
        int counter = 0;
        float xinters;
        float[] p1,p2;
        int numPoints = selection.size();

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

        if (counter % 2 == 0)
            return false;
        else
            return true;
    }

    // A helper method which applies the x and y scaling factors to each coordinate pair in the
    // selection in order to allow portability across different device screen sizes.
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

}
