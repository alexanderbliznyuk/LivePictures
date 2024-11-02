package com.blizniuk.livepictures.domain.graphics.entity

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import com.blizniuk.livepictures.domain.graphics.ToolData
import com.blizniuk.livepictures.domain.graphics.entity.cmd.DrawCmd
import com.blizniuk.livepictures.domain.graphics.entity.cmd.ErasePathCmd
import com.blizniuk.livepictures.domain.graphics.entity.cmd.FreeDrawableCmd
import com.blizniuk.livepictures.domain.graphics.entity.cmd.FreePathCmd

class FrameBuilder(private val frame: Frame) : Renderable {
    private val cmds: MutableList<DrawCmd> = frame.drawCmds.toMutableList()
    private var currentCmd: DrawCmd? = null
    private var toolData: ToolData? = null

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

    var onDataChangedListener: (() -> Unit)? = null

    fun onTouchEvent(event: MotionEvent) {
        val action = event.actionMasked

        val touchX = event.x
        val touchY = event.y

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                createCmdIfPossible()
                if (currentCmd is FreeDrawableCmd) {
                    (currentCmd as? FreeDrawableCmd)?.newPoint(touchX, touchY)
                    onDataChangedListener?.invoke()
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (currentCmd is FreeDrawableCmd) {
                    (currentCmd as? FreeDrawableCmd)?.newPoint(touchX, touchY)
                    onDataChangedListener?.invoke()
                }
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_POINTER_UP -> {
                if (currentCmd is FreeDrawableCmd) {
                    (currentCmd as? FreeDrawableCmd)?.newPoint(touchX, touchY)
                    onDataChangedListener?.invoke()
                }
                addCmdIfPossible()
            }
        }
    }

    private fun createCmdIfPossible() {
        if (currentCmd != null) return
        currentCmd = when (val data = toolData) {
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
        val cmd = currentCmd
        if (cmd != null) {
            cmds += cmd
        }

        currentCmd = null
    }


    private val rect = RectF()


    override fun render(canvas: Canvas, renderContext: RenderContext) {
        cmds.forEach { it.render(canvas, renderContext) }
        currentCmd?.render(canvas, renderContext)
    }

    private fun drawBorder(canvas: Canvas, renderContext: RenderContext, cmd: DrawCmd) {
        cmd.bounds(rect)
        if (!rect.isEmpty) {
            val paint = renderContext.get(BorderPaintKey) {
                val borderSize = renderContext.convertToPx(2F)
                createBorderPaint(borderSize)
            }
            canvas.drawRect(rect, paint)
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

    fun canUndo(): Boolean {
        return false
    }

    fun undo() {}

    fun canRedo(): Boolean {
        return false
    }

    fun redo() {
    }

    fun build(): Frame {
        return frame.copy(drawCmds = cmds)
    }

    private companion object {
        private val BorderPaintKey = RenderContext.newKey()
    }
}