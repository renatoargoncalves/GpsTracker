package com.sow.gpstrackerpro.classes;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

/**
 * Created by renato on 17/04/2017.
 */

public class BubbleDrawable extends Drawable {

    private Paint mPaint;
    private int mColor;

    private RectF mBoxRect;
    private int mBoxWidth;
    private int mBoxHeight;
    private int mBoxBottom;
    private float mCornerRad;
    private Rect mBoxPadding = new Rect();

    private Path mPointer;
    private int mPointerWidth;
    private int mPointerHeight;
    private int mPointerAlignment;

    // Constructors
    ////////////////////////////////////////////////////////////

    public BubbleDrawable(int pointerAlignment) {
        setPointerAlignment(pointerAlignment);
        initBubble();
    }

    // Setters
    ////////////////////////////////////////////////////////////

    public void setPadding(int left, int top, int right, int bottom) {
        mBoxPadding.left = left;
        mBoxPadding.top = top;
        mBoxPadding.right = right;
        mBoxPadding.bottom = bottom;
    }

    public void setCornerRadius(float cornerRad) {
        mCornerRad = cornerRad;
    }

    public void setPointerAlignment(int pointerAlignment) {
            mPointerAlignment = pointerAlignment;
    }

    public void setPointerWidth(int pointerWidth) {
        mPointerWidth = pointerWidth;
    }

    public void setPointerHeight(int pointerHeight) {
        mPointerHeight = pointerHeight;
    }

    // Private Methods
    ////////////////////////////////////////////////////////////

    private void initBubble() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(0xFF2196F3);
        mPaint.setStyle(Paint.Style.FILL);
        mCornerRad = 0;
        setPointerWidth(40);
        setPointerHeight(40);
    }

    private void updatePointerPath() {
        mPointer = new Path();
        mPointer.setFillType(Path.FillType.EVEN_ODD);
        // Set the starting point
        mPointer.moveTo(mPointerAlignment+20, 40);

        // Define the lines
        mPointer.rLineTo(0, 0);
        mPointer.rLineTo(-(mPointerWidth / 2), -mPointerHeight);
        mPointer.rLineTo(-(mPointerWidth / 2), mPointerHeight);
        mPointer.close();
    }


    // Superclass Override Methods
    ////////////////////////////////////////////////////////////

    @Override
    public void draw(Canvas canvas) {
        mBoxRect = new RectF(20.0f, 40.0f, mBoxWidth-20, mBoxHeight);
        canvas.drawRoundRect(mBoxRect, mCornerRad, mCornerRad, mPaint);
        updatePointerPath();
        canvas.drawPath(mPointer, mPaint);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setAlpha(int alpha) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean getPadding(Rect padding) {
        padding.set(mBoxPadding);

        // Adjust the padding to include the height of the pointer
        padding.bottom += mPointerHeight;
        return true;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        mBoxWidth = bounds.width();
        mBoxHeight = getBounds().height() - mPointerHeight;
        mBoxBottom = getBounds().bottom;
        super.onBoundsChange(bounds);
    }
}