package com.blizniuk.livepictures.util.gesture

import android.content.Context
import android.graphics.PointF
import android.view.MotionEvent
import com.blizniuk.livepictures.util.GeomUtils

internal class MoveGestureDetector(
    context: Context,
    private val listener: OnMoveGestureListener
) : BaseGestureDetector(context) {

    private val focusExternal = PointF()

    var focusDelta = PointF()
        private set

    val focusX: Float
        get() = focusExternal.x

    val focusY: Float
        get() = focusExternal.y

    interface OnMoveGestureListener {
        fun onMove(detector: MoveGestureDetector): Boolean
        fun onMoveBegin(detector: MoveGestureDetector): Boolean
        fun onMoveEnd(detector: MoveGestureDetector)
    }

    open class SimpleOnMoveGestureListener : OnMoveGestureListener {
        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveBegin(detector: MoveGestureDetector): Boolean {
            return true
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {
            // Do nothing, overridden implementation may be used
        }
    }

    override fun handleStartProgressEvent(actionCode: Int, event: MotionEvent) {
        when (actionCode) {
            MotionEvent.ACTION_DOWN -> {
                resetState() // In case we missed an UP/CANCEL event

                prevEvent = MotionEvent.obtain(event)
                timeDelta = 0

                updateStateByEvent(event)
            }

            MotionEvent.ACTION_MOVE -> isInProgress = listener.onMoveBegin(this)
        }
    }

    override fun handleInProgressEvent(actionCode: Int, event: MotionEvent) {
        when (actionCode) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                listener.onMoveEnd(this)
                resetState()
            }

            MotionEvent.ACTION_MOVE -> {
                updateStateByEvent(event)

                // Only accept the event if our relative pressure is within
                // a certain limit. This can help filter shaky data as a
                // finger is lifted.
                if (mCurrPressure / mPrevPressure > BaseGestureDetector.PRESSURE_THRESHOLD) {
                    val updatePrevious = listener.onMove(this)
                    if (updatePrevious) {
                        prevEvent?.recycle()
                        prevEvent = MotionEvent.obtain(event)
                    }
                }
            }
        }
    }

    override fun updateStateByEvent(curr: MotionEvent) {
        super.updateStateByEvent(curr)

        val prev = prevEvent ?: return

        // Focus intenal
        val prevFocusInternal = GeomUtils.determineFocalPoint(prev)
        val currFocusInternal = GeomUtils.determineFocalPoint(curr)

        // Focus external
        // - Prevent skipping of focus delta when a finger is added or removed
        val mSkipNextMoveEvent = prev.pointerCount != curr.pointerCount
        focusDelta = if (mSkipNextMoveEvent) FOCUS_DELTA_ZERO else PointF(
            currFocusInternal.x - prevFocusInternal.x,
            currFocusInternal.y - prevFocusInternal.y
        )

        // - Don't directly use mFocusInternal (or skipping will occur). Add
        // 	 unskipped delta values to focusExternal instead.
        focusExternal.x += focusDelta.x
        focusExternal.y += focusDelta.y
    }


    companion object {
        private val FOCUS_DELTA_ZERO = PointF()
    }
}