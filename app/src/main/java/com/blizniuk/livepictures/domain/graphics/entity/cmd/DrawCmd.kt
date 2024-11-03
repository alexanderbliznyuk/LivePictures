package com.blizniuk.livepictures.domain.graphics.entity.cmd

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.blizniuk.livepictures.domain.graphics.entity.Point
import com.blizniuk.livepictures.domain.graphics.entity.RenderContext
import com.blizniuk.livepictures.domain.graphics.entity.Renderable

sealed class DrawCmd : Renderable {


    //fun render(canvas: Canvas, renderContext: RenderContext)
    open fun getDrawData(): DrawCmdData {
        TODO()
    }

    open fun bounds(rect: RectF) {
    }

    open fun restore(drawCmdData: DrawCmdData) {
    }


    open val isScalable: Boolean = false
    open val isMovable: Boolean = false
    open val isRotatable: Boolean = false

    override fun render(canvas: Canvas, renderContext: RenderContext) {
    }

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


sealed class ShapeCmd(center: Point, color: Int, thicknessLevel: Float, filled: Boolean) :
    DrawCmd() {
    override val isMovable: Boolean = true
    override val isScalable: Boolean = true

    var filled: Boolean = filled
    var color: Int = color
    var thicknessLevel: Float = thicknessLevel

    protected var scale: Float = 1F
    protected var rotationAngleDegrees: Float = 0F

    protected var cx: Float = center.x
    protected var cy: Float = center.y

    override fun moveBy(dx: Float, dy: Float) {
        cx += dx
        cy += cy
    }

    override fun scaleBy(scale: Float) {
        this.scale *= scale
    }

    override fun rotateBy(degrees: Float) {
        rotationAngleDegrees += degrees
    }

    protected fun createPaint(filled: Boolean): Paint {
        val style = if (filled) Paint.Style.FILL else Paint.Style.STROKE
        return Paint().apply {
            this.style = style
            isAntiAlias = true
        }
    }

    companion object {
        val StrokePaintKey = RenderContext.newKey()
        val FillPaintKey = RenderContext.newKey()
    }
}









