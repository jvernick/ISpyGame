package com.picspy.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.picspy.firstapp.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;

/**
 * Created by Justin12 on 8/9/2015.
 */

/**
 * A custom view which allows for drawing on a device's screen.
 */
class DrawingView extends View {
    private Paint mPaint;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mBitmapPaint;
    private float mX, mY;
    // TODO: are these constants adequate for different sized devices?
    private static final float TOUCH_TOLERANCE = 4;
    private static final int MIN_NUM_OF_POINTS = 30;
    private static final int LINE_SEG_TOLERANCE = 7;
    public static final int MAX_NUM_LINE_SEGMENTS = 300;
    private ArrayList<float[]> coordsOfPrevDraw;
    private Stack<ArrayList<float[]>> coordsOfPastDrawings;
    private float[] startCoordinates;
    private boolean isShapeDrawn;
    private boolean invalidShapeDrawn;
    private static final int TOLERANCE_RATIO = 20;
    private Animation mAnimation;
    private int counter = 0;
    private int currColor;
    private static final int colorChanger = 0xffffff;
    DisplayMetrics metrics = getResources().getDisplayMetrics();
    int width;
    int height;
    int averageDim;

    private HashSet<LineSegment> lineSegments = new HashSet<>();

    private static final double EPSILON = 0.000001;

    // Constructor for this class which instantiates the Paint object for drawing, along with
    // other essential objects.
    public DrawingView(Context context) {
        super(context);
        currColor = Color.RED;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(currColor);
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
     * Calculate the cross product of two points.
     * @param a first point
     * @param b second point
     * @return the value of the cross product
     */
    public static double crossProduct(Point a, Point b) {
        return a.x * b.y - b.x * a.y;
    }

    /**
     * Check if bounding boxes do intersect. If one bounding box
     * touches the other, they do intersect.
     * @param a first bounding box
     * @param b second bounding box
     * @return <code>true</code> if they intersect,
     *         <code>false</code> otherwise.
     */
    public static boolean doBoundingBoxesIntersect(Point[] a, Point[] b) {
        return a[0].x <= b[1].x && a[1].x >= b[0].x && a[0].y <= b[1].y
                && a[1].y >= b[0].y;
    }

    /**
     * Checks if a Point is on a line
     * @param a line (interpreted as line, although given as line
     *                segment)
     * @param b point
     * @return <code>true</code> if point is on line, otherwise
     *         <code>false</code>
     */
    public static boolean isPointOnLine(LineSegment a, Point b) {
        // Move the image, so that a.first is on (0|0)
        LineSegment aTmp = new LineSegment(new Point(0, 0), new Point(
                a.second.x - a.first.x, a.second.y - a.first.y));
        Point bTmp = new Point(b.x - a.first.x, b.y - a.first.y);
        double r = crossProduct(aTmp.second, bTmp);
        return Math.abs(r) < EPSILON;
    }

    /**
     * Checks if a point is right of a line. If the point is on the
     * line, it is not right of the line.
     * @param a line segment interpreted as a line
     * @param b the point
     * @return <code>true</code> if the point is right of the line,
     *         <code>false</code> otherwise
     */
    public static boolean isPointRightOfLine(LineSegment a, Point b) {
        // Move the image, so that a.first is on (0|0)
        LineSegment aTmp = new LineSegment(new Point(0, 0), new Point(
                a.second.x - a.first.x, a.second.y - a.first.y));
        Point bTmp = new Point(b.x - a.first.x, b.y - a.first.y);
        return crossProduct(aTmp.second, bTmp) < 0;
    }

    /**
     * Check if line segment first touches or crosses the line that is
     * defined by line segment second.
     *
     * @param a line segment interpreted as line
     * @param b line segment
     * @return <code>true</code> if line segment first touches or
     *                           crosses line second,
     *         <code>false</code> otherwise.
     */
    public static boolean lineSegmentTouchesOrCrossesLine(LineSegment a,
                                                          LineSegment b) {
        return isPointOnLine(a, b.first)
                || isPointOnLine(a, b.second)
                || (isPointRightOfLine(a, b.first) ^ isPointRightOfLine(a,
                b.second));
    }

    /**
     * Check if line segments intersect
     * @param a first line segment
     * @param b second line segment
     * @return <code>true</code> if lines do intersect,
     *         <code>false</code> otherwise
     */
    public static boolean doLinesIntersect(LineSegment a, LineSegment b) {
        Point[] box1 = a.getBoundingBox();
        Point[] box2 = b.getBoundingBox();
        return doBoundingBoxesIntersect(box1, box2)
                && lineSegmentTouchesOrCrossesLine(a, b)
                && lineSegmentTouchesOrCrossesLine(b, a);
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
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        width = metrics.widthPixels;
        height = metrics.heightPixels;
        averageDim = width+height/2;
    }

    // This is called immediately after invalidate() is called and it draws the current path, mPath
    // on the canvas.
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.drawPath(mPath, mPaint);
    }

//    public void drawCircle(float centerX, float centerY, float radius) {
//        touch_start(centerX + radius, centerY);
//        invalidate();
//        double xMove, yMove, angle;
//        for (int phase = 0; phase <= 360; phase+=5) {
//            angle = Math.toRadians(phase);
//
//            xMove = radius*Math.cos(angle) + centerX;
//            yMove = radius*Math.sin(angle) + centerY;
//            touch_move((float)xMove, (float)yMove);
//        }
//        //close the circle
//        touch_up();
//        invalidate();
//    }

