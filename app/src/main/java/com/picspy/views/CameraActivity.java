package com.picspy.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.picspy.firstapp.R;
import com.picspy.utils.ChallengesRequests.GAME_LABEL;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Justin12 on 6/28/2015.
 */

/**
 * The class which represents the custom camera. It has functionality such as auto-focusing,
 * switching between cameras, and changing flash modes. Once a picture is taken, the layout of this
 * activity changes to enable drawing.
 */
// TODO: Fix the focusing to focus in on the area touched
public class CameraActivity extends Activity implements Camera.AutoFocusCallback {

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static final String TAG = "CameraActivity";
    public static final String DRAWING_VIEW_TAG = "DRAWING_VIEW";
    public static final String FINISH_BUTTON_TAG = "FINISH_BUTTON";
    //TODO remove
    public static final String HINT = "HINT";
    public static final String GUESSES = "GUESSES";
    public static final String TIME = "TIME";
    public static final String LEADERBOARDS = "LEADERBOARDS";
    public static final String IMAGE_PATH = "IMAGE_PATH";
    public static final String SELECTION = "SELECTION";
    //
    public static final int CHALLENGE_INFO_REQUEST = 2;
    private Camera mCamera;
    private CameraPreview mPreview;
    private String flashMode = Camera.Parameters.FLASH_MODE_OFF;
    private boolean flashEnabled = false;
    // TODO: Will the orientation listener be needed (along with deviceOrientation)?
    private OrientationEventListener myOrientationEventListener;
    private int deviceOrientation;
    private FrameLayout myFrameLayout;
    private int focusAreaSize;
    private Matrix matrix;
    private ImageButton captureButton;
    private ImageButton flashButton;
    private ImageButton switchButton;
    private ImageButton undoButton;
    private ImageButton brushColorButton;
    private Button finishButton;
    private DrawingView mDrawingView;
    private RelativeLayout drawingPad;
    private boolean isPictureTaken;
    private static int[] colors = new int[6];
    private static int currColor;
    private File imageFile;
    private int friend_id;  //for storing friend id if passed in.

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            isPictureTaken = true;

            // Initialize the array of colors
            colors[0] = Color.RED;
            colors[1] = getResources().getColor(R.color.palette_orange);
            colors[2] = Color.YELLOW;
            colors[3] = Color.GREEN;
            colors[4] = Color.BLUE;
            colors[5] = getResources().getColor(R.color.palette_purple);
            // Now that a picture is taken, change the layout of the screen to enable drawing

            // get the parent layout for CameraActivity
            ViewGroup layout = (ViewGroup) findViewById(R.id.camera_activity_layout);

            // remove the buttons used for taking picture
            captureButton.setVisibility(View.GONE);
            flashButton.setVisibility(View.GONE);
            switchButton.setVisibility(View.GONE);

            // Inflate the layout defined in drawing_pad.xml
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            drawingPad = (RelativeLayout) inflater.inflate(R.layout.drawing_pad, null, false);
            // Instantiate the custom DrawingView which will allow for drawing on the screen
            mDrawingView = new DrawingView(getApplicationContext());
            // Distinguish the DrawingView from its relatives by setting a tag on it
            mDrawingView.setTag(DRAWING_VIEW_TAG);
            // add the DrawingView as the first child so all other views are overlayed on top
            drawingPad.addView(mDrawingView, 0);

