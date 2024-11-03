package com.blizniuk.livepictures.domain.graphics.entity

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import com.blizniuk.livepictures.domain.graphics.ToolData
import com.blizniuk.livepictures.domain.graphics.entity.cmd.DrawCmd
import com.blizniuk.livepictures.domain.graphics.entity.cmd.DrawCmdData
import com.blizniuk.livepictures.domain.graphics.entity.cmd.ErasePathCmd
import com.blizniuk.livepictures.domain.graphics.entity.cmd.FreeDrawableCmd
import com.blizniuk.livepictures.domain.graphics.entity.cmd.FreePathCmd

class FrameBuilder(private val frame: Frame) : Renderable {
    private val cmds: MutableList<DrawCmd> = frame.drawCmds.toMutableList()
    private var cmdBuilder: DrawCmd? = null
    private var toolData: ToolData? = null
    private val cmdBounds = RectF()

    private var editCmdOriginalData: DrawCmdData? = null
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
                createCmdIfPossible()
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
                addCmdIfPossible()
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
        return frame.copy(drawCmds = cmds)
    }

    fun confirmChanges() {
        cmdToEdit = null
    }

    fun discardChanges() {
        val cmd = cmdToEdit
        val data = editCmdOriginalData
        if (cmd != null && data != null) {
            cmd.restore(data)
        }

        cmdToEdit = null
    }

    private fun createCmdIfPossible() {
        if (cmdBuilder != null) return
        cmdBuilder = when (val data = toolData) {
            is ToolData.Pencil -> FreePathCmd(
                color = data.color,
                thicknessLevel = data.thicknessLevel
            )

            is ToolData.Erase -> ErasePathCmd(
                thicknessLevel = data.thicknessLevel
            )

            null -> return
        }
    }

    private fun addCmdIfPossible() {
        val cmd = cmdBuilder
        if (cmd != null) {
            cmds += cmd
        }

        cmdBuilder = null
    }

    override fun render(canvas: Canvas, renderContext: RenderContext) {
        cmds.forEach { it.render(canvas, renderContext) }
        cmdBuilder?.render(canvas, renderContext)

        cmdToEdit?.let { drawBorder(canvas, renderContext, it) }
    }

    private fun drawBorder(canvas: Canvas, renderContext: RenderContext, cmd: DrawCmd) {
        cmd.bounds(cmdBounds)
        if (!cmdBounds.isEmpty) {
            val paint = renderContext.get(BorderPaintKey) {
                val borderSize = renderContext.convertToPx(2F)
                createBorderPaint(borderSize)
            }
            canvas.drawRect(cmdBounds, paint)
        }
    }

    private fun createBorderPaint(borderWidth: Float): Paint {
        return Paint().apply {
            isAntiAlias = true
            color = Color.GREEN
            style = Paint.Style.STROKE
            strokeWidth = borderWidth
        }
    }

    private fun notifyChange() {
        callbacks?.onChanged()
    }

    private companion object {
        private val BorderPaintKey = RenderContext.newKey()
    }

    interface Callbacks {
        fun onChanged()
        fun onCmdEditStarted()
        fun onCmdEditEnded()
    }
}