package com.blizniuk.livepictures.domain.graphics.entity.cmd

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import com.blizniuk.livepictures.domain.graphics.entity.Point
import com.blizniuk.livepictures.domain.graphics.entity.RenderContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.max
import kotlin.math.min

class ErasePathCmd(
    var thicknessLevel: Float,
    points: List<Point> = emptyList(),
    offset: Point = Point.Zero
) : DrawCmd(), FreeDrawableCmd {

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

    override fun bounds(rect: RectF) {
        if (points.isNotEmpty()) {
            rect.set(minX, minY, maxX, maxY)
        } else {
            rect.set(0F, 0F, 0F, 0F)
        }
    }

    override fun moveBy(dx: Float, dy: Float) {
        offsetX += dx
        offsetY += dy
    }

    override fun restore(drawCmdData: DrawCmdData) {
        if (drawCmdData !is ErasePathCmdData) return

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
    }

    override fun copy(): ErasePathCmd {
        return ErasePathCmd(
            points = points.toList(),
            thicknessLevel = thicknessLevel,
            offset = Point(offsetX, offsetY)
        )
    }

    override fun getDrawData(): DrawCmdData {
        return ErasePathCmdData(
            points = points.toList(),
            thicknessLevel = thicknessLevel,
            offset = Point(offsetX, offsetY)
        )
    }

    override fun render(canvas: Canvas, renderContext: RenderContext) {
        val paint = renderContext.get(PaintKey, ::newPaint)
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
            color = Color.BLACK
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            pathEffect = CornerPathEffect(30F)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ErasePathCmd

        if (thicknessLevel != other.thicknessLevel) return false
        if (points != other.points) return false
        if (offsetX != other.offsetX) return false
        if (offsetY != other.offsetY) return false

        return true
    }

    override fun hashCode(): Int {
        var result = thicknessLevel.hashCode()
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
@SerialName("erase_path")
data class ErasePathCmdData(
    @SerialName("points") val points: List<Point>,
    @SerialName("thickness_level") val thicknessLevel: Float,
    @SerialName("offset") val offset: Point,
) : DrawCmdData() {
    override fun toDrawCmd(): DrawCmd {
        return ErasePathCmd(
            thicknessLevel = thicknessLevel,
            points = points,
            offset = offset,
        )
    }
}