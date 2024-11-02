package com.blizniuk.livepictures.ui.colorpicker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class ColorBarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val rect = RectF()

    private val gradientColors = intArrayOf(
        0xFFFFFFFF.toInt(),
        0xFF000000.toInt(),
        0xFFFF0000.toInt(),
        0xFFFFFF00.toInt(),
        0xFF00FF00.toInt(),
        0xFF00FFFF.toInt(),
        0xFF0000FF.toInt(),
        0xFFFF00FF.toInt(),
        0xFFFF0000.toInt()
    )

    private val paint: Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rect.set(0F, 0F, w.toFloat(), h.toFloat())
        paint.shader = LinearGradient(
            0F,
            0F,
            w.toFloat(),
            1F,
            gradientColors,
            null,
            Shader.TileMode.REPEAT
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val rx = measuredHeight.toFloat() / 2
        canvas.drawRoundRect(rect, rx, rx, paint)
    }

    fun getColor(part: Float): Int {
        val segmentSize = 1F / (gradientColors.size - 1)
        val startIndex = max(floor(part.toDouble() / segmentSize).toInt(), 0)
        val endIndex = min(startIndex + 1, gradientColors.size - 1)

        val startColor = gradientColors[startIndex]
        val endColor = gradientColors[endIndex]

        val ratio = (part - startIndex * segmentSize) / segmentSize

        return ColorUtils.blendARGB(startColor, endColor, ratio)
    }

    fun getPart(color: Int): Float {
        val red = color.red
        val green = color.green
        val blue = color.blue

        val minColor = Color.argb(
            0xFF,
            if (red == 0xFF) 0xFF else 0,
            if (green == 0xFF) 0xFF else 0,
            if (blue == 0xFF) 0xFF else 0
        )
        val maxColor = Color.argb(
            0xFF,
            if (red > 0) 0xFF else 0,
            if (green > 0) 0xFF else 0,
            if (blue > 0) 0xFF else 0
        )

        var startIndex = 0
        for (index in gradientColors.indices) {
            if (gradientColors[index] == minColor && gradientColors.getOrNull(index - 1) == maxColor) {
                startIndex = index - 1
                break
            }

            if (gradientColors[index] == minColor && gradientColors.getOrNull(index + 1) == maxColor) {
                startIndex = index
                break
            }
        }


        val startColor = gradientColors[startIndex]
        val endColor = gradientColors[startIndex + 1]


        var ratio = 0F
        var count = 0

        if (startColor.red != endColor.red) {
            ratio += 1F * (red - startColor.red) / (endColor.red - startColor.red)
            count++
        }

        if (startColor.green != endColor.green) {
            ratio += 1F * (green - startColor.green) / (endColor.green - startColor.green)
            count++
        }

        if (startColor.blue != endColor.blue) {
            ratio += 1F * (blue - startColor.blue) / (endColor.blue - startColor.blue)
            count++
        }

        ratio /= count

        val segmentSize = 1F / (gradientColors.size - 1)
        return segmentSize * startIndex + segmentSize * ratio
    }
}