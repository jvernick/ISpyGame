package com.picspy.views;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.dreamfactory.api.FilesApi;
import com.dreamfactory.client.ApiException;
import com.dreamfactory.model.FileRequest;
import com.dreamfactory.model.FileResponse;
import com.picspy.GamesRequests;
import com.picspy.firstapp.R;
import com.picspy.utils.AppConstants;
import com.picspy.utils.PrefUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Justin12 on 6/28/2015.
 */

/**
 * The class which represents the custom camera. It has functionality such as auto-focusing,
 * switching between cameras, and changing flash modes.
 */
// TODO: Fix the focusing to focus in on the area touched
public class CameraActivity extends Activity implements Camera.AutoFocusCallback {

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static final String TAG = "CameraActivity";
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
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            Intent intent = new Intent(CameraActivity.this, CreateChallengeActivity.class);
            // store the URI of the image for the picture taken
            Uri imageUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
            try {
                // Write the data to a file on the device
                FileOutputStream fos = new FileOutputStream(new File(imageUri.getPath()));
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
            Log.d("DIMENS", "width = " + myFrameLayout.getWidth() + " height = " + myFrameLayout.getHeight());
            intent.putExtra("CameraID", mPreview.getCameraID());
            intent.putExtra("deviceOrientation", deviceOrientation);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivity(intent);
        }
    };

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
        matrix = new Matrix();

        myOrientationEventListener
                = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL){
            @Override
            public void onOrientationChanged(int arg0) {
                deviceOrientation = arg0;
            }};

        // Add a listener to the Capture button
        ImageButton captureButton = (ImageButton) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
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
                    }
                }
        );

        // Add a listener to the change flash button
        final ImageButton flashButton = (ImageButton) findViewById(R.id.button_change_flash);
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
        ImageButton switchButton = (ImageButton) findViewById(R.id.button_camera_switch);
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

    // Helper method to set the picture size for the next picture taken
    private void setPictureSize(int width, int height) {
        Camera.Parameters params = mCamera.getParameters();
        params.setPictureSize(width, height);
        mCamera.setParameters(params);
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();              // release the camera immediately on pause event
    }

    // Helper method to release the camera resources when no longer using it
    private void releaseCamera(){
        if (mCamera != null){
            flashMode = mCamera.getParameters().getFlashMode(); // save the flash mode
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
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
            }
            else {
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
        // TODO: Is this null check needed?
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        List<Camera.Area> focusAreaList = new ArrayList<Camera.Area>();
        focusAreaList.add(new Camera.Area(focusRect, 1000));
        parameters.setFocusAreas(focusAreaList);

        if (parameters.getMaxNumMeteringAreas() > 0) {
            List<Camera.Area> meteringAreaList = new ArrayList<Camera.Area>();
            meteringAreaList.add(new Camera.Area(meteringRect, 1000));
            parameters.setMeteringAreas(meteringAreaList);
        }

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
        File mediaStorageDir = CameraActivity.this.getFilesDir();

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

    /**
     * Class to send challenge to backend;
     * called as follows
     * new UploadFileTask().execute(file, params); where:
     * file is a FileRequest object containing the filename and file path
     * parsms isa ChallengeParams object containing challenge parameters
     * TODO implement the above call to this class so send challenge to backend
     */
    class UploadFileTask extends AsyncTask<Object, Void, String> {
        @Override
        protected void onPreExecute() {
            //uncomment to show a rogress bar
            //TODO confirm/create a progress display
            //progressDialog.show();
        }

        @Override
        protected String doInBackground(Object... params) {
            GamesRequests request = new GamesRequests(getApplicationContext(), true);
            return request.createGame((FileResponse) params[0],
                    (GamesRequests.ChallengeParams) params[1]);
        }

        @Override
        //TODO handle result and possible errors from server
        protected void onPostExecute(String resp) {
           /* if(progressDialog != null && progressDialog.isShowing()){
                progressDialog.cancel();
            }*/
            Toast.makeText(getApplicationContext(), resp, Toast.LENGTH_LONG).show();
        }
    }
}
