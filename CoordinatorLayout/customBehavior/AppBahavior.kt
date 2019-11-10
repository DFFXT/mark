package com.example.myapplication.tools

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Scroller
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout
import kotlin.math.max

class AppBahavior(context: Context?, attrs: AttributeSet?) : AppBarLayout.Behavior(context, attrs) {

    override fun onStartNestedScroll(
        parent: CoordinatorLayout,
        child: AppBarLayout,
        directTargetChild: View,
        target: View,
        nestedScrollAxes: Int,
        type: Int
    ): Boolean {

        return true
        /*if(child.bottom>0) return true
        else return !(target.canScrollVertically(-1))*/
        //return !target.canScrollVertically(nestedScrollAxes)
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: AppBarLayout,
        dependency: View
    ): Boolean {
        log("app->change-->"+System.currentTimeMillis())
        return super.onDependentViewChanged(parent, child, dependency)
    }

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: AppBarLayout,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {

        val y=-dyUnconsumed
        val c=if(child.top+y>0){
            -child.top
        }else if(child.bottom+y<0){
            -child.bottom
        }else{
            y
        }
        if(child.bottom>0||(!target.canScrollVertically(-1))){
            child.layout(0,child.top+c,child.measuredWidth,child.bottom+c)
            consumed[1]=-c
        }

        log("app->onNestedScroll-->$dyConsumed $dyUnconsumed ${consumed[1]} ${dy>0} ${(dy>0&&!target.canScrollVertically(-1))}")

    }



    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: AppBarLayout,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        val y=-dy
        val c=if(child.top+y>0){
            -child.top
        }else if(child.bottom+y<0){
            -child.bottom
        }else{
            y
        }
        if(child.bottom>0||(y>0&&!target.canScrollVertically(1))){
            child.layout(0,child.top+c,child.measuredWidth,child.bottom+c)
            log("app->onNestedPreScroll-->$dy ${-c} ${child.top}")
            consumed[1]=-c
        }


       // super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
    }

    private var scroller:Scroller?=null
    override fun onNestedPreFling(
        coordinatorLayout: CoordinatorLayout,
        child: AppBarLayout,
        target: View,
        velocityX: Float,
        velocityY: Float
    ): Boolean {

        /*scroller=scroller?: Scroller(child.context)

        scroller!!.fling(0,0, velocityX.toInt(), velocityY.toInt(),0,0, Int.MIN_VALUE, Int.MAX_VALUE)
        scroller!!.computeScrollOffset()
        onNestedPreScroll(coordinatorLayout,child,target,0,scroller!!.currY,)
        child.viewTreeObserver.addOnGlobalLayoutListener (object :ViewTreeObserver.OnGlobalLayoutListener{
            override fun onGlobalLayout() {

                child.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
        log("app->onNestedPreFling-->$velocityY")*/
        return false
    }

    override fun onNestedFling(
        coordinatorLayout: CoordinatorLayout,
        child: AppBarLayout,
        target: View,
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean
    ): Boolean {
        log("app->onNestedFling-->$velocityY $consumed")

        return true
    }


    private var preY=-1f
    private var currY=-1f
    private var dy=0f
    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: AppBarLayout,
        ev: MotionEvent
    ): Boolean {
        if(ev.action==MotionEvent.ACTION_DOWN){
            preY=ev.y
        }else if(ev.action==MotionEvent.ACTION_MOVE){
            dy=ev.y-preY
            log(">>>>>${ev.y}  $preY")
            preY=ev.y
        }
        return super.onInterceptTouchEvent(parent, child, ev)
    }
}