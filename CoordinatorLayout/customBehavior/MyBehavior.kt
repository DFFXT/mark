package com.example.myapplication.tools

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.example.myapplication.R
import kotlin.math.max

class MyBehavior(context: Context?, attrs: AttributeSet?) :
    CoordinatorLayout.Behavior<View>(context, attrs) {

    private lateinit var dependency: View
    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        if (dependency.id == R.id.appbar) {
            this.dependency = dependency
            return true
        }
        return false
    }


    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        val change=max(0, dependency.bottom)
        child.layout(0, change,child.measuredWidth,change+child.measuredHeight)
        this.dependency = dependency
        return change!=0
    }

    override fun onLayoutChild(
        parent: CoordinatorLayout,
        child: View,
        layoutDirection: Int
    ): Boolean {
        log("" + dependency.height + " | " + child.width)
        child.layout(
            0,
            dependency.measuredHeight,
            1080,
            dependency.measuredHeight + child.measuredHeight
        )
        return true
    }

    override fun onMeasureChild(
        parent: CoordinatorLayout,
        child: View,
        parentWidthMeasureSpec: Int,
        widthUsed: Int,
        parentHeightMeasureSpec: Int,
        heightUsed: Int
    ): Boolean {
        val h = View.MeasureSpec.getSize(parentHeightMeasureSpec)
        val w = View.MeasureSpec.getSize(parentWidthMeasureSpec)
        //child.measure(View.MeasureSpec.makeMeasureSpec(w,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(h,View.MeasureSpec.EXACTLY))
        log(" " + h + " " + heightUsed + " " + child.measuredHeight + " " + dependency.measuredHeight)
        return false
    }

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
        log("scroll-->$dyConsumed  $dyUnconsumed")
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        log("start--->${!target.canScrollVertically(axes)}")
        return !target.canScrollVertically(axes)
    }


}

fun log(obj: Any) {
    Log.i("logInfo", obj.toString())
}