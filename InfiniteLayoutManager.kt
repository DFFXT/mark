package com.pwrd.dls.marble.moudle.monument.monumentGuide.adapter

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pwrd.dls.marble.common.view.getMarginBottom
import com.pwrd.dls.marble.common.view.getMarginEnd
import com.pwrd.dls.marble.common.view.getMarginStart
import com.pwrd.dls.marble.common.view.getMarginTop

/**
 * 使用LayoutManager支持无限滑动
 * 由于没有重载其他重要方法，这个只能靠手滑动，如果调用scrollToPosition等方法会出现奇怪bug
 * 将infiniteScroll置为false应该可以调用scrollToPosition的方法
 */
class InfiniteLayoutManager : LinearLayoutManager {

    //**默认无限滚动 false走LinearLayoutManager的逻辑
    var infiniteScroll = true

    //todo 更改回收View的时机和onLayoutChildren的规则可实现预加载item

    constructor(ctx: Context) : super(ctx)
    constructor(ctx: Context, orientation: Int, reverse: Boolean) : super(ctx, orientation, reverse)
    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(ctx, attrs, defStyleAttr, defStyleRes)

    /**
     * 计算垂直方向滑动的距离
     */
    override fun scrollVerticallyBy(dy: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
        if (!infiniteScroll) {
            return super.scrollVerticallyBy(dy, recycler, state)
        }
        val y = fillVertical(dy, recycler, state)
        if (y == 0) return 0
        offsetChildrenVertical(-y)//**应用滑动
        for (c in 0 until childCount) {
            val v = getChildAt(c) ?: continue
            if (getDecoratedBottom(v) <= 0 || getDecoratedTop(v) >= height) {
                removeAndRecycleView(v, recycler)
            }
        }
        return y
    }


    override fun scrollHorizontallyBy(dx: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
        if (!infiniteScroll) {
            return super.scrollHorizontallyBy(dx, recycler, state)
        }
        val x = fillHorizontal(dx, recycler, state)
        if (x == 0) return 0
        offsetChildrenHorizontal(-x)
        for (c in 0 until childCount) {
            val v = getChildAt(c) ?: continue
            if (getDecoratedRight(v) <= 0 || getDecoratedLeft(v) >= width) {
                removeAndRecycleView(v, recycler)
            }
        }
        return x
    }

    /**
     * 根据滑动距离来填充View
     */
    private fun fillVertical(dy: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
        if (itemCount == 0 || itemCount == 1) return 0
        if (dy > 0) {//up
            val child = getChildAt(childCount - 1) ?: return 0
            var bottom = getDecoratedBottom(child) + child.getMarginBottom()
            if (bottom - dy < height) {//**需要进行填充
                var p = getPosition(child)
                while (bottom - dy < height) {//**如果是快速滑动、有可能填充一个View还不够，所以要一直填充知道填充满
                    if (p == itemCount - 1) p = -1
                    p += 1
                    val itemView = recycler.getViewForPosition(p)
                    addView(itemView)
                    measureChildWithMargins(itemView, 0, 0)//测量View，包括margin值
                    val lp = itemView.layoutParams as ViewGroup.MarginLayoutParams
                    val h = getDecoratedMeasuredHeight(itemView) + lp.topMargin + lp.bottomMargin
                    val w = getDecoratedMeasuredWidth(itemView) + lp.marginStart + lp.marginEnd
                    layoutDecoratedWithMargins(itemView, paddingStart, bottom, w + paddingStart, bottom + h)
                    bottom += h
                }
            }
        } else {//down
            val child = getChildAt(0) ?: return 0
            var top = getDecoratedTop(child) - child.getMarginTop()
            if (getDecoratedTop(child) - dy > 0) {
                var p = getPosition(child)
                while (top - dy >= 0) {
                    if (p == 0) p = itemCount
                    p -= 1
                    val itemView = recycler.getViewForPosition(p)
                    addView(itemView, 0)//插入为第一个
                    measureChildWithMargins(itemView, 0, 0)
                    val lp = itemView.layoutParams as ViewGroup.MarginLayoutParams
                    val h = getDecoratedMeasuredHeight(itemView) + lp.topMargin + lp.bottomMargin
                    val w = getDecoratedMeasuredWidth(itemView) + lp.marginStart + lp.marginEnd
                    layoutDecoratedWithMargins(itemView, paddingStart, top - h, w + paddingStart, top)
                    top -= h
                }
            }
        }
        return dy
    }


