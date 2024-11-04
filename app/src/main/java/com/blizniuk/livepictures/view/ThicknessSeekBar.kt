package com.blizniuk.livepictures.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatSeekBar
import com.blizniuk.livepictures.R
import com.blizniuk.livepictures.domain.graphics.ToolData
import kotlin.math.min
import kotlin.math.roundToInt

class ThicknessSeekBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : AppCompatSeekBar(context, attrs) {
    init {
        progressDrawable = ThicknessDrawable(context)
        thumb = context.getDrawable(R.drawable.thickness_thumb)
        splitTrack = false
        max = 1000

        setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                onThicknessChanged?.invoke(convertFromProgress(progress))
            }
        })
    }

    var onThicknessChanged: ((Float) -> Unit)? = null

    fun setThickness(thicknessLevel: Float) {
        progress = convertToProgress(thicknessLevel)
    }

    private fun convertToProgress(thicknessLevel: Float) : Int {
        return ((max / (ToolData.MaxThickness - ToolData.MinThickness)) * (thicknessLevel - ToolData.MinThickness)).roundToInt()
    }

    private fun convertFromProgress(progress: Int): Float {
        return (progress.toFloat() / max) * (ToolData.MaxThickness - ToolData.MinThickness) + ToolData.MinThickness
    }
}

class ThicknessDrawable(private val context: Context) : Drawable() {
    private val paint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
        isDither = true
    }

    private val path = Path()

    override fun onBoundsChange(bounds: Rect) {
        val left = bounds.left.toFloat()
        val right = bounds.right.toFloat()
        val top = bounds.top.toFloat()
        val bottom = bounds.bottom.toFloat()
        val cy = (top + bottom) / 2

        paint.shader = LinearGradient(
            left,
            cy,
            right,
            cy,
            StartColor,
            EndColor,
            Shader.TileMode.CLAMP
        )

        val maxRadius = min(bottom - top, dpToPx(MaxHeightDp)) / 2
        val minRadius = dpToPx(MinHeightDp) / 2

        path.moveTo(left + minRadius, cy - minRadius)
        path.lineTo(left + minRadius, cy + minRadius)
        path.lineTo(right - maxRadius, cy + maxRadius)
        path.lineTo(right - maxRadius, cy - maxRadius)
        path.close()

        path.addOval(left, cy - minRadius, left + 2 * minRadius, cy + minRadius, Path.Direction.CCW)
        path.addOval(
            right - 2 * maxRadius,
            cy - maxRadius,
            right,
            cy + maxRadius,
            Path.Direction.CCW
        )
    }


    private fun dpToPx(value: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value,
            context.resources.displayMetrics
        )
    }

    override fun draw(canvas: Canvas) {
        canvas.drawPath(path, paint)
    }

    override fun setAlpha(alpha: Int) {
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    companion object {
        private const val StartColor = 0xFFEAFFAB.toInt()
        private const val EndColor = 0xFF9CCB0C.toInt()

        private const val MinHeightDp = 2F
        private const val MaxHeightDp = 12F
    }
}