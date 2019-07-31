package com.pwrd.dls.marble

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView
import com.pwrd.dls.marble.common.view.inflate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

/**
 * 可以用来长截屏的RecyclerView#adapter
 * 支持截取ItemDecoration部分
 * 截图范围包括RecyclerView的padding部分和背景
 * 只支持Vertical
 * 类似于QQ聊天截图
 */

abstract class ScreenShotVerticalAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var screenShotEnable = false
        private set
    private lateinit var rv: RecyclerView
    private var itemAnimator: RecyclerView.ItemAnimator? = null
    private var captureSet: TreeSet<Int> = TreeSet()
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        rv = recyclerView
        itemAnimator = recyclerView.itemAnimator
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = parent.inflate(R.layout.item_screen_shoot) as ViewGroup
        v.addView(createView(v, viewType), 0)
        return object : RecyclerView.ViewHolder(v) {}
    }

    @CallSuper
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val mask = holder.itemView.findViewById<View>(R.id.view_mask)
        if (screenShotEnable) {
            mask.visibility = View.VISIBLE
            if (captureSet.contains(position)) {
                onCaptured(holder.itemView, mask, position)
            } else {
                onUncaptured(holder.itemView, mask, position)
            }
            mask.setOnClickListener {
                if (captureSet.contains(position)) {
                    removeCaptureItem(position)
                    onUncaptured(holder.itemView, mask, position)
                } else {
                    captureItem(position)
                    onCaptured(holder.itemView, mask, position)
                }
            }
        } else {
            mask.visibility = View.GONE
        }
        holder.itemView.findViewById<View>(R.id.view_mask)
    }

    fun switchMode() {
        screenShotEnable = !screenShotEnable
        captureSet.clear()
        if (screenShotEnable) {
            rv.itemAnimator = null

        } else {
            rv.itemAnimator = itemAnimator
        }
        notifyDataSetChanged()
    }

    private fun removeCaptureItem(position: Int) {
        captureSet.remove(position)
        notifyItemChanged(position)
    }

    private fun captureItem(position: Int) {
        captureSet.add(position)
        notifyItemChanged(position)
    }

    private fun convertToBitmap(v: View, p: Int): Bitmap {
        val lm = rv.layoutManager!!
        lm.measureChildWithMargins(v, 0, 0)
        val lp = v.layoutParams as RecyclerView.LayoutParams
        //view 需要经过View.layout, View.draw才会绘制到canvas上，不然left==right==0
        lm.layoutDecoratedWithMargins(v, rv.paddingStart, 0, rv.width - rv.paddingEnd, v.measuredHeight + lp.bottomMargin + lp.marginStart)
        val rect = Rect(0, 0, 0, 0)
        var itemDecoration: ScreenShotItemDecoration? = null
        v.findViewById<View>(R.id.view_mask).visibility = View.GONE
        if (rv.itemDecorationCount != 0) {
            itemDecoration = rv.getItemDecorationAt(0) as? ScreenShotItemDecoration
            if (itemDecoration == null) {
                throw Exception("ItemDecoration should implement ScreenShotItemDecoration")
            } else if (rv.itemDecorationCount > 1) {
                Timber.w(ScreenShotVerticalAdapter::class.java.name, "itemDecorationCount must <=1")
            }
        }
        itemDecoration?.getItemOffsets(rect, v, rv, RecyclerView.State())
        val bitmap = Bitmap.createBitmap(rv.width, v.measuredHeight + lp.topMargin + lp.bottomMargin + rect.top + rect.bottom, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        //绘制itemDecoration和itemView
        itemDecoration?.drawItemDecoration(canvas, rv, v.left - lp.marginStart, rect.top, v.right + lp.marginEnd, rect.top + v.measuredHeight + lp.bottomMargin + lp.topMargin, p)
        canvas.translate(v.left.toFloat(), rect.top.toFloat() + lp.topMargin)
        v.draw(canvas)
        return bitmap
    }

    /**
     * 合并长图
     */
    fun mergeCaptureItem(callback: (Bitmap?) -> Unit) {
        if (captureSet.isEmpty()) {
            callback(null)
            return
        }
        GlobalScope.launch(Dispatchers.IO) {
            var totalHeight = rv.paddingTop + rv.paddingBottom
            val totalWidth = rv.width
            val field = RecyclerView::class.java.getDeclaredField("mRecycler")
            field.isAccessible = true
            val recycler = field.get(rv) as RecyclerView.Recycler
            val itemBitmapList = ArrayList<Bitmap>()

            //获取item的bitmap
            for (i in captureSet) {//计算总高度
                val child = recycler.getViewForPosition(i)
                val itemBitmap = convertToBitmap(child, i)
                itemBitmapList.add(itemBitmap)
                totalHeight += itemBitmap.height
            }

            val bitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888)
            var offsetY = rv.paddingTop
            val canvas = Canvas(bitmap)
            val paint = Paint()

            //绘制背景
            canvas.save()
            val rvBackgroundDrawable = rv.background
            var offset = 0
            while (offset < totalHeight) {
                rvBackgroundDrawable.draw(canvas)
                canvas.translate(0f, rv.height.toFloat())
                offset += rv.height
            }
            canvas.restore()

            //绘制item
            for (itemBitmap in itemBitmapList) {
                itemBitmap.apply {
                    canvas.drawBitmap(this, Rect(0, 0, totalWidth, height), Rect(0, offsetY, totalWidth, offsetY + height), paint)
                    offsetY += height
                }
            }
            withContext(Dispatchers.Main) {
                callback(bitmap)
            }
        }
    }

    fun mergeAll(callback: (Bitmap?) -> Unit) {
        for (i in 0 until itemCount) {
            captureSet.add(i)
        }
        mergeCaptureItem(callback)
    }

    fun isCaptured(position: Int): Boolean {
        return captureSet.contains(position)
    }

    open fun onCaptured(itemView: View, maskView: View, position: Int) {}
    open fun onUncaptured(itemView: View, maskView: View, position: Int) {}
    abstract fun createView(parent: ViewGroup, viewType: Int): View

}