package com.blizniuk.livepictures.domain.graphics.entity.cmd

import android.graphics.Canvas
import android.graphics.RectF
import com.blizniuk.livepictures.domain.graphics.entity.Point
import com.blizniuk.livepictures.domain.graphics.entity.RenderContext
import com.blizniuk.livepictures.util.GeomUtils.max4
import com.blizniuk.livepictures.util.GeomUtils.min4
import com.blizniuk.livepictures.util.GeomUtils.rotateX
import com.blizniuk.livepictures.util.GeomUtils.rotateY
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.cos
import kotlin.math.sin

class SquareShapeCmd(
    center: Point,
    halfSize: Float,
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
    scale = scale,
    rotation = rotation,
) {
    override val isMovable: Boolean = true
    override val isScalable: Boolean = true
    override val isRotatable: Boolean = true

    private var halfSize: Float = halfSize

    override fun bounds(rect: RectF) {
        val scaledSize = halfSize * scale
        rect.set(cx - scaledSize, cy - scaledSize, cx + scaledSize, cy + scaledSize)
        rotateRect(rect, rotationAngleDegrees)
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

    override fun copy(): SquareShapeCmd {
        return SquareShapeCmd(
            center = Point(cx, cy),
            halfSize = halfSize,
            color = color,
            thicknessLevel = thicknessLevel,
            filled = filled,
            scale = scale,
            rotation = rotationAngleDegrees,
        )
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
        canvas.save()
        canvas.rotate(rotationAngleDegrees, cx, cy)
        canvas.drawRect(cx - scaledSize, cy - scaledSize, cx + scaledSize, cy + scaledSize, paint)
        canvas.restore()
    }

    private fun rotateRect(rect: RectF, angleDegrees: Float) {
        val cx = rect.centerX()
        val cy = rect.centerY()

        val left = rect.left
        val right = rect.right
        val top = rect.top
        val bottom = rect.bottom

        val angle = Math.toRadians(angleDegrees.toDouble())
        val cos = cos(angle).toFloat()
        val sin = sin(angle).toFloat()

        val x1 = rotateX(left, top, cx, cy, cos, sin)
        val y1 = rotateY(left, top, cx, cy, cos, sin)

        val x2 = rotateX(right, top, cx, cy, cos, sin)
        val y2 = rotateY(right, top, cx, cy, cos, sin)

        val x3 = rotateX(right, bottom, cx, cy, cos, sin)
        val y3 = rotateY(right, bottom, cx, cy, cos, sin)

        val x4 = rotateX(left, bottom, cx, cy, cos, sin)
        val y4 = rotateY(left, bottom, cx, cy, cos, sin)

        rect.left = min4(x1, x2, x3, x4)
        rect.top = min4(y1, y2, y3, y4)

        rect.right = max4(x1, x2, x3, x4)
        rect.bottom = max4(y1, y2, y3, y4)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SquareShapeCmd) return false
        if (!super.equals(other)) return false

        if (halfSize != other.halfSize) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + halfSize.hashCode()
        return result
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
) : DrawCmdData() {
    override fun toDrawCmd(): DrawCmd {
        return SquareShapeCmd(
            center = center,
            halfSize = halfSize,
            color = color,
            thicknessLevel = thicknessLevel,
            filled = filled,
            scale = scale,
            rotation = rotation
        )
    }

}

