package com.flowerhop.resizables

import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF

data class Roi(
    val rectF: RectF,
    val degree: Float = 0F
) {
    val points: FloatArray by lazy {
        floatArrayOf(
            rectF.left, rectF.top,
            rectF.right, rectF.top,
            rectF.right, rectF.bottom,
            rectF.left, rectF.bottom
        ).apply {
            val matrix = Matrix()
            matrix.setRotate(degree, rectF.centerX(), rectF.centerY())
            matrix.mapPoints(this)
        }
    }

    fun getPointAt(corner: Corner): PointF = when (corner) {
        Corner.LeftTop -> PointF(points[0], points[1])
        Corner.RightTop -> PointF(points[2], points[3])
        Corner.RightBottom -> PointF(points[4], points[5])
        else -> PointF(points[6], points[7])
    }
}

enum class Corner {
    LeftTop, RightTop, RightBottom, LeftBottom
}