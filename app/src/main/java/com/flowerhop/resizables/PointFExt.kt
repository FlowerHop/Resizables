package com.flowerhop.resizables

import android.graphics.PointF
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.sqrt

fun PointF.degreeFrom(srcPoint: PointF): Double {
    val deltaX: Float = x - srcPoint.x
    val deltaY: Float = y - srcPoint.y
    val radians:Float = atan(deltaY/deltaX)
    return radians*180/ PI
}

fun PointF.distanceTo(dstPointF: PointF): Float {
    val deltaX: Float = dstPointF.x - x
    val deltaY: Float = dstPointF.y - y
    return sqrt(deltaX*deltaX + deltaY*deltaY)
}