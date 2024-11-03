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
) : ShapeCmd(center = center, color = color, thicknessLevel = thicknessLevel, filled = filled) {

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
) : DrawCmdData()
