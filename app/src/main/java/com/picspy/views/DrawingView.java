package com.picspy.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by Justin12 on 8/9/2015.
 */

/**
 * A custom view which allows for drawing on a device's screen.
 */
class DrawingView extends View {
    Paint mPaint;
    //MaskFilter  mEmboss;
    //MaskFilter  mBlur;
    Bitmap mBitmap;
    Canvas mCanvas;
    Path mPath;
    Paint mBitmapPaint;
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;
    ArrayList<float[]> coordsOfPrevDraw;
    Stack<ArrayList<float[]>> coordsOfPastDrawings;
    float[] startCoordinates;
    boolean isShapeDrawn;
    private static final double RADIUS_TOLERANCE = 7;

    public DrawingView(Context context) {
        super(context);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFFFF0000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(10);

        mPath = new Path();
        mBitmapPaint = new Paint();
        // TODO: Does this color do anything?
        mBitmapPaint.setColor(Color.RED);

        coordsOfPastDrawings = new Stack<>();
    }

    /**
     * This is called when the view is first assigned a size and every time the size of the view changes
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.drawPath(mPath, mPaint);
    }

    private void touch_start(float x, float y) {
        //mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touch_move(float x, float y) {
        // TODO: change color appropriately so that drawing changes color when a complete shape is drawn
        if (coordsOfPrevDraw.size() % 2 == 0) {
            mPaint.setColor(0xFF0000FF);
        } else {
            mPaint.setColor(0xFFFF0000);
        }
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;
        }
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);
        // commit the path to our offscreen
        mCanvas.drawPath(mPath, mPaint);
        //mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
        // kill this so we don't double draw
        mPath.reset();
        // mPath= new Path();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Only handle touch events if a shape has not been drawn
        if (!isShapeDrawn) {
            float x = event.getX();
            float y = event.getY();
            float[] currCoordPair = new float[2];
            // Assign the touch event's coordinates as part of a pair
            currCoordPair[0] = x;
            currCoordPair[1] = y;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // hide all siblings (i.e. buttons) while drawing so they are not in the way
                    changeVisibilityOfSiblings(View.INVISIBLE);
                    Log.d("MOVE", "X start coord is " + x + " Y start coord is " + y);

                    // reset the arrays of x and y coordinate pairs for the current drawing
                    coordsOfPrevDraw = new ArrayList<>();
                    coordsOfPrevDraw.add(currCoordPair);
                    startCoordinates = currCoordPair;
                    touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    // TODO: do not think shape is drawn when two points are slowly drawn in sequence
                    // TODO: what should happen when you cross over your drawing but end at the start point?
                    double xDistance = Math.abs(startCoordinates[0] - x);
                    double yDistance = Math.abs(startCoordinates[1] - y);
                    double radiusFromCenter = Math.sqrt(xDistance * xDistance + yDistance * yDistance);
                    Log.d("MOVE", "X coord is " + x + " Y coord is " + y + "  distance is " + radiusFromCenter);
                    if (radiusFromCenter <= RADIUS_TOLERANCE && coordsOfPrevDraw.size() > 10) {
                        isShapeDrawn = true;
                        // restore the visibility of the siblings because the drawing has finished
                        changeVisibilityOfSiblings(View.VISIBLE);
                        // connect the drawing back to the starting coordinates
                        touch_move(startCoordinates[0], startCoordinates[1]);
                        touch_up();
                        invalidate();
                        // TODO: disable touching events and enable them back again at what point in time?
                        coordsOfPrevDraw.add(startCoordinates);
                        // Store the drawing for this shape in the stack
                        coordsOfPastDrawings.push(coordsOfPrevDraw);
                        break;
                    }

                    coordsOfPrevDraw.add(currCoordPair);

                    touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    // Store the previous stroke in the stack
                    coordsOfPastDrawings.push(coordsOfPrevDraw);

                    // restore visibility of siblings as drawing has finished
                    changeVisibilityOfSiblings(View.VISIBLE);
                    Log.d("MOVE", "X END coord is " + x + " Y END coord is " + y);
                    touch_up();
                    invalidate();
                    break;
            }
        }
        return true;
    }

    // Helper method to change the color of the brush
    public void changeBrushColor(int newColor) {
        mBitmapPaint.setColor(newColor);
    }

    // Helper method to undo the previous drawing. This is accomplished by essentially retracing
    // the drawing with an eraser.
    public void undoDrawing() {
        // Ensure the stack contains a previous stroke
        if (!coordsOfPastDrawings.isEmpty()) {
            ArrayList<float[]> previousCoords = coordsOfPastDrawings.pop();
            float[] coordPair = previousCoords.get(0);
            // make the stroke bigger so it erases completely
            mPaint.setStrokeWidth(mPaint.getStrokeWidth() + 10);
            // Enable erasing so that the previous points are erased
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

            touch_start(coordPair[0], coordPair[1]);
            for (int i = 1; i < previousCoords.size(); i++) {
                coordPair = previousCoords.get(i);
                touch_move(coordPair[0], coordPair[1]);
            }
            touch_up();

            invalidate();   // force the view to redraw itself to reflect the changes
            // turn the normal drawing mode back on
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));

            // restore the normal stroke width
            mPaint.setStrokeWidth(mPaint.getStrokeWidth() - 10);
            isShapeDrawn = false;
        }
    }

    // Helper method to change the brush width
    // TODO: ^ this method


    // A helper method to change the visibility of the views which are the siblings of this
    // DrawingView to the visibility passed in.
    private void changeVisibilityOfSiblings(int visibilityMode) {
        RelativeLayout rootView = (RelativeLayout) this.getParent();
        View child;
        if (rootView != null) {
            // iterate through the relatives of this DrawingView
            for (int i = 0; i < rootView.getChildCount(); i++) {
                child = rootView.getChildAt(i);
                // set the visibility of all views which don't have a tag or are not the drawingView
                if (child.getTag() == null || !(child.getTag().equals("DRAWING_VIEW"))) {
                    child.setVisibility(visibilityMode);
                }
            }
        }
    }
}
