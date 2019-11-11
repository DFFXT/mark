package com.example.myapplication.behavior

import android.animation.ValueAnimator
import android.content.Context
import android.hardware.SensorManager
import android.util.AttributeSet
import android.view.View
import android.view.ViewConfiguration
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln

/**
 * 类似于BottomSheetBehavior
 *
 */
class SelfBehavior(context: Context?, attrs: AttributeSet?) :
    CoordinatorLayout.Behavior<View>(context, attrs) {


    private val minTop = 0
    private var maxTop = 1000
    private val initMaxTop = 1000
    private val dragHeight = 200
    private var animator: ValueAnimator = ValueAnimator()
    private lateinit var child: View
    private var needAnimator = true

    private var autoStopWhenChildFlingToTop = true

    init {
        animator.duration = 500
        animator.addUpdateListener {
            ViewCompat.offsetTopAndBottom(child, -child.top + it.animatedValue as Int)
        }
    }


    override fun onLayoutChild(
        parent: CoordinatorLayout,
        child: View,
        layoutDirection: Int
    ): Boolean {
        this.child = child
        child.layout(0, 400, child.measuredWidth, child.measuredHeight + 400)
        return true
    }

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        return child == dependency
    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        if (type == ViewCompat.TYPE_TOUCH) {
            needAnimator = true
            animator.cancel()
        }
        if (type == ViewCompat.TYPE_TOUCH || child.top > initMaxTop) {
            maxTop = initMaxTop + dragHeight
        } else {
            maxTop = initMaxTop
        }
        if (type == ViewCompat.TYPE_NON_TOUCH && child.top > initMaxTop && animator.isRunning) {
            consumed[1] = dy
            return
        }
        val y = -dy
        val c = when {
            child.top + y < minTop -> minTop - child.top
            child.top + y > maxTop -> maxTop - child.top
            else -> y
        }

        val top = target.canScrollVertically(1)
        val down = target.canScrollVertically(-1)
        log("--->$y  ${child.top} $c $maxTop $top $down")
        if (c > 0 && child.top == minTop && target.canScrollVertically(-1)) {//在顶部下拉时谁先消费滑动
            //这里如果什么都不做，内容下滑到顶部就终止
            if (c > target.scrollY) {
                ViewCompat.offsetTopAndBottom(child, c - target.scrollY)
                consumed[1] = -c + target.scrollY
            }

        } else if (autoStopWhenChildFlingToTop && c <= 0 && type == ViewCompat.TYPE_NON_TOUCH && child.top + c == minTop && target.scrollY == 0) {//上滑，child到达顶部是是否阻止内部的惯性滑动
            consumed[1] = dy
            ViewCompat.offsetTopAndBottom(child, c)
        } else {
            ViewCompat.offsetTopAndBottom(child, c)
            consumed[1] = -c
        }


    }

    private fun rec(child: View) {
        if (child.top <= initMaxTop || !needAnimator) return
        if (animator.isRunning) {
            animator.cancel()
        }
        animator.setIntValues(child.top, initMaxTop)
        animator.start()
    }

    override fun onStopNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        type: Int
    ) {
        log("rec")
        rec(child)
    }

    override fun onNestedPreFling(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        //在drag状态，往上fling需要停止恢复动画，但需要确保向上的fling能够代替恢复动画
        //vy>0 上滑fling
        //onNestedPreFling 先于 onStopNestedScroll被调用
        //needAnimator = dragHeight > getFlingTotalDistance(child.context, velocityY)
        needAnimator = velocityY < 300
        return super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY)
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        return true
    }

    //**Scroller里面的，似乎不准？
    private fun getFlingTotalDistance(context: Context, velocity: Float): Double {
        val mPpi = context.resources.displayMetrics.density * 160.0f
        val mFlingFriction = ViewConfiguration.getScrollFriction()
        val mPhysicalCoeff = SensorManager.GRAVITY_EARTH * 39.37f * mPpi * 0.84
        val DECELERATION_RATE = ln(0.78) / ln(0.9)
        val l = ln((0.35 * abs(velocity) / (mFlingFriction * mPhysicalCoeff)))
        val decelMinusOne = (DECELERATION_RATE).toFloat() - 1.0
        return mFlingFriction.toDouble() * mPhysicalCoeff * exp(DECELERATION_RATE / decelMinusOne * l)
    }
}