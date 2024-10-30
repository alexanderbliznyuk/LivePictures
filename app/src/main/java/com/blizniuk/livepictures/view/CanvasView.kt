package com.blizniuk.livepictures.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View

class CanvasView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(0x200000FF)
    }
}