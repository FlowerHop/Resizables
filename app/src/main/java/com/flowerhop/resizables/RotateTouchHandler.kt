package com.flowerhop.resizables

import android.graphics.PointF
import android.graphics.RectF
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.sqrt

abstract class RotateTouchHandler: View.OnTouchListener {
    companion object {
        private val DEBUG = BuildConfig.DEBUG
        private const val TAG = "RotateTouchHandler"
        private fun logMsg(tag: String, msg: String) {
            if (!DEBUG) return
            Log.d(TAG, "[$tag]: $msg")
        }
    }

    private var lastPointOnParent: PointF? = null
    var contentRoi = Roi(RectF(0F, 0F, 200F, 200F))
    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        val pointOnParent = PointF(
            motionEvent.x,
            motionEvent.y
        )
        when (motionEvent.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                logMsg(
                    "Touch Down",
                    "at x: ${pointOnParent.x}, y: ${pointOnParent.y}"
                )

                lastPointOnParent = pointOnParent

                onStart(contentRoi)
            }

            MotionEvent.ACTION_MOVE -> {
                logMsg(
                    "Touch Move",
                    "at x: ${pointOnParent.x}, y: ${pointOnParent.y}"
                )

                val centerPointOfContent = PointF(
                    contentRoi.rectF.centerX(),
                    contentRoi.rectF.centerY()
                )

                // scaled
                val oriDistance = (lastPointOnParent!!.x - centerPointOfContent.x)*(lastPointOnParent!!.x - centerPointOfContent.x) + (lastPointOnParent!!.y - centerPointOfContent.y)*(lastPointOnParent!!.y - centerPointOfContent.y)
                val currDistance = (pointOnParent.x - centerPointOfContent.x)*(pointOnParent.x - centerPointOfContent.x) + (pointOnParent.y - centerPointOfContent.y)*(pointOnParent.y - centerPointOfContent.y)
                val scaled = sqrt(currDistance/oriDistance)
                val scaledWidth = contentRoi.rectF.width()*scaled
                val scaledHeight = contentRoi.rectF.height()*scaled

                val degreeChange = pointOnParent.vectorFrom(centerPointOfContent).degreeFromVector(
                    lastPointOnParent!!.vectorFrom(centerPointOfContent)
                )

                contentRoi = contentRoi.copy(
                    rectF = RectF(
                        centerPointOfContent.x - 0.5f*scaledWidth,
                        centerPointOfContent.y - 0.5f*scaledHeight,
                        centerPointOfContent.x + 0.5f*scaledWidth,
                        centerPointOfContent.y + 0.5f*scaledHeight
                    ),
                    degree = (contentRoi.degree + (degreeChange)).toFloat()
                )

                lastPointOnParent = pointOnParent

                onChanging(contentRoi)
            }

            MotionEvent.ACTION_CANCEL -> {
                logMsg("Touch Cancel", "cancel!")
            }
            MotionEvent.ACTION_UP -> {
                logMsg("Touch Up", "up!")
                onEnd(contentRoi)
                lastPointOnParent = null
            }
            else -> {}
        }
        return false
    }

    protected abstract fun onStart(roi: Roi)
    protected abstract fun onChanging(roi: Roi)
    protected abstract fun onEnd(roi: Roi)
}