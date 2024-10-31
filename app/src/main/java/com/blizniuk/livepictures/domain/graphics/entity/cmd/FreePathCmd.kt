package com.blizniuk.livepictures.domain.graphics.entity.cmd

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.icu.text.DecimalFormat
import com.blizniuk.livepictures.domain.graphics.entity.Point
import com.blizniuk.livepictures.domain.graphics.entity.RenderContext
import com.blizniuk.livepictures.domain.graphics.entity.Renderable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

class FreePathCmd(
    var color: Int,
    var thicknessLevel: Float,
    points: List<Point> = emptyList(),
) : DrawCmd(), Renderable, FreeDrawableCmd {

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
        return FreePathCmdData(
            points = points,
            color = color,
            thicknessLevel = thicknessLevel,
        )
    }

    override fun render(canvas: Canvas, renderContext: RenderContext) {
        val paint = renderContext.get(PaintKey, ::newPaint)
        paint.color = color
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
            pathEffect = CornerPathEffect(30F)
        }
    }

    private companion object {
        private val PaintKey = RenderContext.newKey()
    }
}


@Serializable
@SerialName("free_path")
data class FreePathCmdData(
    @SerialName("points") val points: List<Point>,
    @SerialName("color") val color: Int,
    @SerialName("thickness_level") val thicknessLevel: Float,
) : DrawCmdData() {
    override fun toDrawCmd(): DrawCmd {
        return FreePathCmd(
            color = color,
            thicknessLevel = thicknessLevel,
            points = points
        )
    }
}