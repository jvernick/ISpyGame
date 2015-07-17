package com.picspy.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.picspy.firstapp.R;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Justin12 on 6/28/2015.
 */
public class CameraActivity extends Activity {

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static final String TAG = "CameraActivityError";
    private Camera mCamera;
    private CameraPreview mPreview;
    private String flashMode = Camera.Parameters.FLASH_MODE_OFF;
    private boolean flashEnabled = false;
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            Intent intent = new Intent(getApplicationContext(), CreateChallengeActivity.class);
            // store the data of the image for the picture taken
            intent.putExtra("PictureTaken", data);
            startActivity(intent);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Add a listener to the Capture button
        ImageButton captureButton = (ImageButton) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        mCamera.takePicture(null, null, mPicture);
                    }
                }
        );



        final ImageButton flashButton = (ImageButton) findViewById(R.id.button_change_flash);
        flashButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        changeFlash(v);
                        if (flashMode != null && flashMode.equals(Camera.Parameters.FLASH_MODE_ON)) {
                            flashEnabled = true;
                        } else {
                            flashEnabled = false;
                        }
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
                        mCamera = mPreview.changeCamera();
                        flashMode = mCamera.getParameters().getFlashMode();
                        if (flashMode == null) {
                            flashEnabled = false;
                            flashButton.setSelected(flashEnabled);
                        }
                    }
                }
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();              // release the camera immediately on pause event
    }

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
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.removeAllViews();
        if (mCamera == null) {
            // Create an instance of Camera
            if (mPreview != null) {
                // resume the previously opened camera upon waking
                mCamera = getCameraInstance(mPreview.getCameraID());
                // retain the state of the flash if the camera has it
                if (flashMode != null) {
                    Camera.Parameters params = mCamera.getParameters();
                    params.setFlashMode(flashMode);
                    mCamera.setParameters(params);
                }
            }
            else {
                // upon first starting this activity, the back camera is opened
                mCamera = getCameraInstance();
            }
        }
        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera, this);

        preview.addView(mPreview);
    }

    @Override
    public void onBackPressed() {
        if (mPreview != null) {
            // reset the camera to be the front facing camera when the activity resumes
            mPreview.setCameraID(Camera.CameraInfo.CAMERA_FACING_BACK);
        }
        super.onBackPressed();  // this exits out of the current activity
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
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
        }
        return c; // returns null if camera is unavailable
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
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

    public void changeFlash(View view) {
        // TODO: Is this null check needed?
        if (mCamera != null) {
            Camera.Parameters params = mCamera.getParameters();
            flashMode = params.getFlashMode();
            if (flashMode != null) {
                if (flashMode.equals(Camera.Parameters.FLASH_MODE_OFF)) {
                    flashMode = Camera.Parameters.FLASH_MODE_ON;
                } else if (flashMode.equals(Camera.Parameters.FLASH_MODE_ON)) {
                    flashMode = Camera.Parameters.FLASH_MODE_OFF;
                }
                params.setFlashMode(flashMode);
                mCamera.setParameters(params);
            }
        }
    }
}
