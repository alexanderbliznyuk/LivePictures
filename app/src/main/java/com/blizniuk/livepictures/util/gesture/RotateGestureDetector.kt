package com.blizniuk.livepictures.util.gesture

import android.content.Context
import android.graphics.Matrix
import android.view.MotionEvent

internal class RotateGestureDetector(
    context: Context,
    private val listener: OnRotateGestureListener
) : TwoFingerGestureDetector(context) {
    private var mSloppyGesture: Boolean = false

    /**
     * Return the rotation difference from the previous rotate event to the current
     * event.
     *
     * @return The current rotation //difference in degrees.
     */
    val rotationDegreesDelta: Float
        get() {
            val diffRadians =
                Math.atan2(prevFingerDiffY.toDouble(), prevFingerDiffX.toDouble()) - Math.atan2(
                    currFingerDiffY.toDouble(),
                    currFingerDiffX.toDouble()
                )
            return -(diffRadians * 180 / Math.PI).toFloat()
        }

    override fun handleStartProgressEvent(actionCode: Int, event: MotionEvent) {
        when (actionCode) {
            MotionEvent.ACTION_POINTER_DOWN -> {
                // At least the second finger is on screen now

                resetState() // In case we missed an UP/CANCEL event
                prevEvent = MotionEvent.obtain(event)
                timeDelta = 0

                updateStateByEvent(event)

                // See if we have a sloppy gesture
                mSloppyGesture = isSloppyGesture(event)
                if (!mSloppyGesture) {
                    // No, start gesture now
                    isInProgress = listener.onRotateBegin(this)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (mSloppyGesture) {
                    // See if we still have a sloppy gesture
                    mSloppyGesture = isSloppyGesture(event)
                    if (!mSloppyGesture) {
                        // No, start normal gesture now
                        isInProgress = listener.onRotateBegin(this)
                    }
                }
            }
        }
    }


    override fun handleInProgressEvent(actionCode: Int, event: MotionEvent) {
        when (actionCode) {
            MotionEvent.ACTION_POINTER_UP -> {
                // Gesture ended but
                updateStateByEvent(event)

                if (!mSloppyGesture) {
                    listener.onRotateEnd(this)
                }

                resetState()
            }

            MotionEvent.ACTION_CANCEL -> {
                if (!mSloppyGesture) {
                    listener.onRotateEnd(this)
                }

                resetState()
            }

            MotionEvent.ACTION_MOVE -> {
                updateStateByEvent(event)

                // Only accept the event if our relative pressure is within
                // a certain limit. This can help filter shaky data as a
                // finger is lifted.
                if (mCurrPressure / mPrevPressure > BaseGestureDetector.PRESSURE_THRESHOLD) {
                    val updatePrevious = listener.onRotate(this)
                    if (updatePrevious) {
                        prevEvent!!.recycle()
                        prevEvent = MotionEvent.obtain(event)
                    }
                }
            }
        }
    }

    override fun resetState() {
        super.resetState()
        mSloppyGesture = false
    }

    interface OnRotateGestureListener {
        fun onRotate(detector: RotateGestureDetector): Boolean

        fun onRotateBegin(detector: RotateGestureDetector): Boolean

        fun onRotateEnd(detector: RotateGestureDetector)
    }

    open class SimpleOnRotateGestureListener : OnRotateGestureListener {
        override fun onRotate(detector: RotateGestureDetector): Boolean {
            return false
        }

        override fun onRotateBegin(detector: RotateGestureDetector): Boolean {
            return true
        }

        override fun onRotateEnd(detector: RotateGestureDetector) {
            // Do nothing, overridden implementation may be used
        }
    }
}
