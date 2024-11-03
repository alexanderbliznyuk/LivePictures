package com.blizniuk.livepictures.domain.graphics.entity.cmd

import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import com.blizniuk.livepictures.domain.graphics.entity.Point
import com.blizniuk.livepictures.domain.graphics.entity.RenderContext
import com.blizniuk.livepictures.util.GeomUtils.max3
import com.blizniuk.livepictures.util.GeomUtils.min3
import com.blizniuk.livepictures.util.GeomUtils.rotateX
import com.blizniuk.livepictures.util.GeomUtils.rotateY
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class TriangleShapeCmd(
    center: Point,
    radius: Float,
    color: Int,
    thicknessLevel: Float,
    filled: Boolean,
    scale: Float = 1F,
    rotation: Float = 0F,
) : ShapeCmd(
    center = center,
    color = color,
    thicknessLevel = thicknessLevel,
    filled = filled,
    scale,
    rotation
) {
    override val isMovable: Boolean = true
    override val isScalable: Boolean = true
    override val isRotatable: Boolean = true

    private var radius: Float = radius
    private val path = Path()

    override fun bounds(rect: RectF, renderContext: RenderContext) {
        val scaledRadius = radius * scale

        val halfThickness = renderContext.convertToPx(thicknessLevel + 2) / 2

        val leftX = cx - scaledRadius * Cos30 - halfThickness
        val rightX = cx + scaledRadius * Cos30 + halfThickness

        val topY = cy - scaledRadius - halfThickness
        val bottomY = cy + scaledRadius * Cos60 + halfThickness

        val angle = Math.toRadians(rotationAngleDegrees.toDouble())
        val cos = cos(angle).toFloat()
        val sin = sin(angle).toFloat()

        val x1 = rotateX(cx, topY, cx, cy, cos, sin)
        val y1 = rotateY(cx, topY, cx, cy, cos, sin)

        val x2 = rotateX(leftX, bottomY, cx, cy, cos, sin)
        val y2 = rotateY(leftX, bottomY, cx, cy, cos, sin)

        val x3 = rotateX(rightX, bottomY, cx, cy, cos, sin)
        val y3 = rotateY(rightX, bottomY, cx, cy, cos, sin)


        rect.left = min3(x1, x2, x3)
        rect.top = min3(y1, y2, y3)

        rect.right = max3(x1, x2, x3)
        rect.bottom = max3(y1, y2, y3)
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

    override fun copy(): TriangleShapeCmd {
        return TriangleShapeCmd(
            center = Point(cx, cy),
            radius = radius,
            color = color,
            thicknessLevel = thicknessLevel,
            filled = filled,
            scale = scale,
            rotation = rotationAngleDegrees,
        )
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

        canvas.save()
        canvas.rotate(rotationAngleDegrees, cx, cy)
        canvas.drawPath(path, paint)
        canvas.restore()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TriangleShapeCmd) return false
        if (!super.equals(other)) return false

        if (radius != other.radius) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + radius.hashCode()
        return result
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
) : DrawCmdData() {
    override fun toDrawCmd(): DrawCmd {
        return TriangleShapeCmd(
            center = center,
            radius = radius,
            color = color,
            thicknessLevel = thicknessLevel,
            filled = filled,
            scale = scale,
            rotation = rotation
        )
    }
}

