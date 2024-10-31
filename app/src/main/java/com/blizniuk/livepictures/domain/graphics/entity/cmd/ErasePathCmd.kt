package com.blizniuk.livepictures.domain.graphics.entity.cmd

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import com.blizniuk.livepictures.domain.graphics.entity.Point
import com.blizniuk.livepictures.domain.graphics.entity.RenderContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

class ErasePathCmd(
    var thicknessLevel: Float,
    points: List<Point> = emptyList(),
) : DrawCmd(), FreeDrawableCmd {
    private val points: MutableList<Point> = mutableListOf()
    private val path = Path()

    init {
        if (points.isNotEmpty()) {
            this.points.addAll(points)
            val startPoint = points.first()
            path.moveTo(startPoint.x, startPoint.y)
            for (i in 1..<points.size) {
                val point = points[i]
                path.lineTo(point.x, point.y)
            }
        }
    }

    override fun newPoint(x: Float, y: Float) {
        if (points.isEmpty()) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }

        points.add(Point(x, y))
    }

    override fun getDrawData(): DrawCmdData {
        return ErasePathCmdData(
            points = points,
            thicknessLevel = thicknessLevel,
        )
    }

    override fun render(canvas: Canvas, renderContext: RenderContext) {
        val paint = renderContext.get(PaintKey, ::newPaint)
        paint.strokeWidth = renderContext.convertToPx(thicknessLevel)

        canvas.drawPath(path, paint)
    }

    private fun newPaint(): Paint {
        return Paint().apply {
            style = Paint.Style.STROKE
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            color = Color.RED
            isDither = true
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            pathEffect = CornerPathEffect(30F)
        }
    }

    private companion object {
        private val PaintKey = RenderContext.newKey()
    }
}

@Serializable
@SerialName("erase_path")
data class ErasePathCmdData(
    @SerialName("points") val points: List<Point>,
    @SerialName("thickness_level") val thicknessLevel: Float,
) : DrawCmdData() {
    override fun toDrawCmd(): DrawCmd {
        return ErasePathCmd(
            thicknessLevel = thicknessLevel,
            points = points
        )
    }
}