    // This helper method is used to initiate a path being drawn
    private void touch_start(float x, float y) {
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    // This helper method is used to continue a path that is being drawn
    private void touch_move(float x, float y) {
        // If too many line segments are drawn, alert the user with a toast and force them to redraw
        if (lineSegments.size() >= MAX_NUM_LINE_SEGMENTS) {
            forceRedraw("You were drawing for too long!");
            return;
        }
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        // construct a line segment from the new x and y coordinates
        LineSegment lineSeg = new LineSegment(new Point((int)mX,(int)mY), new Point((int)x, (int)y), counter);
        // Check if any line segments drawn already intersect with the current line segment drawn
        for (LineSegment lin : lineSegments) {
            int numSegmentsInBetween = Math.abs(lin.id - lineSeg.id);
            if (numSegmentsInBetween >= LINE_SEG_TOLERANCE && doLinesIntersect(lin, lineSeg)) {
                Log.d("LINE", String.format("Intersection between seg (%d,%d), (%d,%d) and (%d,%d), (%d,%d)", (int)mX, (int)mY, (int)x, (int)y, lin.first.x, lin.first.y, lin.second.x, lin.second.y));

                // Set the text for the toast to alert the user
                String text;
                if (numSegmentsInBetween == LINE_SEG_TOLERANCE) {
                    text = "Try drawing a little faster!";
                } else {
                    text = "You can't cross lines!";
                }
                forceRedraw(text);
                return;
            }
        }

        // Add the line segment to the set of previously drawn line segments
        lineSegments.add(lineSeg);
        counter++;  // increment the line segment counter, which uniquely identifies each segment
        // Only draw the path if the new coordinates are a distance greater than the touch tolerance
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.lineTo(x,y);
            mX = x;
            mY = y;
        }
    }

