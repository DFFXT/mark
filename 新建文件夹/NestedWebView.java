package com.pwrd.dls.marble.moudle.monument.monumentGuide.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
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

    private int mNestedYOffset=0;

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

    private int id(MotionEvent event){
        return event.getPointerId(event.getActionIndex());
    }

    /**
     * 在没有发生位移时,event 正常下发，在产生位移时阻止下发，直到位移为0；在没有位移和有位移之间创建一个中间even下发，避免卡顿
     * */
    private int sy=0;
    private SparseArray<Float> pointer=new SparseArray<>();
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = false;
        final int action = MotionEventCompat.getActionMasked(event);

        if (action == MotionEvent.ACTION_DOWN) {
            scroller.forceFinished(true);
        }
        event.offsetLocation(0,getTop());
        tracker.addMovement(event);
        switch (action) {
            case MotionEvent.ACTION_POINTER_DOWN:{
                pointer.put(id(event),event.getY(event.getActionIndex()));
                if(getTop()!=0){
                    return true;
                }
                result= super.onTouchEvent(event);
            }
            case MotionEvent.ACTION_DOWN:
                pointer.put(id(event),event.getY());
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                result = super.onTouchEvent(event);
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaY =0;
                for(int i=0;i<event.getPointerCount();i++){
                    deltaY+=pointer.get(event.getPointerId(i))-event.getY(i);
                    pointer.put(event.getPointerId(i),event.getY(i));
                }
                deltaY/=event.getPointerCount();
                int preTop=getTop();
                if (dispatchNestedPreScroll(0, deltaY, mScrollConsumed, mScrollOffset)) { }
                sy=getScrollY();
                if(getTop()!=0){//**产生了位移
                    if(preTop*getTop()<=0){//异号
                       /* MotionEvent ex=MotionEvent.obtain(event.getDownTime(),event.getEventTime(),event.getAction(),event.getX(),event.getY()+preTop,0);
                        super.onTouchEvent(ex);
                        ex.recycle();*/

                    }
                    return true;
                }
                if(preTop<0&&getTop()>=0){
                    MotionEvent ex=MotionEvent.obtain(event.getDownTime(),event.getEventTime(),MotionEvent.ACTION_DOWN,event.getX(),event.getY()-preTop,0);
                    super.onTouchEvent(ex);
                    ex.recycle();
                     /*ex=MotionEvent.obtain(event.getDownTime(),event.getEventTime(),event.getAction(),event.getX(),event.getY(),0);
                        super.onTouchEvent(ex);
                        ex.recycle();*/
                    return true;
                }
                MotionEvent e=MotionEvent.obtain(event.getDownTime(),event.getEventTime(),event.getAction(),event.getX(),event.getY(),0);
                result = super.onTouchEvent(event);
                e.recycle();
                return true;
            case MotionEvent.ACTION_POINTER_UP:
                pointer.remove(id(event));
                if(getTop()!=0){
                    return true;
                }
                result= super.onTouchEvent(event);
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                pointer.remove(id(event));
                //stopNestedScroll();
                tracker.computeCurrentVelocity(1000, ViewConfiguration.get(getContext()).getScaledMaximumFlingVelocity());
                float vy=tracker.getYVelocity();
                if(Math.abs(vy)>ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity()){
                    mChildHelper.stopNestedScroll(ViewCompat.TYPE_TOUCH);
                    mChildHelper.dispatchNestedPreFling(0,vy);
                    mChildHelper.startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL,ViewCompat.TYPE_NON_TOUCH);
                    setScrollY(sy);
                    scroller.fling(0,getScrollY(),0,-(int)vy,0,0,Integer.MIN_VALUE,Integer.MAX_VALUE);
                    preSY=getScrollY();
                    invalidate();
                    return true;
                }else{
                    mChildHelper.stopNestedScroll(ViewCompat.TYPE_TOUCH);
                }
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
            int dy=y-preSY;
            preSY=y;
            mScrollConsumed[1]=0;
            mScrollOffset[1]=0;
            mChildHelper.dispatchNestedPreScroll(0, dy,mScrollConsumed,mScrollOffset,ViewCompat.TYPE_NON_TOUCH);


           // setScrollY((getScrollY()+dy-mScrollConsumed[1]+mScrollOffset[1]));
            setScrollY((int) Math.min(Math.max(0,getScrollY()+dy-mScrollConsumed[1]),getContentHeight()*getScale()-getHeight()));

            invalidate();
        }
    }

    @Override
    public boolean canScrollVertically(int direction) {

        if(direction<0){//上滑
            return Math.abs(getContentHeight()*getScale()-getHeight()-getScrollY())>8;
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
