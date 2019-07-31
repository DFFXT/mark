package com.pwrd.dls.marble

import android.graphics.*
import android.util.SparseArray
import android.view.View
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView如果需要截屏且需要ItemDecoration，应实现该抽象类
 * decoration绘制高度不能超过itemView.height+itemOffset.top+itemOffset.bottom，截屏时超出部分不显示
 */
abstract class ScreenShotItemDecoration : RecyclerView.ItemDecoration() {

    private val insets = SparseArray<Rect?>()
    private val paint = Paint()

    init {
        paint.color = Color.parseColor("#66000000")
    }

    /**
     * 最后再callSuper
     */
    @CallSuper
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val p = parent.getChildAdapterPosition(view)
        if (insets[p] == null) {
            insets.put(p, Rect(outRect))
        } else {
            insets[p]!!.set(outRect)
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {

        val adapter = parent.adapter as ScreenShotVerticalAdapter
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val lp = child.layoutParams as RecyclerView.LayoutParams
            val position = parent.getChildAdapterPosition(child)
            val left = child.left - lp.marginStart
            val top = child.top - lp.topMargin
            val right = child.right + lp.marginEnd
            val bottom = child.bottom + lp.bottomMargin
            drawItemDecoration(c, parent, left, top, right, bottom, position)

            val inset = insets[position]!!
            if (adapter.screenShotEnable) {
                val rect = RectF(
                        0f,
                        top - inset.top.toFloat() - if (position == 0) parent.paddingTop else 0,
                        parent.width.toFloat(),
                        bottom + inset.bottom.toFloat() + if (position == adapter.itemCount - 1) parent.paddingBottom else 0)
                if (!adapter.isCaptured(position)) {//绘制未选中item的遮罩区域
                    drawMask(c, rect)
                } else {
                    drawCaptured(c, rect)
                }
            }
        }
    }


    /**
     * 绘制遮罩层
     */
    open fun drawMask(canvas: Canvas, rect: RectF) {
        canvas.drawRect(rect, paint)
    }

    /**
     * 绘制选中时的遮罩层
     */
    open fun drawCaptured(canvas: Canvas, rect: RectF) {}

    /**
     * @param canvas
     * @param left itemView的left-marginStart
     * @param top
     * @param right
     * @param bottom
     */
    abstract fun drawItemDecoration(canvas: Canvas, parent: RecyclerView, left: Int, top: Int, right: Int, bottom: Int, position: Int)


}