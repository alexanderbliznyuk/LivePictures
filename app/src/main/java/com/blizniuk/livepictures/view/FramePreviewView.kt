package com.blizniuk.livepictures.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import com.blizniuk.livepictures.domain.graphics.entity.RenderContext
import com.blizniuk.livepictures.domain.graphics.entity.Renderable
import kotlin.math.abs
import kotlin.math.max

class FramePreviewView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var canvasWidth: Int = 0
    var canvasHeight: Int = 0
    var renderContext: RenderContext? = null

    var frame: Renderable? = null
    override fun onDraw(canvas: Canvas) {
        if (canvasWidth == 0 || canvasHeight == 0) return
        val renderContext = renderContext ?: return
        val frame = frame ?: return

        val scale = max(canvasWidth / width.toFloat(), canvasHeight / height.toFloat())
        val scaledW = width * scale
        val scaledH = height * scale

        canvas.save()
        canvas.scale(1 / scale, 1 / scale)
        canvas.translate(abs(canvasWidth - scaledW) / 2, abs(canvasHeight - scaledH) / 2)
        frame.render(canvas, renderContext)
        canvas.restore()
    }
}