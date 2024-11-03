package com.blizniuk.livepictures.domain.graphics.entity.cmd

import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import com.blizniuk.livepictures.domain.graphics.entity.Point
import com.blizniuk.livepictures.domain.graphics.entity.RenderContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.PI
import kotlin.math.cos

class TriangleShapeCmd(
    center: Point,
    radius: Float,
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

    private var radius: Float = radius
    private val path = Path()

    override fun bounds(rect: RectF) {
        val scaledRadius = radius * scale
        val left = cx - scaledRadius * Cos30
        val right = cx + scaledRadius * Cos30

        val top = cy - scaledRadius
        val bottom = cy + scaledRadius * Cos60

        rect.set(left, top, right, bottom)
    }

    override fun restore(drawCmdData: DrawCmdData) {
        if (drawCmdData !is TriangleShapeCmdData) return
        cx = drawCmdData.center.x
        cy = drawCmdData.center.y
        radius = drawCmdData.radius
        thicknessLevel = drawCmdData.thicknessLevel
        color = drawCmdData.color
        filled = drawCmdData.filled
        scale = drawCmdData.scale
        rotationAngleDegrees = drawCmdData.rotation
    }

    override fun getDrawData(): TriangleShapeCmdData {
        return TriangleShapeCmdData(
            center = Point(cx, cy),
            radius = radius,
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

        path.reset()

        val scaledRadius = radius * scale
        path.moveTo(cx - scaledRadius * Cos30, cy + scaledRadius * Cos60)
        path.lineTo(cx + scaledRadius * Cos30, cy + scaledRadius * Cos60)
        path.lineTo(cx, cy - scaledRadius)
        path.close()

        canvas.drawPath(path, paint)


    }

    private companion object {
        private val Cos60 = cos(PI / 3).toFloat()
        private val Cos30 = cos(PI / 6).toFloat()
    }

}

@Serializable
@SerialName("triangle_shape")
data class TriangleShapeCmdData(
    @SerialName("center") val center: Point,
    @SerialName("radius") val radius: Float,
    @SerialName("color") val color: Int,
    @SerialName("thickness_level") val thicknessLevel: Float,
    @SerialName("filled") val filled: Boolean,
    @SerialName("scale") val scale: Float,
    @SerialName("rotation") val rotation: Float,
) : DrawCmdData()
