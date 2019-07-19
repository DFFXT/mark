package com.example.myapplication.rvToPager

import androidx.recyclerview.widget.RecyclerView

/**
 * 使用LayoutManager支持无限滑动
 */
class InfiniteLayoutManager : RecyclerView.LayoutManager() {
    /**
     * 计算垂直方向滑动的距离
     */
    override fun scrollVerticallyBy(dy: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
        val y = fill(dy, recycler, state)
        if (y == 0) return 0
        offsetChildrenVertical(-y)//**应用滑动
        for (c in 0 until childCount) {
            val v = getChildAt(c) ?: continue
            if (v.bottom < 0 || v.top > height) {
                removeAndRecycleView(v, recycler)
            }
        }
        return y
    }

    /**
     * 根据滑动距离来填充View
     */
    private fun fill(dy: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
        if (dy > 0) {//up
            val child = getChildAt(childCount - 1) ?: return 0
            if (child.bottom - dy <= height) {//**需要进行填充
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
            if (child.top - dy >= 0) {
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

    /**
     * 初始化布局
     */
    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (itemCount == 0 || state.isPreLayout) return
        detachAndScrapAttachedViews(recycler)
        var i = 0
        var totalHeight = 0
        while (true) {//**由于是无限滑动，所以这里是填充满
            val itemView = recycler.getViewForPosition(i)
            addView(itemView)
            measureChildWithMargins(itemView, 0, 0)
            val h = getDecoratedMeasuredHeight(itemView)
            val w = getDecoratedMeasuredWidth(itemView)
            layoutDecorated(itemView, 0, totalHeight, w, totalHeight + h)
            totalHeight += h

            if (totalHeight > height) {
                break
            }
            i++
            if (i == itemCount) i = 0
        }

    }


    override fun canScrollVertically(): Boolean {
        return true
    }

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT)
    }
}