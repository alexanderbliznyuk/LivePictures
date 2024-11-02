package com.blizniuk.livepictures.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.blizniuk.livepictures.domain.graphics.entity.RenderContext
import com.blizniuk.livepictures.domain.graphics.entity.cmd.DrawCmd
import com.blizniuk.livepictures.domain.graphics.entity.cmd.ErasePathCmd
import kotlin.math.abs
import kotlin.math.max

class CmdPreviewView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    var renderContext: RenderContext? = null
    var cmd: DrawCmd? = null

    private var rect = RectF()

    override fun onDraw(canvas: Canvas) {
        val renderContext = renderContext ?: return
        val cmd = cmd ?: return

        cmd.bounds(rect)
        if (rect.isEmpty) return

        val cmdWidth = rect.width()
        val cmdHeight = rect.height()
        canvas.save()

        if (cmdWidth <= width && cmdHeight <= height) {
            canvas.translate(-rect.left, -rect.top)
            canvas.translate(abs(cmdWidth - width) / 2, abs(cmdHeight - height) / 2)
        } else {
            val scale = max(cmdWidth / width.toFloat(), cmdHeight / height.toFloat())
            val scaledW = width * scale
            val scaledH = height * scale
            canvas.scale(1 / scale, 1 / scale)

            canvas.translate(-rect.left, -rect.top)
            canvas.translate(abs(cmdWidth - scaledW) / 2, abs(cmdHeight - scaledH) / 2)
        }

        if (cmd is ErasePathCmd) {
            canvas.drawColor(0x33FFFF00)
        }

        cmd.render(canvas, renderContext)
        canvas.restore()
    }
}