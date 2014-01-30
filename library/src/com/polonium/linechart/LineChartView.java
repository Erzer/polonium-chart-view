package com.polonium.linechart;

import java.util.ArrayList;

import com.polonium.linechart.LinePoint.Type;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * View for drawing line charts.
 */
public class LineChartView extends View {

    private float mTouchSlop = 10;
    private ArrayList<Line> mLines = new ArrayList<Line>();
    private float mViewPortLeft = 0;
    private float mViewPortRight = 0;
    private float mViewPortTop = 0;
    private float mViewPortBottom = 0;
    private float mViewPortMarginLeft, mViewPortMarginRight, mViewPortMarginTop, mViewPortMarginBottom;
    private Shader cropViewPortShader;
    private Shader cropHorValuesShader;
    private Shader cropVerValuesShader;
    private ChartGrid mGrid;
    private float mLastX = Integer.MAX_VALUE;
    private float mLastY = Integer.MAX_VALUE;
    private float mScaleX;
    private float mScaleY;
    private Matrix mMatrix = new Matrix();
    private float mMaxX = Float.MIN_VALUE;
    private float mMaxY = Float.MIN_VALUE;
    private float mMinX = Float.MAX_VALUE;
    private float mMinY = Float.MAX_VALUE;
    private float mViewPortHorFreedom = 0;
    private float mViewPortVerFreedom = 0;
    private float velocityX = 0;
    private float velocityY = 0;
    private float frictionX = 1;
    private float frictionY = 1;
    private float mFrictionForceX = 10;
    private float mFrictionForceY = 10;
    private boolean isMovingX = false;
    private boolean isMovingY = false;
    private float pointClickRadius;

    private OnChartPointClickListener mListener;

    /**
     * The listener interface for receiving onChartPointClick events. The class that is interested in processing a
     * onPointClick event implements this interface, and the object created with that class is registered with a
     * component using the component's <code>setOnPointClickListener</code> method. When the onPointClick event occurs,
     * that object's appropriate method is invoked.
     */
    public interface OnChartPointClickListener {

        /**
         * Callback on chart point click.
         * 
         * @param point
         *            the point nearest to clicked position in point click radius
         * @param line
         *            the line - point holder
         */
        void onPointClick(LinePoint point, Line line);
    }

    /**
     * Instantiates a new line chart view.
     * 
     * @param context
     *            the context of activity
     */
    public LineChartView(Context context) {
        super(context);
        init(context);

    }

