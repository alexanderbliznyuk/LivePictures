package com.blizniuk.livepictures.domain.graphics.entity

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import com.blizniuk.livepictures.domain.graphics.ToolData
import com.blizniuk.livepictures.domain.graphics.entity.cmd.CircleShapeCmd
import com.blizniuk.livepictures.domain.graphics.entity.cmd.DrawCmd
import com.blizniuk.livepictures.domain.graphics.entity.cmd.DrawCmdData
import com.blizniuk.livepictures.domain.graphics.entity.cmd.ErasePathCmd
import com.blizniuk.livepictures.domain.graphics.entity.cmd.FreeDrawableCmd
import com.blizniuk.livepictures.domain.graphics.entity.cmd.FreePathCmd
import com.blizniuk.livepictures.domain.graphics.entity.cmd.SquareShapeCmd
import com.blizniuk.livepictures.domain.graphics.entity.cmd.TriangleShapeCmd

class FrameBuilder(private val frame: Frame) : Renderable {
    private val cmds: MutableList<DrawCmd> = frame.drawCmds.map { it.copy() }.toMutableList()
    private var cmdBuilder: DrawCmd? = null
    private var toolData: ToolData? = null
    private val cmdBounds = RectF()

    private var editCmdOriginalData: DrawCmdData? = null
    private var isDiscardCompletely: Boolean = false

    var cmdToEdit: DrawCmd? = null
        set(value) {
            field = value
            editCmdOriginalData = value?.getDrawData()

            notifyChange()
            if (value != null) {
                callbacks?.onCmdEditStarted()
            } else {
                callbacks?.onCmdEditEnded()
            }
        }

    val drawCommands: List<DrawCmd>
        get() = cmds

    val id: Long
        get() = frame.id

    val index: Long
        get() = frame.index

    fun isChanged(): Boolean {
        val isChanged = frame.drawCmds != cmds
        return isChanged
    }

    fun setToolData(toolData: ToolData?) {
        this.toolData = toolData
    }

    var callbacks: Callbacks? = null
        set(value) {
            field = value
            if (cmdToEdit != null) {
                value?.onCmdEditStarted()
            } else {
                value?.onCmdEditEnded()
            }
        }

    fun onTouchEvent(event: MotionEvent) {
        if (cmdToEdit != null) return

        val action = event.actionMasked

        val touchX = event.x
        val touchY = event.y

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                createCmdIfPossible(touchX, touchY)
                if (cmdBuilder is FreeDrawableCmd) {
                    (cmdBuilder as? FreeDrawableCmd)?.newPoint(touchX, touchY)
                    notifyChange()
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (cmdBuilder is FreeDrawableCmd) {
                    (cmdBuilder as? FreeDrawableCmd)?.newPoint(touchX, touchY)
                    notifyChange()
                }
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_POINTER_UP -> {
                if (cmdBuilder is FreeDrawableCmd) {
                    (cmdBuilder as? FreeDrawableCmd)?.newPoint(touchX, touchY)
                    notifyChange()
                }
                finishCmdBuilding()
            }
        }
    }

    fun onMove(dx: Float, dy: Float): Boolean {
        if (cmdToEdit?.isMovable == true) {
            cmdToEdit?.moveBy(dx, dy)
            notifyChange()
            return true
        }

        return false
    }

    fun onScale(scaleBy: Float): Boolean {
        if (cmdToEdit?.isScalable == true) {
            cmdToEdit?.scaleBy(scaleBy)
            notifyChange()
            return true
        }
        return false
    }

    fun onRotate(rotateBy: Float): Boolean {
        if (cmdToEdit?.isRotatable == true) {
            cmdToEdit?.rotateBy(rotateBy)
            notifyChange()
            return true
        }
        return false
    }

    fun canUndo(): Boolean {
        return false
    }

    fun undo() {
    }

    fun canRedo(): Boolean {
        return false
    }

    fun redo() {
    }

    fun build(): Frame {
        return frame.copy(drawCmds = cmds.map { it.copy() })
    }

    fun confirmChanges() {
        cmdToEdit = null
        isDiscardCompletely = false
    }

    fun discardChanges() {
        val cmd = cmdToEdit
        val data = editCmdOriginalData
        if (cmd != null && data != null) {
            if (isDiscardCompletely) {
                cmds.remove(cmd)
            } else {
                cmd.restore(data)
            }
        }
        isDiscardCompletely = false
        cmdToEdit = null
    }

    private fun createCmdIfPossible(x: Float, y: Float) {
        if (cmdBuilder != null) return
        when (val data = toolData) {
            is ToolData.Pencil -> {
                val cmd = FreePathCmd(
                    color = data.color,
                    thicknessLevel = data.thicknessLevel
                )
                cmds += cmd
                cmdBuilder = cmd
            }

            is ToolData.Erase -> {
                val cmd = ErasePathCmd(
                    thicknessLevel = data.thicknessLevel
                )
                cmds += cmd
                cmdBuilder = cmd
            }


            is ToolData.Circle -> {
                val cmd = CircleShapeCmd(
                    center = Point(x, y),
                    circleRadius = 100F,
                    color = data.color,
                    thicknessLevel = data.thicknessLevel,
                    filled = data.filled
                )

                cmds += cmd
                cmdToEdit = cmd
                isDiscardCompletely = true
            }

            is ToolData.Square -> {
                val cmd = SquareShapeCmd(
                    center = Point(x, y),
                    halfSize = 100F,
                    color = data.color,
                    thicknessLevel = data.thicknessLevel,
                    filled = data.filled
                )

                cmds += cmd
                cmdToEdit = cmd
                isDiscardCompletely = true
            }

            is ToolData.Triangle -> {
                val cmd = TriangleShapeCmd(
                    center = Point(x, y),
                    radius = 100F,
                    color = data.color,
                    thicknessLevel = data.thicknessLevel,
                    filled = data.filled
                )

                cmds += cmd
                cmdToEdit = cmd
                isDiscardCompletely = true
            }

            null -> Unit
        }
    }

    private fun finishCmdBuilding() {
        cmdBuilder = null
    }

    override fun render(canvas: Canvas, renderContext: RenderContext) {
        cmds.forEach { it.render(canvas, renderContext) }
        cmdToEdit?.let { drawBorder(canvas, renderContext, it) }
    }

    private fun drawBorder(canvas: Canvas, renderContext: RenderContext, cmd: DrawCmd) {
        cmd.bounds(cmdBounds)
        if (!cmdBounds.isEmpty) {
            val fillPaint = renderContext.get(FillPaintKey) {
                createFillPaint()
            }
            canvas.drawRect(cmdBounds, fillPaint)

            val strokePaint = renderContext.get(BorderPaintKey) {
                val borderSize = renderContext.convertToPx(1F)
                createBorderPaint(borderSize)
            }
            canvas.drawRect(cmdBounds, strokePaint)
        }
    }

    private fun createBorderPaint(borderWidth: Float): Paint {
        return Paint().apply {
            isAntiAlias = true
            color = 0xFF0078D7.toInt()
            style = Paint.Style.STROKE
            strokeWidth = borderWidth
        }
    }

    private fun createFillPaint(): Paint {
        return Paint().apply {
            isAntiAlias = true
            color = 0x330078D7
            style = Paint.Style.FILL
        }
    }

    private fun notifyChange() {
        callbacks?.onChanged()
    }

    private companion object {
        private val BorderPaintKey = RenderContext.newKey()
        private val FillPaintKey = RenderContext.newKey()
    }

    interface Callbacks {
        fun onChanged()
        fun onCmdEditStarted()
        fun onCmdEditEnded()
    }
}