package com.example.myapplication.behavior;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.webkit.WebView;
import android.widget.Scroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MotionEventCompat;
import androidx.core.view.NestedScrollingChild3;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.ViewCompat;

public class NestedWebView extends WebView implements NestedScrollingChild3 {

    public static final String TAG = "logInfo";

    private int mLastMotionY;

    private final int[] mScrollOffset = new int[2];
    private final int[] mScrollConsumed = new int[2];

    private int mNestedYOffset;

    private NestedScrollingChildHelper mChildHelper;


    private VelocityTracker tracker;
    private Scroller scroller;


    public NestedWebView(Context context) {
        super(context);
        init();
    }

    public NestedWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NestedWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
        tracker=VelocityTracker.obtain();
        scroller=new Scroller(getContext());
    }

    private int sy=0;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = false;

        MotionEvent trackedEvent = MotionEvent.obtain(event);

        final int action = MotionEventCompat.getActionMasked(event);

        if (action == MotionEvent.ACTION_DOWN) {
            mNestedYOffset = 0;
            scroller.forceFinished(true);
        }

        int y = (int) event.getY();



        event.offsetLocation(0, mNestedYOffset);
        tracker.addMovement(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionY = y;
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                result = super.onTouchEvent(event);
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaY = mLastMotionY - y;

                if (dispatchNestedPreScroll(0, deltaY, mScrollConsumed, mScrollOffset)) {
                    deltaY -= mScrollConsumed[1];
                    trackedEvent.offsetLocation(0, -mScrollOffset[1]);
                    mNestedYOffset += mScrollOffset[1];
                }



                trackedEvent.recycle();
                //Log.i("log","--->"+ trackedEvent.getY()+" "+mScrollConsumed[1]+" "+mScrollOffset[1]);
                Log.i("log","scrollY--->"+ getScrollY()+" "+getContentHeight());

                sy=getScrollY();
                result = super.onTouchEvent(trackedEvent);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                //stopNestedScroll();
                tracker.computeCurrentVelocity(1000, ViewConfiguration.get(getContext()).getScaledMaximumFlingVelocity());
                float vy=tracker.getYVelocity();
                mChildHelper.dispatchNestedPreFling(0,vy);
                Log.i(TAG, "onTouchEvent: cancel "+vy +" "+getScrollY());
                setScrollY(sy);
                scroller.fling(0,getScrollY(),0,-(int)vy,0,0,Integer.MIN_VALUE,Integer.MAX_VALUE);
                preSY=getScrollY();
                invalidate();
                result = super.onTouchEvent(event);
                //result = true;
                break;
        }
        return result;
    }

    private int preSY=0;

    @Override
    public void computeScroll() {
        if(scroller.computeScrollOffset()){
            int y=scroller.getCurrY();
            int dy=-y+preSY;
            preSY=y;
            mScrollConsumed[1]=0;
            mScrollOffset[1]=0;
            mChildHelper.dispatchNestedPreScroll(0, -dy,mScrollOffset,mScrollConsumed);
            Log.i(TAG, "computeScroll: "+dy+" "+mScrollConsumed[1]+" "+mScrollOffset[1]);

           // setScrollY((getScrollY()+dy-mScrollConsumed[1]+mScrollOffset[1]));
            setScrollY((int) Math.min(Math.max(0,getScrollY()-dy+mScrollConsumed[1]),getContentHeight()*getScale()-getHeight()));

            Log.i(TAG, "computeScroll: end->"+getScrollY());
            invalidate();
        }
    }

    @Override
    public boolean canScrollVertically(int direction) {

        if(direction<0){//上滑

        }else if(direction>0){
            return getScrollY()!=0;
        }

        return super.canScrollVertically(direction);
    }

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        //mChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }




    @Override
    public void dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow, int type, @NonNull int[] consumed) {

    }

    @Override
    public boolean startNestedScroll(int axes, int type) {
        Log.i(TAG, "startNestedScroll: ");
        return false;
    }

    @Override
    public void stopNestedScroll(int type) {

    }

    @Override
    public boolean hasNestedScrollingParent(int type) {
        return false;
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow, int type) {
        Log.i(TAG, "dispatchNestedScroll: "+dyConsumed+" "+dyUnconsumed +" "+offsetInWindow[1]);
        return false;
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed, @Nullable int[] offsetInWindow, int type) {
        Log.i(TAG, "dispatchNestedPreScroll: "+dy+" "+consumed[1]+" "+offsetInWindow[1]);
        return false;
    }
}
