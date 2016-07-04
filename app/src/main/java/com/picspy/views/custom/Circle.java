package com.picspy.views.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.picspy.firstapp.R;

/**
 * Created by BrunelAmC on 6/23/2016.
 */
public class Circle extends View {
    private static final int START_ANGLE_POINT = 0;
    private static final int STROKE_WIDTH = 20;

    private RectF mCircleBounds = new RectF();

    private Paint wonPaint, lostPaint;
    private int wonColor, lostColor;

    private float wonAngle = 0;
    private float lostAngle = 0;
    private boolean isWon;

    /**
     * Class constructor taking only a context. Use this constructor to create
     * {@link Circle} objects from code.
     *
     * @param context application context
     */
    public Circle(Context context) {
        super(context);
        init();
    }

    public Circle(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.Circle,
                0, 0
        );

        try {
            wonColor = a.getColor(R.styleable.Circle_wonColor, getResources().getColor(R.color.red_500));
            lostColor = a.getColor(R.styleable.Circle_lostColor, getResources().getColor(R.color.primary));
        } finally {
            a.recycle();
        }

        init();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Try for a width based on our minimum
        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();

        int w = Math.max(minw, MeasureSpec.getSize(widthMeasureSpec));

        // Whatever the width ends up being, ask for a height that would let the pie
        // get as big as it can
        int minh = w + getPaddingBottom() + getPaddingTop();
        int h = Math.min(MeasureSpec.getSize(heightMeasureSpec), minh);

        setMeasuredDimension(w, h);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Set dimensions for circle
        // Account for padding
        float xpad = (float) (getPaddingLeft() + getPaddingRight());
        float ypad = (float) (getPaddingTop() + getPaddingBottom());

        float ww = (float) w - xpad;
        float hh = (float) h - ypad;

        // Figure out how big we can make the circle.
        float diameter = Math.min(ww, hh);
        mCircleBounds = new RectF(xpad / 2, ypad / 2, diameter, diameter);
    }

    private void init() {
        // Set up the paint for the Circle
        wonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wonPaint.setStyle(Paint.Style.STROKE);
        wonPaint.setStrokeWidth(STROKE_WIDTH);
        wonPaint.setColor(wonColor);

        lostPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lostPaint.setStyle(Paint.Style.STROKE);
        lostPaint.setStrokeWidth(STROKE_WIDTH);
        lostPaint.setColor(lostColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawArc(mCircleBounds, START_ANGLE_POINT, lostAngle, false, lostPaint);
        canvas.drawArc(mCircleBounds, START_ANGLE_POINT, wonAngle, false, wonPaint);
    }

    public void setWonAngle(float wonAngle) {
        this.wonAngle = wonAngle;
    }

    public void setLostAngle(float lostAngle) {
        this.lostAngle = lostAngle;
    }

    public float getWonAngle() {
        return wonAngle;
    }

    public float getLostAngle() {
        return lostAngle;
    }
}