            // Assign the undoButton an onClickListener
            undoButton = (ImageButton) drawingPad.findViewById(R.id.undo_button);
            undoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                      mDrawingView.undoDrawing();
                }
            });

            brushColorButton = (ImageButton) drawingPad.findViewById(R.id.brush_colors);
            brushColorButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currColor = (currColor+1) % colors.length;
                    int colorToUse = colors[currColor];
                    mDrawingView.setBrushColor(colorToUse);
                    // change the color of the palette button
                    ColorFilter filter = new LightingColorFilter(colorToUse,colorToUse);
                    brushColorButton.setColorFilter(filter);
                }
            });

            finishButton = (Button) drawingPad.findViewById(R.id.finish_button);
            finishButton.setTag(FINISH_BUTTON_TAG); // distinguish the finish button
            finishButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mDrawingView.isDoneDrawing() && imageFile.exists()) {
                        // TODO: do we want to call startActivity() instead?
                        // retrieve the coordinates of the selection the user drew
                        ArrayList<float[]> selection = mDrawingView.getSelection();
                        DisplayMetrics display = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(display);
                        // Get the width and height of the canvas that the user drew on
                        int width = mDrawingView.getWidth();
                        int height = mDrawingView.getHeight();

                        // create and launch an activity to prompt the user for info about the challenge
                        Bundle pictureOptionsBundle = new Bundle();
                        pictureOptionsBundle.putString(GAME_LABEL.FILE_NAME_PATH,
                                imageFile.getAbsolutePath());

                        ExifInterface exif = null;
                        try {
                            exif = new ExifInterface(imageFile.getAbsolutePath());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (exif != null) {
                            // Use the exif interface to retrieve the actual width and height of the image
                            // The width and length are actually the height and width, respectively
                            int imageWidth = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, -1);
                            int imageLength = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, -1);
                            float scalingY = (float) imageLength / height;
                            float scalingX = (float) imageWidth / width;
                            // apply the scaling to the selection so that it is normalized to the true image size
                            selection = ImageViewer.applyScalingToSelection(selection, scalingX, scalingY);

                            //For testing
                            testCompression(selection);

                            // put the selection array as a string as a string
                            pictureOptionsBundle.putString(GAME_LABEL.SELECTION, selectionToString2(compressSelection(selection)));
                            pictureOptionsBundle.putString(GAME_LABEL.FILE_NAME, imageFile.getName());


                            Log.d(TAG, "CameraID: " + mPreview.getCameraID());
                            //intent.putExtra("CameraID", mPreview.getCameraID());
                            // TODO: determine where to delete the image
                            // TODO: apply the selfie flip here, rather than in the imageviewer

                            Intent intent = new Intent(getApplicationContext(), SendChallenge.class);
                            intent.putExtra(SendChallenge.BDL_PICTURE_OPTIONS, pictureOptionsBundle);
                            //add friend id if any
                            intent.putExtra(SendChallenge.ARG_FRIEND_ID, friend_id);
                            startActivityForResult(intent, CHALLENGE_INFO_REQUEST);
                        }
                    }
                }
            });

            // add this new drawingPad layout as a child of the root layout for CameraActivity
            layout.addView(drawingPad);

            // save the image temporarily in the app's local directory
            imageFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);

            // TODO: the image should be deleted if the user does not continue on however
            OutputStream outputStream = null;
            try {
                // flip the image if it was in selfie mode
                if (mPreview.getCameraID() == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    Matrix matrix = new Matrix();
                    matrix.preScale(-1, 1);     // this flips the image across the origin
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    // convert the bitmap back to a byte array
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    data = stream.toByteArray();    // write the flipped bitmap to a byte array
                }
                outputStream = new BufferedOutputStream(new FileOutputStream(imageFile));
                outputStream.write(data);   // write the image to the outputStream
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // Always close the outputStream
                try {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private void testCompression(ArrayList<float[]> selection) {
        Log.d(TAG, "uncompressed");
        Log.d(TAG, "size: " + selection.size());
        Log.d(TAG, "string size: " + selectionToString(selection).length());
        Log.d(TAG, "compressed");

        Log.d(TAG, "size: " + compressSelection(selection).size());
        Log.d(TAG, "string size: " + selectionToString2(compressSelection(selection)).length());

        Log.d(TAG, "uncompressed: " + selectionToString(selection));
        Log.d(TAG, "compressed: " + selectionToString2(compressSelection(selection)));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == CHALLENGE_INFO_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                String hint = data.getStringExtra(HINT);
                // TODO: handle the data received
            }
        }
    }

    /**
     * Called when the camera finishes auto-focusing.
     * @param focused
     *      Boolean representing if the camera focused successfully
     * @param camera
     *      The camera object that performed the focusing
     */
    @Override
    public void onAutoFocus(boolean focused, Camera camera) {
        if (focused) {
            Log.d("FOCUS", String.format("Auto focus success=%s. Focus mode: '%s'. Focused on: %s",
                    focused,
                    camera.getParameters().getFocusMode(),
                    camera.getParameters().getFocusAreas().get(0).rect.toString()));
            mCamera.cancelAutoFocus();
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFocusAreas(null);

            List<String> supportedFocusModes = parameters.getSupportedFocusModes();

            if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }

            mCamera.setParameters(parameters);

        }
    }

    // Helper method that iterates through all of the supported picture sizes for the device
    private void viewPictureResolutions() {

        List<Camera.Size> supportedSizes;
        Camera.Parameters params = mCamera.getParameters();

        supportedSizes = params.getSupportedPictureSizes();

        for (Camera.Size sz : supportedSizes) {
            Log.d("CAMERA_SIZE", "supportedPictureSizes " + sz.width + "x" + sz.height);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        processIntent();
        matrix = new Matrix();

        myOrientationEventListener
                = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL){
            @Override
            public void onOrientationChanged(int arg0) {
                deviceOrientation = arg0;
            }};

        // Add a listener to the Capture button to depress the button and to take a picture when
        // it is released
        captureButton = (ImageButton) findViewById(R.id.button_capture);
        captureButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        captureButton.setSelected(true);
                        break;
                    case MotionEvent.ACTION_UP:
                        captureButton.setSelected(false);
                        // TODO: Is the below comment correct (are the dimensions guaranteed to be the same)?
                        // The dimensions (in pixels) of this screen and the next screen should be
                        // the same, so find a good picture size to use.
                        int screenWidth = myFrameLayout.getWidth();
                        int screenHeight = myFrameLayout.getHeight();
                        // TODO: Do testing on various devices and determine how picture size should be set
                        Camera.Size cameraSize = getLargestPictureSize(mCamera.getParameters());
                        // Set the size of the picture using the optimal dimensions as determined
                        // in the above method call.
                        setPictureSize(cameraSize.width, cameraSize.height);
                        // get an image from the camera
                        mCamera.takePicture(null, null, mPicture);
                        break;
                }
                return true;
            }
        });

        // Add a listener to the change flash button
        flashButton = (ImageButton) findViewById(R.id.button_change_flash);
        flashButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        changeFlash();
                        // Change the state of the flash button based on if flash is enabled or not
                        flashButton.setSelected(flashEnabled);
                    }
                }
        );

        // Add a listener to the switch camera button
        switchButton = (ImageButton) findViewById(R.id.button_camera_switch);
        switchButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Change the physical camera
                        mCamera = mPreview.changeCamera();
                        // Update the flashMode for the new camera
                        flashMode = mCamera.getParameters().getFlashMode();
                        // There are no flash modes available, so change the state of the flash button
                        if (flashMode == null) {
                            flashEnabled = false;
                            flashButton.setSelected(false);
                        }
                    }
                }
        );
    }

    /**
     * Gets and stores friend_id from intent if available
     */
    private void processIntent() {
        Intent intent = getIntent();
        friend_id = intent.getIntExtra(SendChallenge.ARG_FRIEND_ID, -1);
    }

    // Helper method to set the picture size for the next picture taken
    private void setPictureSize(int width, int height) {
        Camera.Parameters params = mCamera.getParameters();
        params.setPictureSize(width, height);
        mCamera.setParameters(params);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isPictureTaken) {
            releaseCamera();    // release the camera immediately on pause event
        }
        // TODO: should the camera be released if a picture is taken and the user moves on to the createchallenge screen?
    }

    // Helper method to release the camera resources when no longer using it
    private void releaseCamera(){
        if (mCamera != null){
            flashMode = mCamera.getParameters().getFlashMode(); // save the flash mode
            mCamera.release();  // release the camera for other applications
            mCamera = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (!isPictureTaken) {
            releaseCamera();
            // reset the static variable camera id to the back facing camera
            mPreview.setCameraID(Camera.CameraInfo.CAMERA_FACING_BACK);
            super.onBackPressed();
        } else {
            // restore the camera layout so a new picture can be taken
            ViewGroup layout = (ViewGroup) findViewById(R.id.camera_activity_layout);
            layout.removeView(drawingPad);
            drawingPad = null;
            mDrawingView = null;
            undoButton = null;
            finishButton = null;

            // remove the buttons used for taking picture
            captureButton.setVisibility(View.VISIBLE);
            flashButton.setVisibility(View.VISIBLE);
            switchButton.setVisibility(View.VISIBLE);

            isPictureTaken = false; // reset the boolean flag
            // restore the camera preview with a call to onResume
            onResume();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isPictureTaken) {
            myFrameLayout = (FrameLayout) findViewById(R.id.camera_preview);
            myFrameLayout.removeAllViews();
            Camera.Parameters params;
            // Create an instance of Camera
            if (mCamera == null) {
                // This is not the first time the CameraActivity has been opened so use the previous mPreview
                if (mPreview != null) {
                    // resume the previously opened camera upon waking
                    mCamera = getCameraInstance(mPreview.getCameraID());
                    params = mCamera.getParameters();
                    // retain the previous state of the flash if the camera had it
                    if (flashMode != null) {
                        params.setFlashMode(flashMode);
                        System.out.print(getSmallestPictureSize(params));
                    }
                } else {
                    // upon first starting this activity, the back camera is opened
                    mCamera = getCameraInstance();
                    params = mCamera.getParameters();
                }
            } else {
                // Get the parameters of the previous camera
                params = mCamera.getParameters();
            }

            // Note: getSupportedFocusModes always returns a list with at least one element
            List<String> focusModes = params.getSupportedFocusModes();
            // Set the focus mode if it has the continuous focus option
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
            mCamera.setParameters(params);
            // Create our Preview view and set it as the content of our activity.
            mPreview = new CameraPreview(this, mCamera, this);
            // Add the preview to the framelayout so it will be visible
            myFrameLayout.addView(mPreview);
        } else {
            // TODO: what to do if the user backs out of creating a challenge?
        }
    }

    /**
     * This method is called whenever the user taps the screen when this activity is open.
     */
    public boolean onTouchEvent(MotionEvent event){
        // TODO: Is the null check for mCamera ever necessary?
        // Only focus on the down press and not when the user releases their finger
        if(event.getAction() == MotionEvent.ACTION_DOWN && mCamera != null && mCamera.getParameters().getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)){
            // TODO: What should focusAreaSize be?
            focusAreaSize = myFrameLayout.getWidth() / 4;
            focusOnTouch(event);
        }

        return true;
    }

    // Method to focus the camera after the user presses the screen
    protected void focusOnTouch(MotionEvent event) {

        mCamera.cancelAutoFocus();  // Cancel the previous focusing of the camera
        Rect focusRect = calculateTapArea(event.getX(), event.getY(), 1f);
        Rect meteringRect = calculateTapArea(event.getX(), event.getY(), 1.5f);

        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        List<Camera.Area> focusAreaList = new ArrayList<Camera.Area>();
        focusAreaList.add(new Camera.Area(focusRect, 1000));
        parameters.setFocusAreas(focusAreaList);

        if (parameters.getMaxNumMeteringAreas() > 0) {
            List<Camera.Area> meteringAreaList = new ArrayList<Camera.Area>();
            meteringAreaList.add(new Camera.Area(meteringRect, 1000));
            parameters.setMeteringAreas(meteringAreaList);
        }

        //TODO sometimes causes an exception
        mCamera.setParameters(parameters);
        mCamera.autoFocus(this);

    }

    /**
     * Convert touch position x:y to {@link Camera.Area} position -1000:-1000 to 1000:1000.
     * <p>
     * Rotate, scale and translate touch rectangle using matrix configured in
     */
    private Rect calculateTapArea(float x, float y, float coefficient) {
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();

        int left = clamp((int) x - areaSize / 2, 0, mPreview.getWidth() - areaSize);
        int top = clamp((int) y - areaSize / 2, 0, mPreview.getHeight() - areaSize);

        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);
        // TODO: Am I using the matrix the right way (do I even need it)?
        matrix.mapRect(rectF);

        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
    }

    private int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    // Helper method which returns the Camera.Size object representing the largest pixel dimensions
    // for the given Camera.Parameters
    private Camera.Size getLargestPictureSize(Camera.Parameters params) {
        Camera.Size result=null;

        for (Camera.Size size : params.getSupportedPictureSizes()) {
            if (result == null) {
                result=size;
            }
            else {
                int prevArea=result.width * result.height;
                int newArea=size.width * size.height;

                if (newArea > prevArea) {
                    result=size;
                }
            }
        }

        return(result);
    }

    // Helper method which returns the smallest picture size for the given camera parameters.
    private Camera.Size getSmallestPictureSize(Camera.Parameters parameters) {
        Camera.Size result=null;

        for (Camera.Size size : parameters.getSupportedPictureSizes()) {
            if (result == null) {
                result=size;
            }
            else {
                int resultArea=result.width * result.height;
                int newArea=size.width * size.height;

                if (newArea < resultArea) {
                    result=size;
                }
            }
        }

        return(result);
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            Log.d(TAG, "Camera could not be opened");
        }
        return c; // returns null if camera is unavailable
    }

    /** Overloaded method that opens a camera with the given id. */
    public static Camera getCameraInstance(int cameraID){
        Camera c = null;
        try {
            c = Camera.open(cameraID); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            Log.d(TAG, "Camera could not be opened");
        }
        return c; // returns null if camera is unavailable
    }

    // Helper method for creating a file Uri for saving an image or video.
    private Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    // Create a File for saving an image or video
    private File getOutputMediaFile(int type){

        // Get the internal storage directory for this app
        File mediaStorageDir = getFilesDir();

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                Log.d(TAG, "failed to create internal storage directory");
                return null;
            }
        }

        // Create a unique media file name based on the current time stamp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        // TODO: change the filename to include the user ID
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    // Method that is called to change the flash of the camera
    private void changeFlash() {
        // TODO: Is this null check needed?
        if (mCamera != null) {
            Camera.Parameters params = mCamera.getParameters();
            flashMode = params.getFlashMode();
            // The camera does have the capability for flash
            if (flashMode != null) {
                if (flashMode.equals(Camera.Parameters.FLASH_MODE_OFF)) {
                    flashMode = Camera.Parameters.FLASH_MODE_ON;
                    flashEnabled = true;
                } else if (flashMode.equals(Camera.Parameters.FLASH_MODE_ON)) {
                    flashMode = Camera.Parameters.FLASH_MODE_OFF;
                    flashEnabled = false;
                }
                params.setFlashMode(flashMode);
                mCamera.setParameters(params);
            }
        }
    }

    private String selectionToString(ArrayList<float[]> selection) {
        String s = "{ Length: " + selection.size() + "\n";
        for (float[] floatArray: selection) {
            s += "[" + floatArray[0] + "," + floatArray[1] + "],"; // Arrays.toString(floatArray) + ", ";
        }
        s += "P";
        s = s.replace(",P","\n}");
        return s;
    }

    private String selectionToString2(ArrayList<int[]> selection) {
        String s = "{ Length: " + selection.size() + "\n";
        for (int[] floatArray: selection) {
            s += "[" + floatArray[0] + "," + floatArray[1] + "],"; // Arrays.toString(floatArray) + ", ";
        }
        s += "P";
        s = s.replace(",P","\n}");
        return s;
    }


    public ArrayList<int[]> compressSelection(ArrayList<float[]> sel) {
        ArrayList<int[]> newSel = new ArrayList<>();
        int[] temp = {(int)sel.get(0)[0],(int)sel.get(0)[1]};
        newSel.add(temp);
        for (int i = 1; i < sel.size() - 1; i++) {
            float[] curr = sel.get(i);
            float[] next = sel.get(i+1);
            newSel.add(new int[]{(int) (next[0] - curr[0]), (int) (next[1] - curr[1])});
        }
        return newSel;
    }

}