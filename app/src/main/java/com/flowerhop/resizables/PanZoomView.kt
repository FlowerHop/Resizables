package com.flowerhop.resizables

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.flowerhop.resizables.PanZoomView.MovingState.Gesture
import com.flowerhop.resizables.PanZoomView.MovingState.Handle
import kotlinx.android.synthetic.main.pan_zoom_view.view.*
import kotlin.math.*

class PanZoomView: ConstraintLayout {
    companion object {
        private val DEBUG = BuildConfig.DEBUG
        private const val TAG = "PanZoomView"
        private fun logMsg(tag: String, msg: String) {
            if (!DEBUG) return
            Log.d(TAG, "[$tag]: $msg")
        }
    }

    interface OnSizeListener {
        fun onSizeChanging(roi: Roi)
        fun onSizeChanged(roi: Roi)
    }

    enum class MovingState {
        Gesture, Handle
    }

    private var movingState = Gesture

    private val mSolidPaint: Paint by lazy {
        Paint().apply {
            style = Paint.Style.FILL
            color = resources.getColor(R.color.purple_700)
        }
    }

    private val mBorderPaint: Paint by lazy {
        Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = resources.displayMetrics.density * 3
            color = Color.WHITE
        }
    }

    private val mPath: Path = Path()

    private val mPanZoomListener: PanZoomListener by lazy {
        object : PanZoomListener(context) {
            override fun onStart(roi: Roi) {
                logMsg("onStart", "$roi")
                onSizeStartChanging(roi)
            }

            override fun onChanging(roi: Roi) {
                logMsg("onChanging", "$roi")
                onSizeChanging(roi)
            }

            override fun onEnd(roi: Roi) {
                logMsg("onEnd", "$roi")
                onSizeChanged(roi)
            }
        }
    }

    private val mRotateHandleListener = object : RotateTouchHandler() {
        override fun onStart(roi: Roi) {
            logMsg("onStart", "$roi")
            onSizeStartChanging(roi)
        }

        override fun onChanging(roi: Roi) {
            logMsg("onChanging", "$roi")
            onSizeChanging(roi)
        }

        override fun onEnd(roi: Roi) {
            logMsg("onEnd", "$roi")
            onSizeChanged(roi)
        }
    }

    fun setRoi(roi: Roi) {
        mPanZoomListener.contentRoi = roi.copy()
        mRotateHandleListener.contentRoi = roi.copy()
        contentRoi = roi
    }

    private var contentRoi = Roi(RectF(0F, 0F, 200F, 200F))
    var onSizeListener: OnSizeListener? = null

    constructor(context: Context): super(context) {
        initView(context)
    }
    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet) {
        initView(context)
    }
    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int): super(context, attributeSet, defStyleAttr) {
        initView(context)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView(context: Context) {
        inflate(context, R.layout.pan_zoom_view, this)

        setOnTouchListener { view, motionEvent ->
            when (motionEvent.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    val hitRect = Rect()
                    rotateHandle.getHitRect(hitRect)
                    movingState = if (hitRect.contains(
                            motionEvent.x.toInt(),
                            motionEvent.y.toInt()
                        )
                    ) Handle else Gesture
                }
                else -> {
                }
            }

            when (movingState) {
                Handle -> mRotateHandleListener.onTouch(view, motionEvent)
                Gesture -> mPanZoomListener.onTouch(view, motionEvent)
                else -> false
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val points = contentRoi.points

        canvas?.let {
            logMsg("onDraw", contentRoi.toString())

            mPath.apply {
                reset()
                moveTo(points[0], points[1])
                lineTo(points[2], points[3])
                lineTo(points[4], points[5])
                lineTo(points[6], points[7])
                lineTo(points[0], points[1])
                mPath.close()
            }

            it.drawPath(mPath, mBorderPaint)
        }

    }

    private fun onSizeStartChanging(roi: Roi) {
        setRoi(roi)
        invalidate()
        onSizeListener?.onSizeChanging(roi)
    }

    private fun onSizeChanging(roi: Roi) {
        setRoi(roi)
        invalidate()

        val pointF = roi.getPointAt(Corner.RightBottom)

        rotateHandle.x = pointF.x - 0.5f*rotateHandle.width
        rotateHandle.y = pointF.y - 0.5f*rotateHandle.height
        rotateHandle.rotation = roi.degree
        rotateHandle.requestLayout()
        onSizeListener?.onSizeChanging(roi)
    }

    private fun onSizeChanged(roi: Roi) {
        setRoi(roi)
        invalidate()
        onSizeListener?.onSizeChanged(roi)
    }
}