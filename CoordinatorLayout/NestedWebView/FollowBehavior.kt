package com.pwrd.dls.marble.moudle.monument.monumentGuide.behavior

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.pwrd.dls.marble.R
import com.pwrd.dls.marble.common.view.getFlingTotalDistance
import java.lang.Math.abs

/**
 * 跟随Behavior 底部跟随dependencyId
 */
class FollowBehavior(context: Context?, attrs: AttributeSet?) : CoordinatorLayout.Behavior<View>(context, attrs) {


    private val dependencyId = R.id.webview
    var minTop = 0
    private var maxTop = 0
    var initMinTop = 0
    var footerHeight = 300
    var dragHeight = 200
    private lateinit var dependency: View
    private var animator: ValueAnimator = ValueAnimator()
    private lateinit var child: View

    /*init {
        animator.duration = 500
        animator.addUpdateListener {
            paddingBottomDelta(child, -child.paddingBottom + it.animatedValue as Int)
        }
    }*/

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        if (dependency.id == dependencyId) {
            this.child = child
            this.dependency = dependency
            return true
        }
        return false
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        this.dependency=dependency
        child.layout(0,dependency.bottom,child.measuredWidth,dependency.bottom+child.measuredHeight)
        return false
    }

    private var layout=false
    override fun onLayoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int): Boolean {
        this.child = child
        if(!layout){
            minTop=0
            maxTop=parent.measuredHeight
            initMinTop=0
            layout=true
            child.layout(0,parent.measuredHeight,parent.measuredWidth,parent.measuredHeight+parent.measuredHeight)
        }else{
            child.layout(0,dependency.bottom,child.measuredWidth,dependency.bottom+child.measuredHeight)
        }

        return true
    }
/*
    *//**
     * 下滑 dy<0
     *//*
    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: View, target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        if(coordinatorLayout!=child)return
        if (type == ViewCompat.TYPE_TOUCH) {
            needAnimator = true
            animator.cancel()
        }
        if (type == ViewCompat.TYPE_TOUCH || -child.paddingBottom < initMinTop - footerHeight) {
            minTop = initMinTop - dragHeight - footerHeight
        } else {
            minTop = initMinTop - footerHeight
        }
        if (type == ViewCompat.TYPE_NON_TOUCH && -child.paddingBottom <= minTop || animator.isRunning) {
            consumed[1] = dy
            return
        }
        val y = -dy
        var c=0
        when {
            -child.paddingBottom + y < minTop -> {
                c=minTop - -child.paddingBottom
                Log.i("logInfo","rote1  ${-child.paddingBottom} $minTop")
            }
            -child.paddingBottom + y > maxTop -> {
                c=maxTop - -child.paddingBottom
                Log.i("logInfo","rote2 ${-child.paddingBottom} $maxTop")
            }
            else -> c=y
        }


        *//*   if (c > 0 && -child.paddingBottom == minTop && target.canScrollVertically(-1)) {//在顶部下拉时谁先消费滑动
               //这里如果什么都不做，内容下滑到顶部就终止,需要再次下拉才能带动child下移
               if (c > target.scrollY) {
                   ViewCompat.offsetTopAndBottom(child, c - target.scrollY)
                   consumed[1] = -c + target.scrollY
               }

           } else if (*//**//*autoStopWhenChildFlingToTop &&*//**//* c <= 0 && type == ViewCompat.TYPE_NON_TOUCH && -child.paddingBottom + c == minTop && target.scrollY == 0) {//上滑，child到达顶部是是否阻止内部的惯性滑动
            consumed[1] = dy
            ViewCompat.offsetTopAndBottom(child, c)
        } else {
            ViewCompat.offsetTopAndBottom(child, c)
            consumed[1] = -c
        }*//*

        //本身不能往上滑才消耗
        if (c < 0 && !target.canScrollVertically(-1)) {//target本身不可以上滑
            paddingBottomDelta(child, c)
            consumed[1] = -c
        }
        *//*else if(c>0){//下滑
            if(-child.paddingBottom>minTop){//需要滑动自身

            }
        }*//*
        else if (c > 0) {
            paddingBottomDelta(child, c)
            consumed[1] = -c
        }
        Log.i("logInfo", "--->$dy $c ${consumed[1]} $type  ${target.canScrollVertically(-1)}")
    }
    private fun paddingBottomDelta(child: View,delta:Int){
        child.setPaddingRelative(0,0,0,child.paddingBottom-delta)

    }

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: View, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int) {
        if (type == ViewCompat.TYPE_TOUCH) {
            //needAnimator = true
            //animator.cancel()
        }
        if (type == ViewCompat.TYPE_TOUCH || -child.paddingBottom > initMinTop) {
            minTop = initMinTop - dragHeight
        } else {
            minTop = initMinTop
        }
        if (type == ViewCompat.TYPE_NON_TOUCH && -child.paddingBottom > initMinTop *//*&& animator.isRunning*//*) {
            //consumed[1] = dy
            return
        }
        val y = -dyConsumed
        child.paddingBottom
        val c = when {
            -child.paddingBottom + y < minTop -> minTop - -child.paddingBottom
            -child.paddingBottom + y > maxTop -> maxTop - -child.paddingBottom
            else -> y
        }

        Log.i("logInfo", "nestedScroll===$dyConsumed $dyUnconsumed  $type")
    }

    private var needAnimator = false
    private fun rec(child: View) {

        if (-child.paddingBottom >= initMinTop - footerHeight || !needAnimator) return
        if (animator.isRunning) {
            animator.cancel()
        }
        animator.setIntValues(-child.paddingBottom, initMinTop - footerHeight)
        animator.start()
    }

    override fun onStopNestedScroll(coordinatorLayout: CoordinatorLayout, child: View, target: View, type: Int) {
        rec(child)
    }

    override fun onNestedPreFling(coordinatorLayout: CoordinatorLayout, child: View, target: View, velocityX: Float, velocityY: Float): Boolean {
        //在drag状态，往上fling需要停止恢复动画，但需要确保向上的fling能够代替恢复动画
        //vy>0 上滑fling
        //onNestedPreFling 先于 onStopNestedScroll被调用
        //needAnimator = gap > getFlingTotalDistance(child.context, velocityY)
        needAnimator = !(velocityY > 0 && getFlingTotalDistance(child.context, velocityY) >= abs(-child.paddingBottom - initMinTop))
        return super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY)
    }

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: View, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
        return true
    }*/

}