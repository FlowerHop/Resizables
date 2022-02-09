package com.flowerhop.resizables

import android.graphics.PointF
import com.flowerhop.resizables.TouchRecords.Companion.INVALID_POINT

data class TouchRecords(
    val activePointF: PointF = INVALID_POINT,
    val extraPointF: PointF = INVALID_POINT
) {
    companion object {
        val INVALID_POINT = PointF(Float.MIN_VALUE, Float.MIN_VALUE)
    }

    fun getDegree(): Double {
        if (extraPointF == INVALID_POINT) return 0.0
        return extraPointF.degreeFrom(activePointF)
    }

    fun getCenter(): PointF {
        if (extraPointF == INVALID_POINT) return PointF(
            activePointF.x,
            activePointF.y
        )

        return PointF((activePointF.x + extraPointF.x)*0.5f, (activePointF.y + extraPointF.y)*0.5f)
    }

    /**
     * return the vector of activePointF to extraPointF, if extraPointF is invalid, return activePointF
     */
    fun getVector(): PointF {
        if (extraPointF == INVALID_POINT) return PointF(activePointF.x, activePointF.y)
        return extraPointF.vectorFrom(activePointF)
    }
}