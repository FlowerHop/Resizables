package com.flowerhop.resizables

import android.content.Context
import android.graphics.PointF
import android.graphics.RectF
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View

abstract class PanZoomListener(context: Context): ScaleGestureDetector.SimpleOnScaleGestureListener(),
    View.OnTouchListener,
    GestureDetector.OnDoubleTapListener,
    GestureDetector.OnGestureListener {
    companion object {
        private val DEBUG = BuildConfig.DEBUG
        private const val TAG = "PanZoomListener"
        const val DEFAULT_SCALED_RESISTANCE = 0.8F
        private const val INVALID_INDEX = -1
        private fun logMsg(tag: String, msg: String) {
            if (!DEBUG) return
            Log.d(TAG, "[$tag]: $msg")
        }
    }

    private val gestureDetector: GestureDetector by lazy {
        GestureDetector(context, this).apply {
            setOnDoubleTapListener(this@PanZoomListener)
        }
    }

    private val scaleDetector: ScaleGestureDetector by lazy {
        ScaleGestureDetector(context, this)
    }

    private var mActivePointerId: Int = INVALID_INDEX
    private var mLastTouchRecords: TouchRecords? = null

    var contentRoi = Roi(RectF(0F, 0F, 200F, 200F))

    private var mIsMultiPoints: Boolean = false
    private var scaledResistance = DEFAULT_SCALED_RESISTANCE

    protected abstract fun onStart(roi: Roi)
    protected abstract fun onChanging(roi: Roi)
    protected abstract fun onEnd(roi: Roi)

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        val scaleFactor = (detector.scaleFactor - 1)*scaledResistance + 1

        logMsg("onScale", "scaleFactor: ${scaleFactor}")
        val oriRect = RectF(contentRoi.rectF)

        val oriCenter = PointF(oriRect.centerX(), oriRect.centerY())
        // TODO: Don't let the object get too small or too large.
        val newWidth = oriRect.width()*scaleFactor
        val newHeight = oriRect.height()*scaleFactor

        contentRoi = contentRoi.copy(
            rectF = RectF(
                oriCenter.x - newWidth*0.5f,
                oriCenter.y - newHeight*0.5f,
                oriCenter.x + newWidth*0.5f,
                oriCenter.y + newHeight*0.5f,
            ),
            degree = contentRoi.degree
        )

        onChanging(contentRoi.copy())
        return true
    }


    override fun onSingleTapConfirmed(p0: MotionEvent?): Boolean = false
    override fun onDoubleTap(p0: MotionEvent?): Boolean = false

    override fun onDoubleTapEvent(p0: MotionEvent?): Boolean = false
    override fun onDown(motionEvent: MotionEvent): Boolean {
        logMsg("Touch Down",
            "at x: ${motionEvent.getX(mActivePointerId)}, y: ${motionEvent.getY(mActivePointerId)}")
        mLastTouchRecords = TouchRecords(
            PointF(
                motionEvent.getX(mActivePointerId),
                motionEvent.getY(mActivePointerId)
            )
        )

        onStart(contentRoi.copy())
        return true
    }

    override fun onShowPress(p0: MotionEvent?) {}

    override fun onSingleTapUp(p0: MotionEvent?): Boolean = false

    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean = false

    override fun onLongPress(p0: MotionEvent?) {}

    override fun onFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean = false
    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(motionEvent)

        if (mActivePointerId == INVALID_INDEX)
            mActivePointerId = motionEvent.getPointerId(0)
        if (gestureDetector.onTouchEvent(motionEvent))
            return true
        else {
            view.onTouchEvent(motionEvent)
            when (motionEvent.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    logMsg("Touch Down", "at x: ${motionEvent.x}, y: ${motionEvent.y}")
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    logMsg("Pointer Down",
                        "at x: ${motionEvent.x}, y: ${motionEvent.y}")
                    val activePointerIndex = motionEvent.findPointerIndex(mActivePointerId)
                    val extraPointerIndex: Int = if (activePointerIndex == 0) 1 else 0

                    mLastTouchRecords = mLastTouchRecords!!.copy(
                        activePointF = mLastTouchRecords!!.activePointF,
                        extraPointF = PointF(
                            motionEvent.getX(extraPointerIndex),
                            motionEvent.getY(extraPointerIndex)
                        )
                    )

                    mIsMultiPoints = true
                }
                MotionEvent.ACTION_MOVE -> {
                    logMsg("Touch Move", "at x: ${motionEvent.x}, y: ${motionEvent.y}")

                    if (mIsMultiPoints) {
                        val activePointerIndex = motionEvent.findPointerIndex(mActivePointerId)
                        val extraPointerIndex: Int = if (activePointerIndex == 0) 1 else 0

                        val touchRecords = TouchRecords(
                            PointF(
                                motionEvent.getX(activePointerIndex),
                                motionEvent.getY(activePointerIndex)
                            ),
                            PointF(
                                motionEvent.getX(extraPointerIndex),
                                motionEvent.getY(extraPointerIndex)
                            )
                        )

                        val oriDegree = mLastTouchRecords!!.getDegree()
                        val currDegree = touchRecords.getDegree()
                        logMsg("Multi Touch Move",
                            "oriDegree: $oriDegree, currDegree: $currDegree")

                        val oriCenter = mLastTouchRecords!!.getCenter()
                        val currCenter = touchRecords.getCenter()
                        logMsg("Multi Touch Move",
                            "oriCenter: $oriCenter, newCenter: $currCenter")

                        val newRectF = RectF(contentRoi.rectF)
                        newRectF.offset(currCenter.x - oriCenter.x, currCenter.y - oriCenter.y)

                        mLastTouchRecords = touchRecords

                        contentRoi = Roi(
                            newRectF,
                            contentRoi.degree + (currDegree - oriDegree).toInt()
                        )

                        onChanging(contentRoi)
                    } else {
                        logMsg("Touch Move", "single point mode")
                        val newRectF = RectF(contentRoi.rectF)
                        newRectF.offset(motionEvent.x - mLastTouchRecords!!.activePointF.x, motionEvent.y - mLastTouchRecords!!.activePointF.y)
                        contentRoi = contentRoi.copy(
                            rectF = newRectF
                        )

                        mLastTouchRecords = TouchRecords(
                            PointF(motionEvent.x, motionEvent.y)
                        )

                        onChanging(contentRoi.copy())
                    }
                }
                MotionEvent.ACTION_POINTER_UP -> {
                    mIsMultiPoints = false
                    logMsg("Pointer Up", "at x: ${motionEvent.x}, y: ${motionEvent.y}")
                    val pointerUpIndex = motionEvent.findPointerIndex(motionEvent.actionIndex)
                    if (pointerUpIndex == mActivePointerId) {
                        val newActiveIndex = if (pointerUpIndex == 0) 1 else 0
                        mLastTouchRecords = TouchRecords(
                            PointF(motionEvent.getX(newActiveIndex), motionEvent.getY(newActiveIndex))
                        )
                        mActivePointerId = motionEvent.getPointerId(newActiveIndex)
                    }

                    TouchRecords(
                        mLastTouchRecords!!.activePointF,
                    ).also { mLastTouchRecords = it }
                }
                MotionEvent.ACTION_CANCEL -> {
                    logMsg("Touch Cancel", "cancel!")
                    mLastTouchRecords = null
                    mActivePointerId = INVALID_INDEX
                }
                MotionEvent.ACTION_UP -> {
                    logMsg("Touch Up", "up!")
                    onEnd(contentRoi.copy())
                    mLastTouchRecords = null
                    mActivePointerId = INVALID_INDEX
                }
            }
            return false
        }
    }
}