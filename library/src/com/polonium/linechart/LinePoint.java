package com.polonium.linechart;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.TypedValue;

/**
 * The Class LinePoint.
 */
public class LinePoint {

    private float x = 0;
    private float y = 0;
    private boolean isVisible = false;
    private Paint strokePaint = new Paint();
    private Paint fillPaint = new Paint();
    private Paint textPaint = new Paint();
    private Type type = Type.CIRCLE;
    private float radius = 5;
    private String text = "";
    private boolean isTextVisible = false;
    private int textAlign = TextAlign.HORIZONTAL_CENTER | TextAlign.TOP;

    /**
     * Type of Point visualisation in {@link com.polonium.linechart.LineChartView LineChartView}.
     */
    public enum Type {
        /** Circle. */
        CIRCLE,
        /** Square. */
        SQUARE,
        /** Triangle. */
        TRIANGLE;
    }

    /**
     * Instantiates a new line point.
     * 
     * @param context
     *            context
     * @param x
     *            horizontal coordinate
     * @param y
     *            vertical coordinate
     */
    public LinePoint(Context context, float x, float y) {
        init(context);
        setPosition(x, y);
    }

    /**
     * Instantiates a new line point.
     * 
     * @param context
     *            the context
     */
    public LinePoint(Context context) {
        init(context);
    }

    /**
     * Sets the position of the point
     * 
     * @param x
     *            horizontal coordinate
     * @param y
     *            vertical coordinate
     * @return this
     */
    public LinePoint setPosition(float x, float y) {
        this.setX(x);
        this.setY(y);
        return this;
    }
   
    private void init(Context context) {
        getStrokePaint().setColor(0xff33b5e5);
        getStrokePaint().setStyle(Style.STROKE);
        getStrokePaint().setAntiAlias(true);
        getStrokePaint().setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                                  2,
                                                                  context.getResources().getDisplayMetrics()));
        getFillPaint().setColor(0xffffffff);

        getTextPaint().setColor(0xcc444444);
        getTextPaint().setAntiAlias(true);
        getTextPaint().setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                                                             10,
                                                             context.getResources().getDisplayMetrics()));
    }

    /**
    
     * @return horizontal coordinate
     */
    public float getX() {
        return x;
    }

    /**
     * Sets horizontal coordinate.
     * 
     * @param x
     *            horizontal coordinate
     * @return this
     */
    public LinePoint setX(float x) {
        this.x = x;
        return this;
    }

    /**
     * Gets the vertical coordinate.
     * 
     * @return vertical coordinate
     */
    public float getY() {
        return y;
    }

    /**
     * Sets the vertical coordinate.
     * 
     * @param y
     *            vertical coordinate
     * @return this
     */
    public LinePoint setY(float y) {
        this.y = y;
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "x= " + x + ", y= " + y;
    }

    /**
     * Checks if point visible.
     * 
     * @return true, if visible
     */
    public boolean isVisible() {
        return isVisible;
    }

    /**
     * Sets point visibility.
     * 
     * @param isVisible
     *            true if visible, false otherwise
     * @return this
     */
    public LinePoint setVisible(boolean isVisible) {
        this.isVisible = isVisible;
        return this;
    }

    /**
     * @return stroke {@link android.graphics.Paint Paint}
     * 
     * @see #setStrokePaint
     */
    public Paint getStrokePaint() {
        return strokePaint;
    }

    /**
     * Sets stroke paint.
     * 
     * @param strokePaint
     *            stroke {@link android.graphics.Paint Paint}
     * @return this
     * 
     * @see #getStrokePaint
     */
    public LinePoint setStrokePaint(Paint strokePaint) {
        this.strokePaint = strokePaint;
        return this;
    }

    /**
     * @return fill {@link android.graphics.Paint Paint}
     * 
     * @see #setFillPaint
     */
    public Paint getFillPaint() {
        return fillPaint;
    }

    /**
     * Sets {@link android.graphics.Paint Paint} to fill center of point.
     * 
     * @param fillPaint
     *            fill {@link android.graphics.Paint Paint}
     * @return this
     */
    public LinePoint setFillPaint(Paint fillPaint) {
        this.fillPaint = fillPaint;
        return this;
    }

    /**
     * Gets the text {@link android.graphics.Paint Paint}.
     * 
     * @return text {@link android.graphics.Paint Paint}
     * 
     * @see #setTextPaint
     */
    public Paint getTextPaint() {
        return textPaint;
    }

    /**
     * Sets the text {@link android.graphics.Paint Paint} for text near point 
     * 
     * @param textPaint
     *            text {@link android.graphics.Paint Paint}
     * @return this
     * 
     * @see #getTextPaint
     */
    public LinePoint setTextPaint(Paint textPaint) {
        this.textPaint = textPaint;
        return this;
    }

    /**
     * Gets the type.
     * 
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets {@link Type Type} for point visualization.
     * 
     * @param type
     *             type
     * @return this
     */
    public LinePoint setType(Type type) {
        this.type = type;
        return this;
    }

    /**
     * 
     * @return radius of the point for visualisation in pixels
     */
    public float getRadius() {
        return radius;
    }

    /**
     * Sets the radius for point visualisation in pixels.
     * 
     * @param radius
     *            radius in pixels
     * @return this
     */
    public LinePoint setRadius(float radius) {
        this.radius = radius;
        return this;
    }

    /**
     * @return text for the point
     */
    public String getText() {
        return text;
    }

    /**
     * Sets text for point which will be drawn if {@link #isTextVisible()}.
     * 
     * @param text
     *            the text
     * @return this
     */
    public LinePoint setText(String text) {
        this.text = text;
        return this;
    }

    /**
     * Checks if is text visible.
     * 
     * @return true, if is text visible
     */
    public boolean isTextVisible() {
        return isTextVisible;
    }

    /**
     * Sets the text visible.
     * 
     * @param isTextVisible
     *            is text visible
     * @return this
     * 
     * @see #isTextVisible()
     */
    public LinePoint setTextVisible(boolean isTextVisible) {
        this.isTextVisible = isTextVisible;
        return this;
    }

    /**
     * 
     * @return {@link com.polonium.linechart.TextAlign TextAlign} for the text
     */
    public int getTextAlign() {
        return textAlign;
    }

    /**
     * Sets {@link com.polonium.linechart.TextAlign TextAlign} for the text.
     * 
     * @param textAlign
     *            bitmask from {@link com.polonium.linechart.TextAlign TextAlign}
     * @return this
     */
    public LinePoint setTextAlign(int textAlign) {
        this.textAlign = textAlign;
        return this;
    }

}