    // Helper method to erase the current drawing and force the user to redraw a new shape. It
    // takes a String parameter which is the message displayed to the user via a toast.
    private void forceRedraw(String message) {
        invalidShapeDrawn = true;
        Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.show();
        // add the current coordinate list to the stack of drawings
        coordsOfPastDrawings.add(coordsOfPrevDraw);

        try {
            Thread.sleep(300);                 // wait for .3 seconds
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        undoDrawing();  // erase the invalid shape
        // restore visibility of buttons
        changeVisibilityOfSiblings(View.VISIBLE);
    }

    // Helper method that ends the current path being drawn.
    private void touch_up() {
        mPath.lineTo(mX, mY);
        // commit the path to our offscreen
        mCanvas.drawPath(mPath, mPaint);
        // kill this so we don't double draw
        mPath.reset();
        // reset the set of line segments for the next drawing
        lineSegments = new HashSet<>();
        counter = 0;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // This check prevents users from continually drawing after an invalidShape was erased and
        // forces them to lift their finger before drawing a new shape.
        if (event.getAction() == MotionEvent.ACTION_UP && invalidShapeDrawn) {
            invalidShapeDrawn = false;
        }
        // Only handle touch events if a shape and an invalid shape have not been drawn
         else if (!isShapeDrawn && !invalidShapeDrawn) {
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
                    break;
                case MotionEvent.ACTION_MOVE:
                    double xDistance = Math.abs(startCoordinates[0] - x);
                    double yDistance = Math.abs(startCoordinates[1] - y);
                    double radiusFromCenter = Math.sqrt(xDistance * xDistance + yDistance * yDistance);
                    Log.d("MOVE", "X coord is " + x + " Y coord is " + y + "  distance is " + radiusFromCenter);
                    // Consider a shape to be drawn if the user drew close to the where they
                    // started and if they drew at least a certain number of points.
                    if (radiusFromCenter <= averageDim/TOLERANCE_RATIO && coordsOfPrevDraw.size() > MIN_NUM_OF_POINTS) {
                        isShapeDrawn = true;
                        // restore the visibility of the siblings because the drawing has finished
                        changeVisibilityOfSiblings(View.VISIBLE);

                        // Make the finish button visible
                        RelativeLayout rootView = (RelativeLayout) this.getParent();
                        Button finishButton = (Button) rootView.findViewById(R.id.finish_button);
                        finishButton.setVisibility(View.VISIBLE);

                        // xor with all 1's to create the opposite color and indicate the drawing is done
                        mPaint.setColor(currColor ^ colorChanger);
                        // connect the drawing back to the starting coordinates with the call to touch_up
                        mX = startCoordinates[0];
                        mY = startCoordinates[1];
                        touch_up();
                        invalidate();
                        coordsOfPrevDraw.add(startCoordinates);
                        // Store the drawing for this shape in the stack
                        coordsOfPastDrawings.push(coordsOfPrevDraw);

                        mAnimation = new AlphaAnimation(1, 0);
                        // Change alpha from fully visible to invisible
                        mAnimation.setDuration(500); // duration - half a second
                        mAnimation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
                        mAnimation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
                        // Reverse animation at the end so the layout will fade back in
                        mAnimation.setRepeatMode(Animation.REVERSE);
                        startAnimation(mAnimation);

                        break;
                    }

                    // TODO: is this logic valid?: only add the coordinates which are within the touch tolerance
                    if (xDistance >= TOUCH_TOLERANCE || yDistance >= TOUCH_TOLERANCE) {
                        coordsOfPrevDraw.add(currCoordPair);
                    }
                    touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    // This case is only evaluated if the user did not draw a valid shape

                    // Store the previous stroke in the stack
                    coordsOfPastDrawings.push(coordsOfPrevDraw);

                    // restore visibility of siblings as drawing has finished
                    changeVisibilityOfSiblings(View.VISIBLE);
                    Log.d("MOVE", "Failed shape: X END coord is " + x + " Y END coord is " + y);
                    touch_up();
                    invalidate();
                    try {
                        Thread.sleep(300);                 // pause for .3 seconds before undoing drawing
                    } catch(InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    undoDrawing();
                    break;
            }
         }
        return true;
    }

    // Helper method to change the color of the brush
    public void setBrushColor(int newColor) {
        currColor = newColor;
        mPaint.setColor(currColor);
    }

    // Returns the most previous valid drawing. If no valid drawing is drawn yet, return null.
    public ArrayList<float[]> getSelection() {
        if (isShapeDrawn) {
            return coordsOfPastDrawings.peek();
        }
        return null;
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
            clearAnimation();
            touch_start(coordPair[0], coordPair[1]);
            for (int i = 1; i < previousCoords.size(); i++) {
                coordPair = previousCoords.get(i);
                mPath.lineTo(coordPair[0],coordPair[1]);
                mX = coordPair[0];
                mY = coordPair[1];
            }
            touch_up();

            invalidate();   // force the view to redraw itself to reflect the changes
            // turn the normal drawing mode back on
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));

            // restore the normal stroke width
            mPaint.setStrokeWidth(mPaint.getStrokeWidth() - 10);
            isShapeDrawn = false;
            // Make the finish button invisible
            RelativeLayout rootView = (RelativeLayout) this.getParent();
            Button finishButton = (Button) rootView.findViewById(R.id.finish_button);
            finishButton.setVisibility(View.INVISIBLE);
            mPaint.setColor(currColor);
        }
    }

    // This method takes a selection array of coordinates and draws it on the drawingView
    public void drawSelection(ArrayList<float[]> selection) {
        float[] coordPair = selection.get(0);
        touch_start(coordPair[0], coordPair[1]);
        int size = selection.size();
        // iterate through each coordinate and repeat the starting point to close the shape
        for (int i = 1; i <= size; i++) {
            coordPair = selection.get(i % size);
            mPath.lineTo(coordPair[0],coordPair[1]);
            mX = coordPair[0];
            mY = coordPair[1];
        }
        touch_up();

        invalidate();   // force the view to redraw itself to reflect the changes
    }

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
                if (child.getTag() == null || (!(child.getTag().equals(CameraActivity.DRAWING_VIEW_TAG)) && !(child.getTag().equals(CameraActivity.FINISH_BUTTON_TAG)))) {
                    child.setVisibility(visibilityMode);
                }
            }
        }
    }

    public boolean isDoneDrawing() {
        return isShapeDrawn;
    }
}
