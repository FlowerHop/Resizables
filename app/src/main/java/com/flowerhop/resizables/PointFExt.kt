package com.flowerhop.resizables

import android.graphics.PointF
import java.util.*
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.sqrt

fun PointF.degreeFrom(srcPoint: PointF): Double {
    val deltaX: Float = x - srcPoint.x
    val deltaY: Float = y - srcPoint.y
    val radians:Float = atan(deltaY/deltaX)
    return radians*180/ PI
}

fun PointF.degreeFromVector(srcVector: PointF): Double {
    val m1 = (srcVector.y) / (srcVector.x)
    val m2 = y/x
    return atan((m2 - m1)/(1 + m1*m2))*180/Math.PI
}

fun PointF.vectorFrom(srcPoint: PointF): PointF {
    return PointF(
        x - srcPoint.x,
        y - srcPoint.y
    )
}

fun PointF.distanceTo(dstPointF: PointF): Float {
    val deltaX: Float = dstPointF.x - x
    val deltaY: Float = dstPointF.y - y
    return sqrt(deltaX*deltaX + deltaY*deltaY)
}