package com.picspy.views;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.picspy.firstapp.R;

import java.io.File;

/**
 * Created by Justin12 on 7/12/2015.
 */

/**
 * The Activity where the user draws their selection of the hidden object in the image they chose.
 */
public class CreateChallengeActivity extends Activity {

    ImageView imageView;
    Bitmap myBitmap;
    Matrix matrix;
    FrameLayout mDrawingPad;
    ImageButton imageButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_challenge);

        DrawingView mDrawingView=new DrawingView(this);

        mDrawingPad = (FrameLayout)findViewById(R.id.view_drawing_pad);
        imageButton = (ImageButton)findViewById(R.id.colors_button);
        imageButton.setTag("COLOR_PALETTE");
        // Insert the Drawing View before the ImageButton so that the button overlays the canvas
        mDrawingPad.addView(mDrawingView, 0);

        // Sets a long click listener for the View using an anonymous listener object that
        // implements the OnLongClickListener interface
        imageButton.setOnLongClickListener(new View.OnLongClickListener() {

            // Defines the one method for the interface, which is called when the View is long-clicked
            public boolean onLongClick(View v) {

                // Create a new ClipData.
                // This is done in two steps to provide clarity. The convenience method
                // ClipData.newPlainText() can create a plain text ClipData in one step.

                // Create a new ClipData.Item from the ImageView object's tag
                ClipData.Item item = new ClipData.Item((String) v.getTag());

                // Create a new ClipData using the tag as a label, the plain text MIME type, and
                // the already-created item. This will create a new ClipDescription object within the
                // ClipData, and set its MIME type entry to "text/plain"
                ClipData dragData = new ClipData((String) v.getTag(), new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN}, item);

                // Instantiates the drag shadow builder.
                View.DragShadowBuilder myShadow = new MyDragShadowBuilder(imageView);

                // Starts the drag

                v.startDrag(dragData,  // the data to be dragged
                        myShadow,  // the drag shadow builder
                        null,      // no need to use local data
                        0          // flags (not currently used, set to 0)
                );
                return true;
            }
        });

        imageView = (ImageView)findViewById(R.id.imageView);
        int cameraID = getIntent().getExtras().getInt("CameraID");
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
        ViewTreeObserver vto = imageView.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                // Remove after the first run so it doesn't fire forever
                imageView.getViewTreeObserver().removeOnPreDrawListener(this);
                // TODO: These dimensions may be needed in the future
                dimens[0] = imageView.getMeasuredHeight();
                dimens[1] = imageView.getMeasuredWidth();
                // Adjust the bitmap accordingly based on the matrix
                Bitmap rotatedBitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true);
                imageView.setImageBitmap(rotatedBitmap);
                // TODO: leave for now for debugging purposes
                System.out.println("drawpad height = " + mDrawingPad.getHeight() + " drawpad width = " + mDrawingPad.getWidth());
                System.out.println("imageButton height = " + mDrawingPad.getChildAt(1).getHeight() + "imageButton width = " + mDrawingPad.getChildAt(1).getWidth());
                System.out.println("canvas height = " + ((DrawingView) mDrawingPad.getChildAt(0)).mCanvas.getHeight() + "canvas width = " + ((DrawingView) mDrawingPad.getChildAt(0)).mCanvas.getWidth());
                return true;
            }
        });

        // Retrieve the image file from the Intent
        Uri imageUri = (Uri) getIntent().getExtras().get(MediaStore.EXTRA_OUTPUT);
        File imgFile = new File(imageUri.getPath());
        if (imgFile.exists()) {
            // Convert the file to a Bitmap
            myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        }

        // TODO: Add UI for brush settings
    }

    // An inner class used for the drag shadow of the brush settings
    // TODO: Change this once the brush settings UI is done
    private static class MyDragShadowBuilder extends View.DragShadowBuilder {

        // The drag shadow image, defined as a drawable thing
        private static Drawable shadow;

        // Defines the constructor for myDragShadowBuilder
        public MyDragShadowBuilder(View v) {

            // Stores the View parameter passed to myDragShadowBuilder.
            super(v);

            // Creates a draggable image that will fill the Canvas provided by the system.
            shadow = new ColorDrawable(Color.LTGRAY);
        }

        // Defines a callback that sends the drag shadow dimensions and touch point back to the
        // system.
        @Override
        public void onProvideShadowMetrics(Point size, Point touch) {
            // Defines local variables
            int width, height;

            // Sets the width of the shadow to half the width of the original View
            width = getView().getWidth() / 2;

            // Sets the height of the shadow to half the height of the original View
            height = getView().getHeight() / 2;

            // The drag shadow is a ColorDrawable. This sets its dimensions to be the same as the
            // Canvas that the system will provide. As a result, the drag shadow will fill the
            // Canvas.
            shadow.setBounds(0, 0, width, height);

            // Sets the size parameter's width and height values. These get back to the system
            // through the size parameter.
            size.set(width, height);

            // Sets the touch point's position to be in the middle of the drag shadow
            touch.set(width / 2, height / 2);
        }

        // Defines a callback that draws the drag shadow in a Canvas that the system constructs
        // from the dimensions passed in onProvideShadowMetrics().
        @Override
        public void onDrawShadow(Canvas canvas) {

            // Draws the ColorDrawable in the Canvas passed in from the system.
            shadow.draw(canvas);
        }
    }
}
