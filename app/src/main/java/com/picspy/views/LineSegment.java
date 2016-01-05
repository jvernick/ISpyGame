package com.picspy.views;

import android.graphics.Point;

/**
 * Created by Justin12 on 12/31/2015.
 */
public class LineSegment {

    Point first, second;
    int id;

    public LineSegment(Point p1, Point p2) {
        first = p1;
        second = p2;
    }

    public LineSegment(Point p1, Point p2, int num) {
        first = p1;
        second = p2;
        id = num;
    }

    // The two coordinates of each box need to be the top-left and bottom-right of the box
    //(i.e. the line connecting the points has to have a positive slope).
    public Point[] getBoundingBox() {
        Point[] box = new Point[2];
        int minX = Math.min(first.x, second.x);
        int maxX = Math.max(first.x, second.x);
        int minY = Math.min(first.y, second.y);
        int maxY = Math.max(first.y, second.y);

        box[0] = new Point(minX, minY);
        box[1] = new Point(maxX, maxY);
        return box;
    }
}
