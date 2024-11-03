package com.blizniuk.livepictures.domain.graphics.entity.cmd

import android.graphics.Canvas
import android.graphics.RectF
import com.blizniuk.livepictures.domain.graphics.entity.Point
import com.blizniuk.livepictures.domain.graphics.entity.RenderContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class CircleShapeCmd(
    center: Point,
    circleRadius: Float,
    color: Int,
    thicknessLevel: Float,
    filled: Boolean,
    scale: Float = 1F
) : ShapeCmd(
    center = center,
    color = color,
    thicknessLevel = thicknessLevel,
    filled = filled,
    scale = scale,
    rotation = 0F
) {

    override val isMovable: Boolean = true
    override val isScalable: Boolean = true

    private var radius: Float = circleRadius

    override fun bounds(rect: RectF) {
        val scaledRadius = radius * scale
        rect.set(cx - scaledRadius, cy - scaledRadius, cx + scaledRadius, cy + scaledRadius)
    }

    override fun restore(drawCmdData: DrawCmdData) {
        if (drawCmdData !is CircleShapeCmdData) return
        cx = drawCmdData.center.x
        cy = drawCmdData.center.y
        radius = drawCmdData.radius
        scale = drawCmdData.scale
        thicknessLevel = drawCmdData.thicknessLevel
        filled = drawCmdData.filled
        color = drawCmdData.color
    }

    override fun copy(): CircleShapeCmd {
        return CircleShapeCmd(
            center = Point(cx, cy),
            circleRadius = radius,
            color = color,
            thicknessLevel = thicknessLevel,
            filled = filled,
            scale = scale
        )
    }

    override fun getDrawData(): CircleShapeCmdData {
        return CircleShapeCmdData(
            center = Point(cx, cy),
            radius = radius,
            color = color,
            thicknessLevel = thicknessLevel,
            scale = scale,
            filled = filled,
        )
    }

    override fun render(canvas: Canvas, renderContext: RenderContext) {
        val paint = if (filled) {
            renderContext.get(FillPaintKey) { createPaint(true) }
        } else {
            renderContext.get(StrokePaintKey) { createPaint(false) }
                .apply {
                    strokeWidth = renderContext.convertToPx(thicknessLevel)
                }
        }

        paint.color = color
        canvas.drawCircle(cx, cy, radius * scale, paint)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CircleShapeCmd) return false
        if (!super.equals(other)) return false

        if (radius != other.radius) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + radius.hashCode()
        return result
    }
}


@Serializable
@SerialName("circle_shape")
data class CircleShapeCmdData(
    @SerialName("center") val center: Point,
    @SerialName("radius") val radius: Float,
    @SerialName("color") val color: Int,
    @SerialName("thickness_level") val thicknessLevel: Float,
    @SerialName("scale") val scale: Float,
    @SerialName("filled") val filled: Boolean,
) : DrawCmdData() {
    override fun toDrawCmd(): DrawCmd {
        return CircleShapeCmd(
            center = center,
            circleRadius = radius,
            color = color,
            thicknessLevel = thicknessLevel,
            filled = filled,
            scale = scale
        )
    }
}
