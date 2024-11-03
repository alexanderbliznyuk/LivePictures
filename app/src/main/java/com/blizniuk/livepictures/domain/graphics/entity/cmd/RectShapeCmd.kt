package com.blizniuk.livepictures.domain.graphics.entity.cmd

import android.graphics.Canvas
import android.graphics.RectF
import com.blizniuk.livepictures.domain.graphics.entity.Point
import com.blizniuk.livepictures.domain.graphics.entity.RenderContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class RectShapeCmd(
    center: Point,
    halfSize: Float,
    color: Int,
    thicknessLevel: Float,
    filled: Boolean,
) : ShapeCmd(
    center = center,
    color = color,
    thicknessLevel = thicknessLevel,
    filled = filled,
) {
    override val isMovable: Boolean = true
    override val isScalable: Boolean = true
    override val isRotatable: Boolean = true

    private var halfSize: Float = halfSize

    override fun bounds(rect: RectF) {
        rect.set(cx - halfSize, cy - halfSize, cx + halfSize, cy + halfSize)
    }

    override fun restore(drawCmdData: DrawCmdData) {
        if (drawCmdData !is SquareShapeCmdData) return
        cx = drawCmdData.center.x
        cy = drawCmdData.center.y
        halfSize = drawCmdData.halfSize
        thicknessLevel = drawCmdData.thicknessLevel
        color = drawCmdData.color
        filled = drawCmdData.filled
        scale = drawCmdData.scale
        rotationAngleDegrees = drawCmdData.rotation
    }

    override fun getDrawData(): SquareShapeCmdData {
        return SquareShapeCmdData(
            center = Point(cx, cy),
            halfSize = halfSize,
            color = color,
            thicknessLevel = thicknessLevel,
            filled = filled,
            scale = scale,
            rotation = rotationAngleDegrees
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
        val scaledSize = halfSize * scale
        canvas.drawRect(cx - scaledSize, cy - scaledSize, cx + scaledSize, cy + scaledSize, paint)
    }
}

@Serializable
@SerialName("square_shape")
data class SquareShapeCmdData(
    @SerialName("center") val center: Point,
    @SerialName("half_size") val halfSize: Float,
    @SerialName("color") val color: Int,
    @SerialName("thickness_level") val thicknessLevel: Float,
    @SerialName("filled") val filled: Boolean,
    @SerialName("scale") val scale: Float,
    @SerialName("rotation") val rotation: Float,
) : DrawCmdData()
