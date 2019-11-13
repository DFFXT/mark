package com.pwrd.dls.marble.moudle.monument.monumentGuide.adapter

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * 使用LayoutManager支持无限滑动
 * 由于没有重载其他重要方法，这个只能靠手滑动，如果调用scrollTo等方法会出现蜜汁bug
 * 由于onLayoutChildren没有暂时考虑margin 所以item如果有margin 会有bug
 */
class InfiniteLayoutManager/*(private val orientaion:Int= VERTICAL)*/ : LinearLayoutManager {


    constructor(ctx: Context) : super(ctx)
    constructor(ctx: Context, orientation: Int, reverse: Boolean) : super(ctx, orientation, reverse)
    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(ctx, attrs, defStyleAttr, defStyleRes)

    /**
     * 计算垂直方向滑动的距离
     */
    override fun scrollVerticallyBy(dy: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
        val y = fillVertical(dy, recycler, state)
        if (y == 0) return 0
        offsetChildrenVertical(-y)//**应用滑动
        for (c in 0 until childCount) {
            val v = getChildAt(c) ?: continue
            if (v.bottom <= 0 || v.top >= height) {
                removeAndRecycleView(v, recycler)
            }
        }
        return y
    }


    override fun scrollHorizontallyBy(dx: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
        val x = fillHorizontal(dx, recycler, state)
        if (x == 0) return 0
        offsetChildrenHorizontal(-x)
        for (c in 0 until childCount) {
            val v = getChildAt(c) ?: continue
            if (v.right <= 0 || v.left >= height) {
                removeAndRecycleView(v, recycler)
            }
        }
        return x
    }

    /**
     * 根据滑动距离来填充View
     */
    private fun fillVertical(dy: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
        if (dy > 0) {//up
            val child = getChildAt(childCount - 1) ?: return 0
            if (child.bottom - dy < height) {//**需要进行填充
                var bottom = child.bottom
                var p = getPosition(child)
                while (bottom - dy < height) {//**如果是快速滑动、有可能填充一个View还不够，所以要一直填充知道填充满
                    if (p == itemCount - 1) p = -1
                    p += 1
                    val itemView = recycler.getViewForPosition(p)
                    addView(itemView)
                    measureChildWithMargins(itemView, 0, 0)//测量View，包括margin值
                    val h = getDecoratedMeasuredHeight(itemView)
                    val w = getDecoratedMeasuredWidth(itemView)
                    layoutDecorated(itemView, 0, bottom, w, bottom + h)
                    bottom += h
                }
            }
        } else {//down
            val child = getChildAt(0) ?: return 0
            if (child.top - dy > 0) {
                var p = getPosition(child)
                var top = child.top
                while (top - dy >= 0) {
                    if (p == 0) p = itemCount
                    p -= 1
                    val itemView = recycler.getViewForPosition(p)
                    addView(itemView, 0)//插入为第一个
                    measureChildWithMargins(itemView, 0, 0)
                    val h = getDecoratedMeasuredHeight(itemView)
                    val w = getDecoratedMeasuredWidth(itemView)
                    layoutDecorated(itemView, 0, top - h, w, top)
                    top -= h
                }
            }
        }
        return dy
    }

    private fun fillHorizontal(dx: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
        if (dx > 0) {//left
            val child = getChildAt(childCount - 1) ?: return 0
            if (child.right - dx < width) {//**需要进行填充
                var right = child.right
                var p = getPosition(child)

                while (right - dx < width) {//**如果是快速滑动、有可能填充一个View还不够，所以要一直填充知道填充满
                    if (p == itemCount - 1) p = -1
                    p += 1
                    val itemView = recycler.getViewForPosition(p)
                    addView(itemView)
                    measureChildWithMargins(itemView, 0, 0)//测量View，包括margin值
                    val h = getDecoratedMeasuredHeight(itemView)
                    val w = getDecoratedMeasuredWidth(itemView)
                    layoutDecorated(itemView, right, 0, right + w, h)
                    right += w
                }
            }
        } else {//right
            val child = getChildAt(0) ?: return 0
            if (child.left - dx > 0) {
                var p = getPosition(child)
                var left = child.left
                while (left - dx >= 0) {
                    if (p == 0) p = itemCount
                    p -= 1
                    val itemView = recycler.getViewForPosition(p)
                    addView(itemView, 0)//插入为第一个
                    measureChildWithMargins(itemView, 0, 0)
                    val h = getDecoratedMeasuredHeight(itemView)
                    val w = getDecoratedMeasuredWidth(itemView)
                    layoutDecorated(itemView, left - w, 0, left, h)
                    left -= w
                }
            }
        }
        return dx
    }


    /**
     * 初始化布局
     * item 初次加载时会requestLayout ，所以会多次调用改方法
     */
    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (itemCount == 0 || state.isPreLayout) return
        if (orientation == VERTICAL) {
            verticalLayout(recycler, state)
        } else {
            horizontalLayout(recycler, state)
        }
    }

    private fun verticalLayout(recycler: RecyclerView.Recycler, state: RecyclerView.State) {

        val fp = findFirstVisibleItemPosition()
        var i = (if (fp == RecyclerView.NO_POSITION) 0 else fp) % itemCount
        var totalHeight = getChildAt(0)?.top ?: 0
        detachAndScrapAttachedViews(recycler)
        while (true) {//**由于是无限滑动，所以这里是填充满
            val itemView = recycler.getViewForPosition(i)
            addView(itemView)
            measureChildWithMargins(itemView, 0, 0)
            val h = getDecoratedMeasuredHeight(itemView)
            val w = getDecoratedMeasuredWidth(itemView)
            layoutDecorated(itemView, 0, totalHeight, w, totalHeight + h)
            totalHeight += h

            if (totalHeight >= height) {
                break
            }
            i++
            if (i == itemCount) i = 0
        }
    }

    private fun horizontalLayout(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        val fp = findFirstVisibleItemPosition()
        var i = (if (fp == RecyclerView.NO_POSITION) 0 else fp) % itemCount
        var totalWidth = getChildAt(0)?.left ?: 0
        detachAndScrapAttachedViews(recycler)
        while (true) {//**由于是无限滑动，所以这里是填充满
            val itemView = recycler.getViewForPosition(i)
            addView(itemView)
            measureChildWithMargins(itemView, 0, 0)
            val h = getDecoratedMeasuredHeight(itemView)
            val w = getDecoratedMeasuredWidth(itemView)
            layoutDecorated(itemView, totalWidth, 0, totalWidth + w, h)
            totalWidth += w

            if (totalWidth >= width) {
                break
            }
            i++
            if (i == itemCount) i = 0
        }
    }

    /*override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
        return PointF(1f,0f)
    }
    override fun smoothScrollToPosition(recyclerView: RecyclerView, state: RecyclerView.State?, position: Int) {
        val scroller=object : LinearSmoothScroller(recyclerView.context){
            override fun calculateDxToMakeVisible(view: View, snapPreference: Int): Int {
                val params = view.layoutParams as RecyclerView.LayoutParams
                val left=this@InfiniteLayoutManager.getDecoratedLeft(view) - params.leftMargin
                Log.i("logInfo","---->>>>>$left  ${view.left}  ${view.right}  $position")
                return -left
            }
        }
        scroller.targetPosition=position
        startSmoothScroll(scroller)
    }*/


    override fun canScrollVertically(): Boolean {
        return orientation == VERTICAL
    }

    override fun canScrollHorizontally(): Boolean {
        return orientation == HORIZONTAL
    }

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT)
    }
}