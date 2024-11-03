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
    private var cmdBuilder: DrawCmd? = null
    private var toolData: ToolData? = null
    private val cmdBounds = RectF()

    private var editCmdOriginalData: DrawCmdData? = null

    private val cmdQueue: CmdQueue = CmdQueue(frame.drawCmds)

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

    val id: Long
        get() = frame.id

    val index: Long
        get() = frame.index

    val drawCommands: List<DrawCmd>
        get() = cmdQueue.drawCmds()

    fun isChanged(): Boolean {
        val isChanged = frame.drawCmds != cmdQueue.drawCmds()
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
        return cmdQueue.canUndo()
    }

    fun undo() {
        cmdQueue.undo()
        notifyChange()
    }

    fun canRedo(): Boolean {
        return cmdQueue.canRedo()
    }

    fun redo() {
        cmdQueue.redo()
        notifyChange()
    }

    fun build(): Frame {
        return frame.copy(drawCmds = cmdQueue.drawCmds().map { it.copy() })
    }

    fun confirmChanges() {
        val cmd = cmdToEdit
        val data = editCmdOriginalData
        if (cmd != null && data != null) {
            if (cmdBuilder == cmd) {
                cmdQueue.add(cmd)
            } else {
                cmdQueue.modify(cmd, data)
            }
        }

        cmdToEdit = null
        cmdBuilder = null
        editCmdOriginalData = null
    }

    fun discardChanges() {
        val cmd = cmdToEdit
        val data = editCmdOriginalData
        if (cmd != null && data != null) {
            cmd.restore(data)
        }

        cmdToEdit = null
        cmdBuilder = null
        editCmdOriginalData = null
    }

    private fun createCmdIfPossible(x: Float, y: Float) {
        if (cmdBuilder != null) return
        when (val data = toolData) {
            is ToolData.Pencil -> {
                val cmd = FreePathCmd(
                    color = data.color,
                    thicknessLevel = data.thicknessLevel
                )
                cmdBuilder = cmd
            }

            is ToolData.Erase -> {
                val cmd = ErasePathCmd(
                    thicknessLevel = data.thicknessLevel
                )
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

                cmdBuilder = cmd
                cmdToEdit = cmd
            }

            is ToolData.Square -> {
                val cmd = SquareShapeCmd(
                    center = Point(x, y),
                    halfSize = 100F,
                    color = data.color,
                    thicknessLevel = data.thicknessLevel,
                    filled = data.filled
                )

                cmdBuilder = cmd
                cmdToEdit = cmd
            }

            is ToolData.Triangle -> {
                val cmd = TriangleShapeCmd(
                    center = Point(x, y),
                    radius = 100F,
                    color = data.color,
                    thicknessLevel = data.thicknessLevel,
                    filled = data.filled
                )

                cmdBuilder = cmd
                cmdToEdit = cmd
            }

            null -> Unit
        }
    }

    private fun finishCmdBuilding() {
        cmdBuilder?.let { cmdQueue.add(it) }
        cmdBuilder = null
        notifyChange()
    }

    override fun render(canvas: Canvas, renderContext: RenderContext) {
        cmdQueue.drawCmds().forEach { it.render(canvas, renderContext) }
        cmdBuilder?.render(canvas, renderContext)
        cmdToEdit?.let { drawBorder(canvas, renderContext, it) }
    }

    private fun drawBorder(canvas: Canvas, renderContext: RenderContext, cmd: DrawCmd) {
        cmd.bounds(cmdBounds, renderContext)
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

    private class CmdQueue(initial: List<DrawCmd>) {
        private val cmds: MutableList<DrawCmd> = mutableListOf()

        private val queue: MutableList<Entry> = mutableListOf()
        private var queueHead: Int = -1
        private var queueTotal: Int = -1

        init {
            initial.forEach { cmd -> add(cmd.copy()) }
        }

        fun add(cmd: DrawCmd) {
            cmds += cmd
            addQueueEntry(Entry.New(cmds.lastIndex, cmd.getDrawData()))
        }

        fun modify(cmd: DrawCmd, oldCmdData: DrawCmdData) {
            val index = cmds.indexOf(cmd)
            if (index < 0) return
            addQueueEntry(Entry.Modify(index, oldCmdData, cmd.getDrawData()))
        }

        fun delete(cmd: DrawCmd) {
            val index = cmds.indexOf(cmd)
            if (index < 0) return
            cmds.removeAt(index)
            addQueueEntry(Entry.Delete(index, cmd.getDrawData()))
        }

        fun drawCmds(): List<DrawCmd> {
            return cmds
        }

        private fun addQueueEntry(entry: Entry) {

            queueHead++
            queueTotal = queueHead

            if (queueHead >= queue.size) {
                queue += entry
            } else {
                queue[queueHead] = entry
            }

        }

        fun canUndo(): Boolean {
            return queueHead >= 0
        }

        fun undo() {
            if (!canUndo()) return
            when (val entry = queue[queueHead--]) {
                is Entry.Delete -> {
                    if (entry.index >= cmds.size) {
                        cmds += entry.cmdData.toDrawCmd()
                    } else {
                        cmds.add(entry.index, entry.cmdData.toDrawCmd())
                    }
                }

                is Entry.Modify -> cmds[entry.index].restore(entry.oldCmdData)
                is Entry.New -> cmds.removeAt(entry.index)
            }
        }

        fun canRedo(): Boolean {
            return queueTotal >= 0 && queueTotal > queueHead
        }

        fun redo() {
            if (!canRedo()) return
            when (val entry = queue[++queueHead]) {
                is Entry.Delete -> cmds.removeAt(entry.index)
                is Entry.Modify -> cmds[entry.index].restore(entry.newCmdData)
                is Entry.New -> {
                    if (entry.index >= cmds.size) {
                        cmds += entry.cmdData.toDrawCmd()
                    } else {
                        cmds.add(entry.index, entry.cmdData.toDrawCmd())
                    }
                }
            }
        }

        private sealed interface Entry {
            data class New(val index: Int, val cmdData: DrawCmdData) : Entry
            data class Modify(
                val index: Int,
                val oldCmdData: DrawCmdData,
                val newCmdData: DrawCmdData
            ) : Entry

            data class Delete(val index: Int, val cmdData: DrawCmdData) : Entry
        }
    }
}