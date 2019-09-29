package com.pwrd.dls.marble.common.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

public class RoundImageView2 extends AppCompatImageView {
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private PorterDuffXfermode mode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);

    public RoundImageView2(Context context) {
        super(context);
    }

    public RoundImageView2(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundImageView2(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 圆角ImageView，支持padding scaleType
     * saveLayer会新建一个图层,后续操作会在新图层上进行
     * @param canvas canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();
        if (drawable == null) return;
        int width;
        int height;
        if (getCropToPadding()) {
            int left = getScrollX() + getPaddingStart();
            int top = getScrollY() + getPaddingTop();
            int right = getScrollX() + getRight() - getLeft() - getPaddingEnd();
            int bottom = getScrollY() + getBottom() - getTop() - getPaddingBottom();
            canvas.clipRect(left, top, right, bottom);
            width = right - left;
            height = bottom - top;
        } else {
            width = getWidth() - getPaddingEnd() - getPaddingStart();
            height = getHeight() - getPaddingBottom() - getPaddingTop();
            canvas.clipRect(getPaddingStart(), getPaddingTop(), getWidth() - getPaddingEnd(), getHeight() - getPaddingBottom());
        }
        canvas.translate(getPaddingStart(), getPaddingTop());
        canvas.saveLayer(0, 0, width, height, mPaint);//园图层
        canvas.drawRoundRect(0, 0, width, height, width / 2f, height / 2f, mPaint);
        mPaint.setXfermode(mode);
        canvas.saveLayer(0, 0, width, height, mPaint);//图片图层，设置paint mode
        canvas.concat(getImageMatrix());
        drawable.draw(canvas);
        canvas.restore();//根据paint规则图层重叠
        mPaint.setXfermode(null);
    }
}

