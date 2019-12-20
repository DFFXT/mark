package com.pwrd.dls.marble.moudle.monument.monumentGuide.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.webkit.WebView;
import android.widget.Scroller;

import androidx.core.view.MotionEventCompat;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.ViewCompat;

/**
 * 适用于两个View  在滑动上 就好像一个View，不管滑动哪个都可以联动，和SmartRefreshLayout#noMoreData=true时的RecyclerView和Footer的行为一样
 * 必须配合behavior使用
 * <p>
 * 禁用多手指触控可以避免很多很多bug
 */
public class NestedWebView extends /*ProgressWebView*/ WebView {

    public static final String TAG = "logInfo";


    private final int[] mScrollOffset = new int[2];
    private final int[] mScrollConsumed = new int[2];

    private int preScroll = 0;
    private int downTop = 0;
    private boolean dropEvent = false;
    private SparseArray<Float> pointer = new SparseArray<>();

    private NestedScrollingChildHelper mChildHelper;
    private VelocityTracker tracker;
    private Scroller scroller;
    private NestedWebView nextWebView;
    public boolean disPatchFling = false;
    public boolean onTouch = false;


    public NestedWebView(Context context) {
        super(context);
        init();
    }

    public NestedWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
        tracker = VelocityTracker.obtain();
        scroller = new Scroller(getContext());
    }

    /**
     * 互相指定
     *
     * @param nextWebView   另一个webView
     * @param disPatchFling 是否分发自己未消耗的fling
     */
    public void config(NestedWebView nextWebView, Boolean disPatchFling) {
        this.nextWebView = nextWebView;
        this.disPatchFling = disPatchFling;
    }

    private int id(MotionEvent event) {
        return event.getPointerId(event.getActionIndex());
    }

    /**
     * 在没有发生位移时,event 正常下发，在产生位移时阻止下发，直到位移为0；在没有位移和有位移之间创建一个中间even下发，避免卡顿
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = false;
        final int action = MotionEventCompat.getActionMasked(event);

        if (action == MotionEvent.ACTION_DOWN) {
            scroller.forceFinished(true);
        }
        //event.offsetLocation(0,getTop());
        tracker.addMovement(event);
        switch (action) {
            case MotionEvent.ACTION_POINTER_DOWN: {
                event.offsetLocation(0, getTop() - downTop);
                pointer.put(id(event), event.getY(event.getActionIndex()));
                if (downTop == 0) return super.onTouchEvent(event);
                return true;

            }
            case MotionEvent.ACTION_DOWN:
                onTouch = true;
                if (nextWebView != null) {
                    nextWebView.stopFling();
                }
                pointer.put(id(event), event.getY());
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                downTop = getTop();
                result = super.onTouchEvent(event);
                break;
            case MotionEvent.ACTION_MOVE:
                if (dropEvent) {
                    dropEvent = false;
                    return true;
                }
                event.offsetLocation(0, getTop() - downTop);
                int deltaY = 0;
                for (int i = 0; i < event.getPointerCount(); i++) {
                    deltaY += pointer.get(event.getPointerId(i)) - event.getY(i);
                    pointer.put(event.getPointerId(i), event.getY(i));

                }
                deltaY /= event.getPointerCount();
                int preTop = getTop();
                if (dispatchNestedPreScroll(0, deltaY, mScrollConsumed, mScrollOffset)) {
                }
                if (preTop < 0 && getTop() >= 0) {
                    MotionEvent ex = MotionEvent.obtain(event.getDownTime(), event.getEventTime(), MotionEvent.ACTION_DOWN, event.getX(), event.getY() - preTop, 0);
                    super.onTouchEvent(ex);
                    ex.recycle();
                    return true;
                }
                if (getTop() != 0) {//**产生了位移
                    if (preTop * getTop() <= 0) {//异号
                    }
                    return true;
                }

                MotionEvent e = MotionEvent.obtain(event.getDownTime(), event.getEventTime(), event.getAction(), event.getX(), event.getY(), 0);
                if (downTop != 0) {
                    super.onTouchEvent(e);
                } else {
                    super.onTouchEvent(event);
                }
                e.recycle();
                return true;
            case MotionEvent.ACTION_POINTER_UP:
                pointer.remove(id(event));
                if (getTop() != 0) {
                    return true;
                }
                if (downTop != 0) return true;
                //result= super.onTouchEvent(event);
                return super.onTouchEvent(event);
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                onTouch = false;
                downTop = 0;
                pointer.remove(id(event));
                tracker.computeCurrentVelocity(1000, ViewConfiguration.get(getContext()).getScaledMaximumFlingVelocity());
                float vy = tracker.getYVelocity();
                if (Math.abs(vy) > ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity()) {
                    if (nextWebView == null || !nextWebView.onTouch) {
                        mChildHelper.stopNestedScroll(ViewCompat.TYPE_TOUCH);
                    }

                    mChildHelper.dispatchNestedPreFling(0, vy);
                    mChildHelper.startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_NON_TOUCH);
                    scroller.fling(0, getScrollY(), 0, -(int) vy, 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
                    preScroll = getScrollY();
                    invalidate();
                    return true;
                } else {
                    if (nextWebView == null || !nextWebView.onTouch) {
                        mChildHelper.stopNestedScroll(ViewCompat.TYPE_TOUCH);
                    }
                }
                result = super.onTouchEvent(event);
                //result = true;
                break;
        }
        return result;
    }


    //region fling
    //分发了fling，接收fling的View不会再次分发
    public void fling(float dx, float dy) {
        consumeFling(0, dy);
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            int y = scroller.getCurrY();
            int dy = y - preScroll;
            preScroll = y;
            float consumeY = consumeFling(0, dy);
            if (disPatchFling && Math.abs(consumeY) < Math.abs(dy) && nextWebView != null) {
                nextWebView.fling(0, dy - consumeY);
            }
            invalidate();
        }
    }

    //只写了consumedY
    private float consumeFling(float dx, float dy) {
        mScrollConsumed[1] = 0;
        mScrollOffset[1] = 0;
        mChildHelper.dispatchNestedPreScroll(0, (int) dy, mScrollConsumed, mScrollOffset, ViewCompat.TYPE_NON_TOUCH);
        int preScrollY = getScrollY();
        setScrollY((int) Math.min(Math.max(0, getScrollY() + dy - mScrollConsumed[1]), getContentHeight() * getScale() - getHeight()));
        return mScrollConsumed[1] + getScrollY() - preScrollY;
    }

    public void stopFling() {
        scroller.forceFinished(true);
    }

    @Override
    public boolean canScrollVertically(int direction) {
        if (direction < 0) {//上滑
            return Math.abs(getContentHeight() * getScale() - getHeight() - getScrollY()) > 8;
        } else if (direction > 0) {
            return getScrollY() != 0;
        }
        return super.canScrollVertically(direction);
    }
    //endregion

    //region nested
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

    //endregion
}
