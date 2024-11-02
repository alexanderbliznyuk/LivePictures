package com.blizniuk.livepictures.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.blizniuk.livepictures.domain.graphics.entity.Frame
import com.blizniuk.livepictures.domain.graphics.entity.FrameBuilder
import com.blizniuk.livepictures.domain.graphics.entity.RenderContext
import com.blizniuk.livepictures.domain.graphics.entity.Renderable
import com.blizniuk.livepictures.ui.home.state.CanvasMode

class CanvasView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), DefaultLifecycleObserver {

    private val renderContext = RenderContext(context)

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    private val alphaPaint = Paint().apply {
        color = 0x33FFFFFF
    }

    var frameBuilder: FrameBuilder? = null
        set(value) {
            field?.onDataChangedListener = null
            field = value
            field?.onDataChangedListener = ::postInvalidate

            animationFrame = null
            //postInvalidate()
        }

    var previousFrame: Renderable? = null
        set(value) {
            field = value
            postInvalidate()
        }

    var mode: CanvasMode = CanvasMode.Draw

    var animationFrame: Frame? = null
        set(value) {
            field = value
            postInvalidate()
        }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mode == CanvasMode.Draw && animationFrame == null) {
            frameBuilder?.onTouchEvent(event)
        }
        return true
    }


    override fun onDraw(canvas: Canvas) {
        animationFrame?.let { frame ->
            frame.render(canvas, renderContext)
            return
        }

        previousFrame?.let {
            val saveCount = canvas.saveLayer(0F, 0F, width.toFloat(), height.toFloat(), alphaPaint)
            previousFrame?.render(canvas, renderContext)
            canvas.restoreToCount(saveCount)
        }

        frameBuilder?.render(canvas, renderContext)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        frameBuilder?.onDataChangedListener = ::postInvalidate
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        frameBuilder?.onDataChangedListener = null
    }
}