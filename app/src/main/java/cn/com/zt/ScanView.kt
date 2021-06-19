package cn.com.zt

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

/**
 * Author:zhangteng
 * description:
 * date：2021/6/19
 */
class ScanView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val circlePaint = Paint() //二维码圆圈画笔
    private var rectList: ArrayList<RectF>? = null  //二维码数组
    private var scanLine: Bitmap//横线
    private var isShowLine = true//是否显示扫描线
    private var animator: ObjectAnimator? = null
    private var floatYFraction = 0f
        set(value) {
            field = value
            invalidate()
        }

    init {
        circlePaint.apply {
            this.style = Paint.Style.FILL
            this.color = ContextCompat.getColor(
                context, android.R.color.holo_green_dark
            )
        }

        scanLine = BitmapFactory.decodeResource(resources, R.drawable.scan_light)
        getAnimator().start()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        parseResult(canvas)
        if (isShowLine) {
            canvas?.drawBitmap(scanLine, (width - scanLine.width) / 2f, height * floatYFraction, circlePaint)
        }
    }

    private fun getAnimator(): ObjectAnimator {
        if (animator == null) {
            animator = ObjectAnimator.ofFloat(
                this,
                "floatYFraction",
                0f,
                1f
            )
            animator?.duration = 5000
            animator?.repeatCount = -1 //-1代表无限循环
        }
        return animator!!
    }

    private fun parseResult(canvas: Canvas?) {
        rectList?.let { list ->
            if (list.isEmpty()) {
                return
            }
            list.forEach {
                canvas?.drawCircle(
                    it.left + (it.right - it.left) / 2f,
                    it.top + (it.bottom - it.top) / 2f,
                    50f,
                    circlePaint
                )
            }
        }
    }

    fun setRectList(list: ArrayList<RectF>?) {
        rectList = list
        rectList?.let {
            if (it.isNotEmpty()) {
                isShowLine = false
                getAnimator().cancel()
                invalidate()
            }
        }
    }
}