    /**
     * Instantiates a new line chart view.
     * 
     * @param context
     *            the context
     * @param attrs
     *            the attrs
     */
    public LineChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * Initialization after instantiation
     * 
     * @param context
     *            the context
     */
    private void init(Context context) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mGrid = new ChartGrid(context);
        setViewPortMargins(0,
                           TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20, getResources().getDisplayMetrics()),
                           0,
                           0);
        setVerValuesMargins((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                                                            8,
                                                            getResources().getDisplayMetrics()),
                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                                                            20,
                                                            getResources().getDisplayMetrics()),
                            0,
                            0);
        setPointClickRadius(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                      20,
                                                      getResources().getDisplayMetrics()));
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    /**
     * Sets the stopping force for inertial scrolling.
     * 
     * @param fx
     *            the horizontal resistance
     * @param fy
     *            the vertical resistance
     */
    public void setFriction(float fx, float fy) {
        mFrictionForceX = Math.abs(fx);
        mFrictionForceY = Math.abs(fy);
    }

    /**
     * Sets the additional movement for ViewPort after limits Max and Min values of LinePoints.
     * 
     * @param hor
     *            - horizontal limits addition
     * @param ver
     *            - vertical limits addition
     */
    public void setViewPortFreedom(float hor, float ver) {
        mViewPortHorFreedom = hor;
        mViewPortVerFreedom = ver;
    }

    /**
     * Sets the vertical grid lines style using {@link android.graphics.Paint Paint} for drawing
     * 
     * @param main
     *            style for main vertical grid lines
     * @param sub
     *            style for additional vertical grid lines
     * 
     * @see #setHorizontalGridStyle
     * @see #enableVerticalGrid
     */
    public void setVerticalGridStyle(Paint main, Paint sub) {
        if (main != null) mGrid.mainVerLinesPaint = main;
        if (sub != null) mGrid.subVerLinesPaint = sub;
    }

    /**
     * Sets the horizontal grid lines style using {@link android.graphics.Paint Paint} for drawing
     * 
     * @param main
     *            style for main vertical grid lines
     * @param sub
     *            style for additional vertical grid lines
     * 
     * @see #setVerticalGridStyle
     */
    public void setHorizontalGridStyle(Paint main, Paint sub) {
        if (main != null) mGrid.mainHorLinesPaint = main;
        if (sub != null) mGrid.subHorLinesPaint = sub;
    }

    /**
     * Show or hide vertical grid lines.
     * 
     * @param main
     *            - is main vertical grid lines visible?
     * @param sub
     *            - is additional vertical grid lines visible? (Not visible without main lines)
     * 
     * @see #setVerticalGridStyle
     */
    public void enableVerticalGrid(boolean main, boolean sub) {
        mGrid.verMainLinesEnabled = main;
        mGrid.verSubLinesEnabled = sub;
    }

    /**
     * Show or hide horizontal grid lines.
     * 
     * @param main
     *            - is main horizontal grid lines visible?
     * @param sub
     *            - is additional horizontal grid lines visible? (Not visible without main lines)
     * 
     * @see #setHorizontalGridStyle
     */
    public void enableHorizontalGrid(boolean main, boolean sub) {
        mGrid.horMainLinesEnabled = main;
        mGrid.horSubLinesEnabled = sub;
    }

    /**
     * Sets steps between main grid lines. Grid lines staring from 0 value.
     * 
     * @param horStep
     *            step between horizontal grid lines
     * @param horSubLinesCount
     *            count of additional lines between main horizontal lines
     * @param verStep
     *            the step between vertical grid lines
     * @param verSubLinesCount
     *            count of additional lines between main vertical lines
     * 
     * @see #setVerticalGridStyle
     * @see #setHorizontalGridStyle
     * @see #enableHorizontalGrid
     * @see #enableVerticalGrid
     */
    public void setGridSize(int horStep, int horSubLinesCount, int verStep, int verSubLinesCount) {
        mGrid.stepHor = horStep;
        mGrid.stepVer = verStep;
        mGrid.horSubLinesCount = horSubLinesCount;
        mGrid.verSubLinesCount = verSubLinesCount;
    }

    /**
     * Sets {@link android.graphics.Paint Paint} for drawing values for grid.
     * 
     * @param hor
     *            {@link android.graphics.Paint Paint} for horizontal values
     * @param ver
     *            {@link android.graphics.Paint Paint} for vertical values
     * 
     * @see #setVerticalGridStyle
     * @see #setHorizontalGridStyle
     */
    public void setMainValuesStyle(Paint hor, Paint ver) {
        if (hor != null) mGrid.mainHorValuesPaint = hor;
        if (ver != null) mGrid.mainVerValuesPaint = ver;
    }

    /**
     * Show or hide values on grid.
     * 
     * @param hor
     *            is horizontal values visible?
     * @param ver
     *            is vertical values visible?
     * 
     * @see LineChartView#setMainValuesStyle
     */
    public void enableMainValues(boolean hor, boolean ver) {
        mGrid.horMainValuesEnabled = hor;
        mGrid.verMainValuesEnabled = ver;
    }

    /**
     * Sets margins for horizontal grid values from view sides in pixels.
     * 
     * @param left
     *            left
     * @param bottom
     *            bottom
     * @param right
     *            right
     * @param top
     *            top
     * 
     * @see #setHorValuesMarginsDP
     */
    public void setHorValuesMargins(int left, int bottom, int right, int top) {
        mGrid.horValuesMarginLeft = left;
        mGrid.horValuesMarginRight = right;
        mGrid.horValuesMarginTop = top;
        mGrid.horValuesMarginBottom = bottom;
        cropHorValuesShader = null;
    }

    /**
     * Sets margins for vertical grid values from view sides in pixels.
     * 
     * @param left
     *            left
     * @param bottom
     *            bottom
     * @param right
     *            right
     * @param top
     *            top
     * 
     * @see #setVerValuesMarginsDP
     */
    public void setVerValuesMargins(int left, int bottom, int right, int top) {
        mGrid.verValuesMarginLeft = left;
        mGrid.verValuesMarginRight = right;
        mGrid.verValuesMarginTop = top;
        mGrid.verValuesMarginBottom = bottom;
        cropVerValuesShader = null;
    }

    /**
     * Sets margins for horizontal values from view sides in dip.
     * 
     * @param left
     *            left
     * @param bottom
     *            bottom
     * @param right
     *            right
     * @param top
     *            top
     * 
     * @see #setHorValuesMargins
     */
    public void setHorValuesMarginsDP(int left, int bottom, int right, int top) {
        mGrid.horValuesMarginLeft = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                                    left,
                                                                    getResources().getDisplayMetrics());
        mGrid.horValuesMarginRight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                                     right,
                                                                     getResources().getDisplayMetrics());
        mGrid.horValuesMarginTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                                   top,
                                                                   getResources().getDisplayMetrics());
        mGrid.horValuesMarginBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                                      bottom,
                                                                      getResources().getDisplayMetrics());
        cropHorValuesShader = null;
    }

    /**
     * Sets margins for vertical values from view sides in dip.
     * 
     * @param left
     *            left
     * @param bottom
     *            bottom
     * @param right
     *            right
     * @param top
     *            top
     * 
     * @see #setVerValuesMargins
     */
    public void setVerValuesMarginsDP(int left, int bottom, int right, int top) {
        mGrid.verValuesMarginLeft = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                                    left,
                                                                    getResources().getDisplayMetrics());
        mGrid.verValuesMarginRight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                                     right,
                                                                     getResources().getDisplayMetrics());
        mGrid.verValuesMarginTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                                   top,
                                                                   getResources().getDisplayMetrics());
        mGrid.verValuesMarginBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                                      bottom,
                                                                      getResources().getDisplayMetrics());
        cropVerValuesShader = null;
    }

    /**
     * Sets custom text for horizontal values. Use {@link android.util.SparseArray} for mapping grid value and text for
     * it. If no text for value, grid line will drawn without text.
     * 
     * @param map
     *            {@link android.util.SparseArray} with text for drawing. If <i>map</i> is not null text for lines
     *            taking from it. Else integer values from grid will be drawn.
     * @see #setVerValuesText
     */
    public void setHorValuesText(SparseArray<String> map) {
        mGrid.horValuesText = map;
    }

    /**
     * Sets custom text for vertical values. Use {@link android.util.SparseArray} for mapping grid value and text for
     * it. If no text for value, grid line will drawn without text.
     * 
     * @param map
     *            {@link android.util.SparseArray} with text for drawing. If <i>map</i> is not null text for lines
     *            taking from it. Else integer values from grid will be drawn.
     * @see #setHorValuesText
     */
    public void setVerValuesText(SparseArray<String> map) {
        mGrid.verValuesText = map;
    }

    /**
     * Removes all lines.
     * 
     * @see addLine
     */
    public void removeAllLines() {
        while (mLines.size() > 0) {
            mLines.remove(0);
        }
        postInvalidate();
    }

    /**
     * Adds the line for drawing.
     * 
     * @param line
     *            {@link com.polonium.linechart.Line Line}
     */
    public void addLine(Line line) {
        mLines.add(line);
        postInvalidate();

        for (LinePoint point : line.getPoints()) {
            mMaxX = point.getX() > mMaxX ? point.getX() : mMaxX;
            mMaxY = point.getY() > mMaxY ? point.getY() : mMaxY;
            mMinX = point.getX() < mMinX ? point.getX() : mMinX;
            mMinY = point.getY() < mMinY ? point.getY() : mMinY;
        }
    }

    /**
     * Correct min and max values for ViewPort moving limits.
     */
    private void limitsCorrection() {
        mMaxX = Float.MIN_VALUE;
        mMaxY = Float.MIN_VALUE;
        mMinX = Float.MAX_VALUE;
        mMinY = Float.MAX_VALUE;
        for (Line line : mLines)
            for (LinePoint point : line.getPoints()) {
                mMaxX = point.getX() > mMaxX ? point.getX() : mMaxX;
                mMaxY = point.getY() > mMaxY ? point.getY() : mMaxY;
                mMinX = point.getX() < mMinX ? point.getX() : mMinX;
                mMinY = point.getY() < mMinY ? point.getY() : mMinY;
            }

        mMaxX = (mMaxX < mViewPortRight) ? mViewPortRight : mMaxX;
        mMaxY = (mMaxY < mViewPortTop) ? mViewPortTop : mMaxY;
        mMinX = (mMinX > mViewPortLeft) ? mViewPortLeft : mMinX;
        mMinY = (mMinY > mViewPortBottom) ? mViewPortBottom : mMinY;
    }

    /**
     * 
     * @param index
     *            the index
     * @return {@link Line}
     */
    public Line getLine(int index) {
        return mLines.get(index);
    }

    /**
     * 
     * @return lines count
     */
    public int getLinesCount() {
        return mLines.size();
    }

    /**
     * Sets ViewPort - part of full chart for drawing
     * 
     * @param left
     *            minimal horizontal value of chart to draw
     * @param bottom
     *            minimal vertical value of chart to draw
     * @param right
     *            maximal horizontal value of chart to draw
     * @param top
     *            maximal horizontal value of chart to draw
     */
    public void setViewPort(float left, float bottom, float right, float top) {
        mViewPortLeft = left;
        mViewPortRight = right;
        mViewPortTop = top;
        mViewPortBottom = bottom;
        limitsCorrection();
    }

    /**
     * Sets ViewPort margins from view sides in pixels.
     * 
     * @param left
     *            left
     * @param bottom
     *            bottom
     * @param right
     *            right
     * @param top
     *            top
     * @see #setViewPortMargins
     */
    public void setViewPortMargins(float left, float bottom, float right, float top) {
        mViewPortMarginLeft = left;
        mViewPortMarginRight = right;
        mViewPortMarginTop = top;
        mViewPortMarginBottom = bottom;
        cropViewPortShader = null;
        scaleCorrection();
    }

    /**
     * Sets ViewPort margins from view sides in dip.
     * 
     * @param left
     *            the left
     * @param bottom
     *            the bottom
     * @param right
     *            the right
     * @param top
     *            the top
     * 
     * @see #setViewPortMargins
     */
    public void setViewPortMarginsDP(float left, float bottom, float right, float top) {
        mViewPortMarginLeft = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                        left,
                                                        getResources().getDisplayMetrics());
        mViewPortMarginRight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                         right,
                                                         getResources().getDisplayMetrics());
        mViewPortMarginTop = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                       top,
                                                       getResources().getDisplayMetrics());
        mViewPortMarginBottom = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                          bottom,
                                                          getResources().getDisplayMetrics());
        cropViewPortShader = null;
        scaleCorrection();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.view.View#onMeasure(int, int)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Log.i("", "onMeasure");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        scaleCorrection();
    }

    /**
     * Scale correction.
     */
    private void scaleCorrection() {
        mScaleX = (mViewPortRight - mViewPortLeft != 0) ? (getMeasuredWidth() - mViewPortMarginLeft - mViewPortMarginRight) / (mViewPortRight - mViewPortLeft)
                                                       : 1;
        mScaleY = (mViewPortTop - mViewPortBottom != 0) ? (getMeasuredHeight() - mViewPortMarginTop - mViewPortMarginBottom) / (mViewPortTop - mViewPortBottom)
                                                       : 1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.view.View#onDraw(android.graphics.Canvas)
     */
    public void onDraw(Canvas canvas) {
        inertionMove();
        if (cropViewPortShader == null) {
            Bitmap cropBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Config.ALPHA_8);
            Canvas cropCanvas = new Canvas(cropBitmap);

            Paint cropPaint = new Paint();
            cropPaint.setColor(0xffffffff);
            cropPaint.setStyle(Paint.Style.FILL);

            cropCanvas.drawRect(mViewPortMarginLeft - 1,
                                mViewPortMarginTop - 1,
                                getWidth() - mViewPortMarginRight + 1,
                                getHeight() - mViewPortMarginBottom + 1,
                                cropPaint);
            cropViewPortShader = new BitmapShader(cropBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        }

        drawVerticalGrid(canvas);
        drawHorizontalGrid(canvas);
        for (Line line : mLines) {
            drawLine(canvas, line);
        }

        drawPoints(canvas);
        drawValues(canvas);
    }

    /**
     * Draw line.
     * 
     * @param canvas
     *            canvas
     * @param line
     *            line
     */
    private void drawLine(Canvas canvas, Line line) {
        Path pathCopy;
        if (line.isFilled()) {
            pathCopy = new Path(line.getFilledPath());
            mMatrix.reset();
            mMatrix.setScale(mScaleX, -mScaleY);

            pathCopy.transform(mMatrix);
            mMatrix.reset();
            mMatrix.setTranslate(-mViewPortLeft * mScaleX + mViewPortMarginLeft, (mViewPortTop) * mScaleY
                                                                                 + mViewPortMarginTop);
            pathCopy.transform(mMatrix);
            line.getFilledPaint().setShader(cropViewPortShader);
            canvas.drawPath(pathCopy, line.getFilledPaint());
        }

        pathCopy = new Path(line.getPath());
        mMatrix.reset();
        mMatrix.setScale(mScaleX, -mScaleY);
        pathCopy.transform(mMatrix);
        mMatrix.reset();
        mMatrix.setTranslate(-mViewPortLeft * mScaleX + mViewPortMarginLeft, (mViewPortTop) * mScaleY
                                                                             + mViewPortMarginTop);
        pathCopy.transform(mMatrix);
        line.getPaint().setShader(cropViewPortShader);
        canvas.drawPath(pathCopy, line.getPaint());
    }

    private void drawPoints(Canvas canvas) {
        for (Line line : mLines) {
            for (LinePoint point : line.getPoints()) {
                drawPoint(canvas, point);
                drawPointText(canvas, point);
            }
        }
    }

    private void drawPoint(Canvas canvas, LinePoint point) {
        float x = point.getX() * mScaleX - mViewPortLeft * mScaleX + mViewPortMarginLeft;
        float y = point.getY() * (-mScaleY) + mViewPortTop * mScaleY + mViewPortMarginTop;
        if (point.isVisible()) {
            if (point.getX() + (point.getRadius() + point.getStrokePaint().getStrokeWidth()) > mViewPortMarginLeft && point.getX() - (point.getRadius() + point.getStrokePaint()
                                                                                                                                                               .getStrokeWidth()) < getWidth() - mViewPortMarginRight
                && point.getY() + (point.getRadius() + point.getStrokePaint().getStrokeWidth()) > mViewPortMarginTop
                && point.getY() - (point.getRadius() + point.getStrokePaint().getStrokeWidth()) < getHeight() - mViewPortMarginBottom) {
                point.getFillPaint().setShader(cropViewPortShader);
                point.getStrokePaint().setShader(cropViewPortShader);
                if (point.getType() == Type.CIRCLE) {
                    canvas.drawCircle(x, y, point.getRadius(), point.getFillPaint());
                    canvas.drawCircle(x, y, point.getRadius(), point.getStrokePaint());
                } else if (point.getType() == Type.SQUARE) {
                    canvas.drawRect(x - point.getRadius(),
                                    y - point.getRadius(),
                                    x + point.getRadius(),
                                    y + point.getRadius(),
                                    point.getFillPaint());
                    canvas.drawRect(x - point.getRadius(),
                                    y - point.getRadius(),
                                    x + point.getRadius(),
                                    y + point.getRadius(),
                                    point.getStrokePaint());
                } else if (point.getType() == Type.TRIANGLE) {
                    Path path = new Path();
                    path.moveTo(x, y - point.getRadius());
                    path.lineTo(x - 0.86f * point.getRadius(), y + 0.5f * point.getRadius());
                    path.lineTo(x + 0.86f * point.getRadius(), y + 0.5f * point.getRadius());
                    path.close();
                    canvas.drawPath(path, point.getFillPaint());
                    canvas.drawPath(path, point.getStrokePaint());
                }

            }

        }
    }

    private void drawPointText(Canvas canvas, LinePoint point) {
        float x = point.getX() * mScaleX - mViewPortLeft * mScaleX + mViewPortMarginLeft;
        float y = point.getY() * (-mScaleY) + mViewPortTop * mScaleY + mViewPortMarginTop;
        if (point.isTextVisible()) {
            point.getTextPaint().setTextAlign(Align.CENTER);
            point.getTextPaint().setShader(cropViewPortShader);
            float txtX = x;
            float txtY = y + (point.getTextPaint().getTextSize() - point.getTextPaint().descent()) / 2;
            if ((point.getTextAlign() & TextAlign.LEFT) > 0) {
                point.getTextPaint().setTextAlign(Align.RIGHT);
                txtX = x - point.getRadius() - point.getTextPaint().descent();
            } else if ((point.getTextAlign() & TextAlign.RIGHT) > 0) {
                point.getTextPaint().setTextAlign(Align.LEFT);
                txtX = x + point.getRadius() + point.getTextPaint().descent();
            }

            if ((point.getTextAlign() & TextAlign.TOP) > 0) {
                txtY = y - point.getRadius() - point.getTextPaint().descent();
            } else if ((point.getTextAlign() & TextAlign.BOTTOM) > 0) {
                txtY = y + point.getRadius() + point.getTextPaint().descent() + point.getTextPaint().getTextSize();
            }
            canvas.drawText(point.getText(), txtX, txtY, point.getTextPaint());
        }
    }

    private void drawVerticalGrid(Canvas canvas) {
        int firstLertLineX = (((int) mViewPortLeft) / mGrid.stepHor) * mGrid.stepHor;
        float subStep = (float) mGrid.stepHor / (mGrid.horSubLinesCount + 1);
        mGrid.mainVerLinesPaint.setShader(cropViewPortShader);
        mGrid.subVerLinesPaint.setShader(cropViewPortShader);
        mGrid.mainHorValuesPaint.setShader(cropHorValuesShader);
        if (mGrid.verMainLinesEnabled) {
            for (int x = firstLertLineX; x < mViewPortRight + mViewPortMarginRight / mScaleY; x += mGrid.stepHor) {
                canvas.drawLine((x - mViewPortLeft) * mScaleX + mViewPortMarginLeft,
                                mViewPortMarginTop,
                                (x - mViewPortLeft) * mScaleX + mViewPortMarginLeft,
                                getHeight() - mViewPortMarginBottom,
                                mGrid.mainVerLinesPaint);
                if (mGrid.verSubLinesEnabled) {
                    for (int i = 1; i <= mGrid.horSubLinesCount; i++) {
                        canvas.drawLine((x + i * subStep - mViewPortLeft) * mScaleX + mViewPortMarginLeft,
                                        mViewPortMarginTop,
                                        (x + i * subStep - mViewPortLeft) * mScaleX + mViewPortMarginLeft,
                                        getHeight() - mViewPortMarginBottom,
                                        mGrid.subVerLinesPaint);
                    }
                }
            }

        }
    }

    private void drawHorizontalGrid(Canvas canvas) {
        int firstHorLineY = (((int) mViewPortBottom) / mGrid.stepVer) * mGrid.stepVer;
        float subStep = (float) mGrid.stepVer / (mGrid.verSubLinesCount + 1);
        mGrid.mainHorLinesPaint.setShader(cropViewPortShader);
        mGrid.subHorLinesPaint.setShader(cropViewPortShader);
        mGrid.mainVerValuesPaint.setShader(cropVerValuesShader);

        if (mGrid.horMainLinesEnabled) {
            for (int y = firstHorLineY; y < mViewPortTop; y += mGrid.stepVer) {
                canvas.drawLine(mViewPortMarginLeft,
                                getHeight() - mViewPortMarginBottom - (y - mViewPortBottom) * mScaleY,
                                getWidth() - mViewPortMarginRight,
                                getHeight() - mViewPortMarginBottom - (y - mViewPortBottom) * mScaleY,
                                mGrid.mainHorLinesPaint);
                if (mGrid.horSubLinesEnabled) {
                    for (int i = 1; i <= mGrid.verSubLinesCount; i++) {
                        canvas.drawLine(mViewPortMarginLeft,
                                        getHeight() - mViewPortMarginBottom
                                                - (y - mViewPortBottom + i * subStep)
                                                * mScaleY,
                                        getWidth() - mViewPortMarginRight,
                                        getHeight() - mViewPortMarginBottom
                                                - (y - mViewPortBottom + i * subStep)
                                                * mScaleY,
                                        mGrid.subHorLinesPaint);
                    }
                }
            }
        }
    }

    private void drawValues(Canvas canvas) {
        drawVerticalValues(canvas);
        drawHorizontalValues(canvas);
    }

    private void drawHorizontalValues(Canvas canvas) {
        if (mGrid.horMainValuesEnabled) {
            if (cropHorValuesShader == null) {
                Bitmap cropHorValuesBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Config.ALPHA_8);
                Canvas cropCanvas = new Canvas(cropHorValuesBitmap);

                Paint cropPaint = new Paint();
                cropPaint.setColor(0xffffffff);
                cropPaint.setStyle(Paint.Style.FILL);

                float descent = (mGrid.mainHorValuesPaint == null) ? 0 : mGrid.mainHorValuesPaint.descent();

                cropCanvas.drawRect(mGrid.horValuesMarginLeft,
                                    mGrid.horValuesMarginTop,
                                    getWidth() - mGrid.horValuesMarginRight,
                                    getHeight() - mGrid.horValuesMarginBottom + descent,
                                    cropPaint);
                cropHorValuesShader = new BitmapShader(cropHorValuesBitmap,
                                                       Shader.TileMode.CLAMP,
                                                       Shader.TileMode.CLAMP);
            }
            int first = ((int) ((mViewPortLeft - mViewPortMarginLeft / mScaleX) / mGrid.stepHor) - 1) * mGrid.stepHor;
            int last = (int) (mViewPortRight + mViewPortMarginRight / mScaleX) + mGrid.stepHor;
            // float subStep = (float) mGrid.stepHor / (mGrid.horSubLinesCount +
            // 1);

            mGrid.mainVerLinesPaint.setShader(cropViewPortShader);
            mGrid.subVerLinesPaint.setShader(cropViewPortShader);
            mGrid.mainHorValuesPaint.setShader(cropHorValuesShader);

            for (int x = first; x <= last; x += mGrid.stepHor) {
                String string = String.valueOf(x);
                if (mGrid.horValuesText != null) string = mGrid.horValuesText.get(x);
                if (string == null) string = "";

                float txtY = getHeight() - 1 - mGrid.horValuesMarginBottom;
                float txtX = (x - mViewPortLeft) * mScaleX + mViewPortMarginLeft;

                if ((mGrid.horValuesAlign & TextAlign.TOP) > 0) {
                    txtY = mGrid.horValuesMarginTop + mGrid.mainHorValuesPaint.getTextSize();
                } else if ((mGrid.horValuesAlign & TextAlign.VERTICAL_CENTER) > 0) {
                    txtY = (getHeight() - (mGrid.horValuesMarginTop + mGrid.horValuesMarginBottom) + mGrid.mainHorValuesPaint.getTextSize()) / 2;
                }
                if ((mGrid.horValuesAlign & TextAlign.LEFT) > 0) {
                    mGrid.mainHorValuesPaint.setTextAlign(Align.LEFT);
                } else if ((mGrid.horValuesAlign & TextAlign.RIGHT) > 0) {
                    mGrid.mainHorValuesPaint.setTextAlign(Align.RIGHT);
                } else if ((mGrid.horValuesAlign & TextAlign.HORIZONTAL_CENTER) > 0) {
                    mGrid.mainHorValuesPaint.setTextAlign(Align.CENTER);
                }
                canvas.drawText(string, txtX, txtY, mGrid.mainHorValuesPaint);
            }
        }
    }

    private void drawVerticalValues(Canvas canvas) {
        if (mGrid.verMainValuesEnabled) {
            if (cropVerValuesShader == null) {
                Bitmap cropBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Config.ALPHA_8);
                Canvas cropCanvas = new Canvas(cropBitmap);

                Paint cropPaint = new Paint();
                cropPaint.setColor(0xffffffff);
                cropPaint.setStyle(Paint.Style.FILL);
                float descent = (mGrid.mainVerValuesPaint == null) ? 0 : mGrid.mainVerValuesPaint.descent();
                cropCanvas.drawRect(mGrid.verValuesMarginLeft,
                                    mGrid.verValuesMarginTop,
                                    getWidth() - mGrid.verValuesMarginRight,
                                    getHeight() - mGrid.verValuesMarginBottom + descent,
                                    cropPaint);
                cropVerValuesShader = new BitmapShader(cropBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            }
            int firstHorLineY = ((int) (mViewPortBottom - mViewPortMarginBottom / mScaleY) / mGrid.stepVer - 1) * mGrid.stepVer;
            int last = (int) (mViewPortTop + mViewPortMarginTop / mScaleY) + mGrid.stepVer;
            // float subStep = (float) mGrid.stepVer / (mGrid.verSubLinesCount + 1);
            mGrid.mainVerValuesPaint.setShader(cropVerValuesShader);
            for (int y = firstHorLineY; y < last; y += mGrid.stepVer) {
                String string = String.valueOf(y);
                if (mGrid.verValuesText != null) string = mGrid.verValuesText.get(y);
                if (string == null) string = "";
                int txtX = mGrid.verValuesMarginLeft;
                float txtY = (getHeight() - mViewPortMarginBottom) - (y - mViewPortBottom)
                             * mScaleY
                             - mGrid.mainVerValuesPaint.descent();

                if ((mGrid.verValuesAlign & TextAlign.TOP) > 0) {
                    txtY = (getHeight() - mViewPortMarginBottom) - (y - mViewPortBottom)
                           * mScaleY
                           + mGrid.mainVerValuesPaint.getTextSize()
                           + mGrid.mainVerValuesPaint.descent();
                } else if ((mGrid.verValuesAlign & TextAlign.VERTICAL_CENTER) > 0) {
                    txtY = (getHeight() - mViewPortMarginBottom) - (y - mViewPortBottom)
                           * mScaleY
                           + (mGrid.mainVerValuesPaint.getTextSize() - mGrid.mainVerValuesPaint.descent())
                           / 2;
                }
                if ((mGrid.verValuesAlign & TextAlign.LEFT) > 0) {
                    mGrid.mainVerValuesPaint.setTextAlign(Align.LEFT);
                } else if ((mGrid.verValuesAlign & TextAlign.RIGHT) > 0) {
                    mGrid.mainVerValuesPaint.setTextAlign(Align.RIGHT);
                    txtX = getWidth() - mGrid.verValuesMarginRight;
                } else if ((mGrid.verValuesAlign & TextAlign.HORIZONTAL_CENTER) > 0) {
                    mGrid.mainVerValuesPaint.setTextAlign(Align.CENTER);
                    txtX = (getWidth() - mGrid.verValuesMarginLeft - mGrid.verValuesMarginRight) / 2;
                }
                canvas.drawText(string, txtX, txtY, mGrid.mainVerValuesPaint);
            }
        }
    }

    private float downX;
    private float downY;

    /*
     * (non-Javadoc)
     * 
     * @see android.view.View#onTouchEvent(android.view.MotionEvent)
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            mLastX = x;
            mLastY = y;
            downX = x;
            downY = y;
            break;
        case MotionEvent.ACTION_MOVE:
            velocityX = (mLastX - x) / mScaleX;
            velocityY = (mLastY - y) / mScaleY;
            moveViewPort(velocityX, velocityY);
            mLastX = x;
            mLastY = y;
            break;
        case MotionEvent.ACTION_UP:
            if (Math.abs(downX - x) < mTouchSlop && Math.abs(downY - y) < mTouchSlop) {
                if (mListener != null) findClickedVerticle(x, y);
            } else {
                frictionX = -Math.signum(velocityX) * Math.abs(mViewPortRight - mViewPortLeft) / 1000 * mFrictionForceX;
                frictionY = -Math.signum(velocityY) * Math.abs(mViewPortTop - mViewPortBottom) / 1000 * mFrictionForceY;
                isMovingX = isMovingY = true;
            }
            break;
        }
        postInvalidate();
        return true;
    }

    private void findClickedVerticle(float x, float y) {
        if (x < mViewPortMarginLeft || x > getWidth() - mViewPortMarginRight
            || y > getHeight() - mViewPortMarginBottom
            || y < mViewPortMarginTop) {
            return;
        }

        float gX = (x - mViewPortMarginLeft) / mScaleX + mViewPortLeft;
        float gY = (getHeight() - y - mViewPortMarginBottom) / mScaleY + mViewPortBottom;

        Log.i("", gX + "," + gY);
        LinePoint point = null;
        Line line = null;
        float minR = Float.MAX_VALUE;

        for (Line l : mLines)
            for (LinePoint p : l.getPoints()) {
                if (gX >= p.getX() - getPointClickRadius() / mScaleX && gX <= p.getX() + getPointClickRadius()
                                                                              / mScaleX
                    && gY >= p.getY() - getPointClickRadius() / mScaleY
                    && gY <= p.getY() + getPointClickRadius() / mScaleY) {
                    float r = (float) Math.sqrt((gX - p.getX()) * (gX - p.getX()) + (gY - p.getY()) * (gY - p.getY()));
                    if (minR > r) {
                        minR = r;
                        point = p;
                        line = l;
                    }
                }
            }
        if (mListener != null && point != null) mListener.onPointClick(point, line);
    }

    /**
     * Move ViewPort.
     * 
     * @param deltaX
     *            horizontal delta
     * @param deltaY
     *            vertical delta
     */
    public void moveViewPort(float deltaX, float deltaY) {
        if (mViewPortLeft + deltaX > mMinX - mViewPortHorFreedom && mViewPortRight + deltaX < mMaxX + mViewPortHorFreedom) {
            mViewPortLeft += deltaX;
            mViewPortRight += deltaX;
        } else if (deltaX > 0) {
            mViewPortLeft += mMaxX + mViewPortHorFreedom - mViewPortRight;
            mViewPortRight = mMaxX + mViewPortHorFreedom;
            isMovingX = false;
        } else if (deltaX < 0) {
            mViewPortRight += mMinX - mViewPortHorFreedom - mViewPortLeft;
            mViewPortLeft = mMinX - mViewPortHorFreedom;
            isMovingX = false;
        }

        if (mViewPortBottom - deltaY > mMinY - mViewPortVerFreedom && mViewPortTop - deltaY < mMaxY + mViewPortVerFreedom) {
            mViewPortBottom -= deltaY;
            mViewPortTop -= deltaY;
        } else if (deltaY < 0) {
            mViewPortBottom -= mMaxY + mViewPortVerFreedom - mViewPortTop;
            mViewPortTop = mMaxY + mViewPortVerFreedom;
            isMovingY = false;
        } else if (deltaY < 0) {
            mViewPortTop -= mMinY - mViewPortVerFreedom - mViewPortBottom;
            mViewPortBottom = mMinY - mViewPortVerFreedom;
            isMovingY = false;
        }
    }

    private void inertionMove() {
        if (isMovingX || isMovingY) {
            if (isMovingX) {
                int signX = (int) Math.signum(velocityX);
                velocityX += frictionX;
                if (signX == (int) Math.signum(velocityX)) moveViewPort(velocityX, 0);
                else isMovingX = false;
            }
            if (isMovingY) {
                int signY = (int) Math.signum(velocityY);
                velocityY += frictionY;
                if (signY == (int) Math.signum(velocityY)) moveViewPort(0, velocityY);
                else isMovingY = false;
            }
            postInvalidate();
        }

    }

    /**
     * Sets the listener for chart point click.
     * 
     * @param listener
     *            new on point click listener
     */
    public void setOnPointClickListener(OnChartPointClickListener listener) {
        this.mListener = listener;
    }

    /**
     * Gets point click radius.
     * 
     * @return point click radius
     */
    public float getPointClickRadius() {
        return pointClickRadius;
    }

    /**
     * Sets the point click radius.
     * 
     * @param pointClickRadius
     *            new point click radius
     */
    public void setPointClickRadius(float pointClickRadius) {
        this.pointClickRadius = pointClickRadius;
    }
}
