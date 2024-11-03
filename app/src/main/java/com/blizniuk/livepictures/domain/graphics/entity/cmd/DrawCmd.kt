package com.blizniuk.livepictures.domain.graphics.entity.cmd

import android.graphics.Canvas
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


sealed class ShapeCmd : DrawCmd()


data class CircleShapeCmd(
    val center: Point,
    val radius: Float,
    val color: Int,
    val thicknessLevel: Float,
) : ShapeCmd()


data class RectShapeCmd(
    val topLeft: Point,
    val bottomRight: Point,
    val color: Int,
    val thicknessLevel: Float,
) : ShapeCmd()








