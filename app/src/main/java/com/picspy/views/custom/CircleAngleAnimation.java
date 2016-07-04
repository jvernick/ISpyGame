package com.picspy.views.custom;

import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by BrunelAmC on 6/23/2016.
 */
public class CircleAngleAnimation extends Animation {

    private Circle circle;

    private float oldLostAngle;
    private float newLostAngle;
    private float oldWonAngle;
    private float newWonAngle;

    public CircleAngleAnimation(Circle circle, float newLostAngle) {
        this.oldWonAngle = circle.getWonAngle();
        this.newWonAngle = 360 - newLostAngle;
        this.oldLostAngle = circle.getLostAngle();
        this.newLostAngle = newLostAngle;
        this.circle = circle;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation transformation) {
        float lostAngle = oldLostAngle + ((newLostAngle - oldLostAngle) * interpolatedTime);
        float wonAngle = oldWonAngle - ((newWonAngle - oldWonAngle) * interpolatedTime);

        circle.setWonAngle(wonAngle);
        circle.setLostAngle(lostAngle);
        circle.requestLayout();
    }
}
