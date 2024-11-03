package com.blizniuk.livepictures.domain.graphics.entity.cmd

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.blizniuk.livepictures.domain.graphics.entity.Point
import com.blizniuk.livepictures.domain.graphics.entity.RenderContext
import com.blizniuk.livepictures.domain.graphics.entity.Renderable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.max
import kotlin.math.min

class FreePathCmd(
    var color: Int,
    var thicknessLevel: Float,
    points: List<Point> = emptyList(),
    offset: Point = Point.Zero,
) : DrawCmd(), Renderable, FreeDrawableCmd {

    override val isMovable: Boolean = true

    private val points: MutableList<Point> = mutableListOf()
    private val path = Path()

    private var minX: Float = Float.MAX_VALUE
    private var minY: Float = Float.MAX_VALUE
    private var maxX: Float = Float.MIN_VALUE
    private var maxY: Float = Float.MIN_VALUE

    private var offsetX: Float = offset.x
    private var offsetY: Float = offset.y

    init {
        points.forEach { newPoint(it.x, it.y) }
    }

    override fun newPoint(x: Float, y: Float) {
        if (points.isEmpty()) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }

        minX = min(minX, x)
        minY = min(minY, y)

        maxX = max(maxX, x)
        maxY = max(maxY, y)

        points.add(Point(x, y))
    }

    override fun restore(drawCmdData: DrawCmdData) {
        if (drawCmdData !is FreePathCmdData) return

        points.clear()
        path.reset()
        minX = Float.MAX_VALUE
        minY = Float.MAX_VALUE
        maxX = Float.MIN_VALUE
        maxY = Float.MIN_VALUE

        drawCmdData.points.forEach { newPoint(it.x, it.y) }

        offsetX = drawCmdData.offset.x
        offsetY = drawCmdData.offset.y
        thicknessLevel = drawCmdData.thicknessLevel
        color = drawCmdData.color
    }

    override fun copy(): FreePathCmd {
        return FreePathCmd(
            points = points.toList(),
            color = color,
            thicknessLevel = thicknessLevel,
            offset = Point(offsetX, offsetY)
        )
    }


    override fun bounds(rect: RectF, renderContext: RenderContext) {
        if (points.isNotEmpty()) {
            val halfThickness = renderContext.convertToPx(thicknessLevel + 2) / 2
            rect.set(
                minX - halfThickness,
                minY - halfThickness,
                maxX + halfThickness,
                maxY + halfThickness
            )
            rect.offset(offsetX, offsetY)
        } else {
            rect.set(0F, 0F, 0F, 0F)
        }
    }

    override fun moveBy(dx: Float, dy: Float) {
        offsetX += dx
        offsetY += dy
    }

    override fun getDrawData(): DrawCmdData {
        return FreePathCmdData(
            points = points.toList(),
            color = color,
            thicknessLevel = thicknessLevel,
            offset = Point(offsetX, offsetY)
        )
    }

    override fun render(canvas: Canvas, renderContext: RenderContext) {
        val paint = renderContext.get(PaintKey, ::newPaint)
        paint.color = color
        paint.strokeWidth = renderContext.convertToPx(thicknessLevel)

        canvas.save()
        canvas.translate(offsetX, offsetY)
        canvas.drawPath(path, paint)

        canvas.restore()
    }

    private fun newPaint(): Paint {
        return Paint().apply {
            style = Paint.Style.STROKE
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            color = Color.RED
            pathEffect = CornerPathEffect(30F)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FreePathCmd

        if (color != other.color) return false
        if (thicknessLevel != other.thicknessLevel) return false
        if (points != other.points) return false
        if (offsetX != other.offsetX) return false
        if (offsetY != other.offsetY) return false

        return true
    }

    override fun hashCode(): Int {
        var result = color
        result = 31 * result + thicknessLevel.hashCode()
        result = 31 * result + points.hashCode()
        result = 31 * result + offsetX.hashCode()
        result = 31 * result + offsetY.hashCode()
        return result
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
    @SerialName("offset") val offset: Point
) : DrawCmdData() {
    override fun toDrawCmd(): DrawCmd {
        return FreePathCmd(
            color = color,
            thicknessLevel = thicknessLevel,
            points = points,
            offset = offset,
        )
    }
}