package com.pwrd.dls.marble.moudle.monument.monumentGuide.behavior

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.pwrd.dls.marble.common.view.getFlingTotalDistance

/**
 * 类似于BottomSheetBehavior
 *
 */
class SelfMoveBehavior(context: Context?, attrs: AttributeSet?) :
        CoordinatorLayout.Behavior<View>(context, attrs) {


    var minTop = 0
    private var maxTop = 1000
    var initMaxTop = 1000
    var dragHeight = 200
    private var animator: ValueAnimator = ValueAnimator()
    private lateinit var child: View
    private var needAnimator = true
    private var hasLayout=false

    private var autoStopWhenChildFlingToTop = true

    init {
        animator.duration = 500
        animator.addUpdateListener {
            ViewCompat.offsetTopAndBottom(child, -child.top + it.animatedValue as Int)
        }
    }


    //有时候onLayoutChild会被coordinatorLayout触发，需要判断是否需要重新布局
    override fun onLayoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int): Boolean {
        this.child = child
        if(!hasLayout){
            child.layout(0, initMaxTop, child.measuredWidth, child.measuredHeight + initMaxTop)
            hasLayout=true
        }
        return true
    }

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        return child == dependency
    }

    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: View, target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
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

        if (c > 0 && child.top == minTop && target.canScrollVertically(-1)) {//在顶部下拉时谁先消费滑动
            //这里如果什么都不做，内容下滑到顶部就终止,需要再次下拉才能带动child下移
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

    override fun onStopNestedScroll(coordinatorLayout: CoordinatorLayout, child: View, target: View, type: Int) {
        rec(child)
    }

    /**
     * ？？？？ 加上fling时间来判断，在fling结束后来进行recover是否会更好
     */
    override fun onNestedPreFling(coordinatorLayout: CoordinatorLayout, child: View, target: View, velocityX: Float, velocityY: Float): Boolean {
        //在drag状态，往上fling需要停止恢复动画，但需要确保向上的fling能够代替恢复动画
        //vy>0 上滑fling
        //onNestedPreFling 先于 onStopNestedScroll被调用
        //needAnimator = gap > getFlingTotalDistance(child.context, velocityY)
        needAnimator = !(velocityY > 0 && getFlingTotalDistance(child.context, velocityY) >= child.top - initMaxTop)
        return super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY)
    }

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: View, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
        return true
    }


}