    private fun fillHorizontal(dx: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
        if (itemCount == 0 || itemCount == 1) return 0
        if (dx > 0) {//left
            val child = getChildAt(childCount - 1) ?: return 0
            var right = getDecoratedRight(child) + child.getMarginEnd()
            if (right - dx < width) {//**需要进行填充
                var p = getPosition(child)
                while (right - dx < width) {//**如果是快速滑动、有可能填充一个View还不够，所以要一直填充知道填充满
                    if (p == itemCount - 1) p = -1
                    p += 1
                    val itemView = recycler.getViewForPosition(p)
                    addView(itemView)
                    measureChildWithMargins(itemView, 0, 0)//测量View，包括margin值
                    val lp = itemView.layoutParams as ViewGroup.MarginLayoutParams
                    val h = getDecoratedMeasuredHeight(itemView) + lp.topMargin + lp.bottomMargin
                    val w = getDecoratedMeasuredWidth(itemView) + lp.marginStart + lp.marginEnd
                    layoutDecoratedWithMargins(itemView, right, paddingTop, right + w, h + paddingTop)
                    right += w
                }
            }
        } else {//right
            val child = getChildAt(0) ?: return 0
            var left = getDecoratedLeft(child) - child.getMarginStart()
            if (left - dx > 0) {
                var p = getPosition(child)
                while (left - dx >= 0) {
                    if (p == 0) p = itemCount
                    p -= 1
                    val itemView = recycler.getViewForPosition(p)
                    addView(itemView, 0)//插入为第一个
                    measureChildWithMargins(itemView, 0, 0)
                    val lp = itemView.layoutParams as ViewGroup.MarginLayoutParams
                    val h = getDecoratedMeasuredHeight(itemView) + lp.topMargin + lp.bottomMargin
                    val w = getDecoratedMeasuredWidth(itemView) + lp.marginStart + lp.marginEnd
                    layoutDecoratedWithMargins(itemView, left - w, paddingTop, left, h + paddingTop)
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
        if (itemCount == 0) {
            detachAndScrapAttachedViews(recycler)
            return
        } else if (state.isPreLayout) return
        if (orientation == VERTICAL) {
            verticalLayout(recycler, state)
        } else {
            horizontalLayout(recycler, state)
        }
    }

    private fun verticalLayout(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        //如果存在已经有的layout ，继承
        val fp = findFirstVisibleItemPosition()
        var i = (if (fp == RecyclerView.NO_POSITION) 0 else fp) % itemCount
        val firstView = getChildAt(0)
        var totalHeight = if (firstView == null) paddingTop else getDecoratedTop(firstView)

        detachAndScrapAttachedViews(recycler)
        if (itemCount == 0) return
        while (true) {//**由于是无限滑动，所以这里是填充满
            val itemView = recycler.getViewForPosition(i)
            addView(itemView)
            measureChildWithMargins(itemView, 0, 0)
            val lp = itemView.layoutParams as ViewGroup.MarginLayoutParams
            val h = getDecoratedMeasuredHeight(itemView) + lp.topMargin + lp.bottomMargin
            val w = getDecoratedMeasuredWidth(itemView) + lp.marginStart + lp.marginEnd
            layoutDecoratedWithMargins(itemView, 0 + paddingStart, totalHeight + paddingTop, w + paddingStart, totalHeight + h)
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
        val firstChild = getChildAt(0)
        var totalWidth = if (firstChild == null) paddingStart else getDecoratedLeft(firstChild)
        detachAndScrapAttachedViews(recycler)
        if (itemCount == 0) return
        while (true) {//**由于是无限滑动，所以这里是填充满
            val itemView = recycler.getViewForPosition(i)
            addView(itemView)
            measureChildWithMargins(itemView, 0, 0)
            val lp = itemView.layoutParams as ViewGroup.MarginLayoutParams
            val h = getDecoratedMeasuredHeight(itemView) + lp.topMargin + lp.bottomMargin
            val w = getDecoratedMeasuredWidth(itemView) + lp.marginStart + lp.marginEnd
            layoutDecoratedWithMargins(itemView, totalWidth, paddingTop, totalWidth + w, h + paddingTop)
            totalWidth += w

            if (totalWidth >= width) {
                break
            }
            i++
            if (i == itemCount) i = 0
        }
    }


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