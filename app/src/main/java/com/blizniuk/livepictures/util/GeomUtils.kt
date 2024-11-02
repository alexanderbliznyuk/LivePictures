package com.blizniuk.livepictures.util

import android.graphics.PointF
import android.view.MotionEvent
import kotlin.math.cos
import kotlin.math.sin


internal class GeomUtils {

    companion object {
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
            pivotY: Float
        ): PointF {
            val angle = Math.toRadians(angleDegrees.toDouble())
            val cos = cos(angle).toFloat()
            val sin = sin(angle).toFloat()

            val rx = pivotX + (x - pivotX) * cos - (y - pivotY) * sin
            val ry = pivotY + (x - pivotX) * sin + (y - pivotY) * cos

            return PointF(rx, ry)
        }
    }
}