package com.blizniuk.livepictures.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.blizniuk.livepictures.domain.graphics.entity.Frame
import com.blizniuk.livepictures.domain.graphics.entity.FrameBuilder
import com.blizniuk.livepictures.domain.graphics.entity.RenderContext
import com.blizniuk.livepictures.domain.graphics.entity.Renderable
import com.blizniuk.livepictures.ui.home.state.CanvasMode
import com.blizniuk.livepictures.util.gesture.MoveGestureDetector
import com.blizniuk.livepictures.util.gesture.RotateGestureDetector

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
        set(value) {
            field = value
            if (value == CanvasMode.Draw) {
                animationFrame = null
            }
        }

    var animationFrame: Frame? = null
        set(value) {
            field = value
            postInvalidate()
        }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mode == CanvasMode.Draw && animationFrame == null) {
            frameBuilder?.onTouchEvent(event)

            rotateGestureDetector.onTouchEvent(event)
            scaleGestureDetector.onTouchEvent(event)
            moveGestureDetector.onTouchEvent(event)
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
        val saveCount = canvas.saveLayer(0F, 0F, width.toFloat(), height.toFloat(), null)
        frameBuilder?.render(canvas, renderContext)
        canvas.restoreToCount(saveCount)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        frameBuilder?.onDataChangedListener = ::postInvalidate
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        frameBuilder?.onDataChangedListener = null
    }


    private val scaleGestureDetector: ScaleGestureDetector =
        ScaleGestureDetector(context, OverlayScaleGestureListener())
    private val moveGestureDetector = MoveGestureDetector(context, OverlayMoveGestureListener())
    private val rotateGestureDetector =
        RotateGestureDetector(context, OverlayRotateGestureListener())

    private inner class OverlayScaleGestureListener :
        ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            println("AABBCC scale ${detector.scaleFactor}")
            return true
        }
    }

    private inner class OverlayRotateGestureListener :
        RotateGestureDetector.SimpleOnRotateGestureListener() {
        override fun onRotate(detector: RotateGestureDetector): Boolean {
            println("AABBCC rotate ${detector.rotationDegreesDelta}")
            return true
        }
    }

    private inner class OverlayMoveGestureListener :
        MoveGestureDetector.SimpleOnMoveGestureListener() {
        override fun onMove(detector: MoveGestureDetector): Boolean {
            println("AABBCC move ${detector.focusDelta}")
            return true
        }
    }
}