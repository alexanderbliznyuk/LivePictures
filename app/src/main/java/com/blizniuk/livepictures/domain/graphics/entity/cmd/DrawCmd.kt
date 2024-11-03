package com.blizniuk.livepictures.domain.graphics.entity.cmd

import android.graphics.Paint
import android.graphics.RectF
import com.blizniuk.livepictures.domain.graphics.entity.Point
import com.blizniuk.livepictures.domain.graphics.entity.RenderContext
import com.blizniuk.livepictures.domain.graphics.entity.Renderable

sealed class DrawCmd : Renderable {
    //fun render(canvas: Canvas, renderContext: RenderContext)
    abstract fun getDrawData(): DrawCmdData
    abstract fun bounds(rect: RectF)
    abstract fun restore(drawCmdData: DrawCmdData)
    abstract fun copy(): DrawCmd

    open val isScalable: Boolean = false
    open val isMovable: Boolean = false
    open val isRotatable: Boolean = false

    open fun moveBy(dx: Float, dy: Float) {
    }

    open fun rotateBy(degrees: Float) {
    }

    open fun scaleBy(scale: Float) {
    }
}

interface FreeDrawableCmd {
    fun newPoint(x: Float, y: Float)
}


sealed class ShapeCmd(
    center: Point,
    color: Int,
    thicknessLevel: Float,
    filled: Boolean,
    scale: Float,
    rotation: Float,
) :
    DrawCmd() {
    override val isMovable: Boolean = true
    override val isScalable: Boolean = true

    var filled: Boolean = filled
    var color: Int = color
    var thicknessLevel: Float = thicknessLevel

    protected var scale: Float = scale
    protected var rotationAngleDegrees: Float = rotation

    protected var cx: Float = center.x
    protected var cy: Float = center.y

    override fun moveBy(dx: Float, dy: Float) {
        cx += dx
        cy += dy
    }

    override fun scaleBy(scale: Float) {
        this.scale *= scale
    }

    override fun rotateBy(degrees: Float) {
        if (isRotatable) {
            rotationAngleDegrees += degrees
        }
    }

    protected fun createPaint(filled: Boolean): Paint {
        val style = if (filled) Paint.Style.FILL else Paint.Style.STROKE
        return Paint().apply {
            this.style = style
            isAntiAlias = true
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ShapeCmd) return false

        if (filled != other.filled) return false
        if (color != other.color) return false
        if (thicknessLevel != other.thicknessLevel) return false
        if (scale != other.scale) return false
        if (rotationAngleDegrees != other.rotationAngleDegrees) return false
        if (cx != other.cx) return false
        if (cy != other.cy) return false

        return true
    }

    override fun hashCode(): Int {
        var result = filled.hashCode()
        result = 31 * result + color
        result = 31 * result + thicknessLevel.hashCode()
        result = 31 * result + scale.hashCode()
        result = 31 * result + rotationAngleDegrees.hashCode()
        result = 31 * result + cx.hashCode()
        result = 31 * result + cy.hashCode()
        return result
    }


    companion object {
        val StrokePaintKey = RenderContext.newKey()
        val FillPaintKey = RenderContext.newKey()
    }
}









