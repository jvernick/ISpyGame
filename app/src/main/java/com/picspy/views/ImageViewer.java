package com.picspy.views;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.picspy.firstapp.R;

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
    boolean isSetup;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_viewer_layout);

        Intent intent = getIntent();
        imgPath = intent.getStringExtra(CameraActivity.IMAGE_PATH);
        selection =  (ArrayList<float[]>) intent.getSerializableExtra(CameraActivity.SELECTION);

        pic = (ImageView) findViewById(R.id.image_view);
        DisplayMetrics display = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(display);
        width = display.widthPixels;
        height = display.heightPixels;

       loadBitmap(imgPath, pic);

        pic.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!isSetup) {
                    isSetup = true;
                    width = pic.getWidth();
                    height = pic.getHeight();

                    ExifInterface exif = null;
                    try {
                        exif = new ExifInterface(imgPath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    int widthTrue = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, -1);
                    int heightTrue = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, -1);
                    float scalingX = (float) width / widthTrue;
                    float scalingY = (float) height / heightTrue;
                    selection = applyScalingToSelection(selection, scalingX, scalingY);
                }
                // TODO: handle the selection
                float x = event.getX();
                float y = event.getY();
                float[] click = {x,y};
                //selection = applyScalingToSelection(selection, );
                 if (isInsideSelection(click, selection)) {
                     Log.d("click", "Right");
                 } else {
                     Log.d("click", "Wrong");
                 }
                return true;
            }
        });

        int cameraID = intent.getIntExtra("CameraID", -1);
        matrix = new Matrix();

        if (cameraID == 1) {
            // Mirror the image for the 'selfie' camera so it is displayed properly
            matrix.preScale(-1, 1);
        }
        // TODO: Ensure that this rotation value applies for all devices
        //set image rotation value to 90 degrees in matrix.
        matrix.postRotate(90);

        final int[] dimens = new int[2]; // height = 0, width = 1
        // The view tree listener is needed in order to get the dimensions of the Views
//        ViewTreeObserver vto = pic.getViewTreeObserver();
//        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
//            public boolean onPreDraw() {
//                // Remove after the first run so it doesn't fire forever
//                pic.getViewTreeObserver().removeOnPreDrawListener(this);
//                // TODO: These dimensions may be needed in the future
//                dimens[0] = pic.getMeasuredHeight();
//                dimens[1] = pic.getMeasuredWidth();
//                // Adjust the bitmap accordingly based on the matrix
//                Bitmap rotatedBitmap = Bitmap.createBitmap(bmImg, 0, 0, bmImg.getWidth(), bmImg.getHeight(), matrix, true);
//                pic.setImageBitmap(rotatedBitmap);
//                return true;
//            }
//        });

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

    public static ArrayList<float[]> applyScalingToSelection(ArrayList<float[]> selection, float scalingFactorX, float scalingFactorY) {
        float[] coord;
        int size = selection.size();
        ArrayList<float[]> scaledResult = new ArrayList<>(size);
        for(int i = 0; i < size-1; i++) {
            coord = selection.get(i);
            coord[0] *= scalingFactorX;
            coord[1] *= scalingFactorY;
            scaledResult.add(i, coord);
        }
        return scaledResult;
    }

}
