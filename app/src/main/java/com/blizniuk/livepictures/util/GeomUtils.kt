package com.blizniuk.livepictures.util

import android.graphics.PointF
import android.view.MotionEvent
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin


object GeomUtils {
    fun determineFocalPoint(e: MotionEvent): PointF {
        val pCount = e.pointerCount
        var x = 0f
        var y = 0f

        for (i in 0 until pCount) {
            x += e.getX(i)
            y += e.getY(i)
        }

        return PointF(x / pCount, y / pCount)
    }

    fun rotatePoint(
        x: Float,
        y: Float,
        angleDegrees: Float,
        pivotX: Float,
        pivotY: Float,
        dest: PointF
    ) {
        val angle = Math.toRadians(angleDegrees.toDouble())
        val cos = cos(angle).toFloat()
        val sin = sin(angle).toFloat()

        dest.x = pivotX + (x - pivotX) * cos - (y - pivotY) * sin
        dest.y = pivotY + (x - pivotX) * sin + (y - pivotY) * cos
    }

    fun min3(a: Float, b: Float, c: Float): Float {
        return min(a, min(b, c))
    }

    fun max3(a: Float, b: Float, c: Float): Float {
        return max(a, max(b, c))
    }

    fun min4(a: Float, b: Float, c: Float, d: Float): Float {
        return min(a, min(b, min(c, d)))
    }

    fun max4(a: Float, b: Float, c: Float, d: Float): Float {
        return max(a, max(b, max(c, d)))
    }

    fun rotateX(
        x: Float,
        y: Float,
        pivotX: Float,
        pivotY: Float,
        cos: Float,
        sin: Float
    ): Float {
        return pivotX + (x - pivotX) * cos - (y - pivotY) * sin
    }

    fun rotateY(
        x: Float,
        y: Float,
        pivotX: Float,
        pivotY: Float,
        cos: Float,
        sin: Float
    ): Float {
        return pivotY + (x - pivotX) * sin + (y - pivotY) * cos
    }
}