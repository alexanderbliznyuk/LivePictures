package com.blizniuk.livepictures.util.gesture

import android.content.Context
import android.content.res.Configuration
import android.view.MotionEvent


internal abstract class BaseGestureDetector(protected val context: Context) {
    /**
     * Returns `true` if a gesture is currently in progress.
     *
     * @return `true` if a gesture is currently in progress, `false` otherwise.
     */
    var isInProgress: Boolean = false
        protected set

    protected var prevEvent: MotionEvent? = null
    protected var currEvent: MotionEvent? = null

    protected var mCurrPressure: Float = 0F
    protected var mPrevPressure: Float = 0F

    /**
     * Return the time difference in milliseconds between the previous accepted
     * GestureDetector event and the current GestureDetector event.
     *
     * @return Time difference since the last move event in milliseconds.
     */
    var timeDelta: Long = 0
        protected set

    /**
     * Return the event time of the current GestureDetector event being
     * processed.
     *
     * @return Current GestureDetector event time in milliseconds.
     */
    val eventTime: Long
        get() = currEvent?.eventTime ?: 0L

    /**
     * All gesture detectors need to be called through this method to be able to
     * detect gestures. This method delegates work to handler methods
     * (handleStartProgressEvent, handleInProgressEvent) implemented in
     * extending classes.
     *
     * @param event
     * @return
     */
    fun onTouchEvent(event: MotionEvent): Boolean {
        val actionCode = event.action and MotionEvent.ACTION_MASK
        if (!isInProgress) {
            handleStartProgressEvent(actionCode, event)
        } else {
            handleInProgressEvent(actionCode, event)
        }
        return true
    }

    /**
     * Called when the current event occurred when NO gesture is in progress
     * yet. The handling in this implementation may set the gesture in progress
     * (via mGestureInProgress) or out of progress
     *
     * @param actionCode
     * @param event
     */
    protected abstract fun handleStartProgressEvent(actionCode: Int, event: MotionEvent)

    /**
     * Called when the current event occurred when a gesture IS in progress. The
     * handling in this implementation may set the gesture out of progress (via
     * mGestureInProgress).
     *
     * @param actionCode
     * @param event
     */
    protected abstract fun handleInProgressEvent(actionCode: Int, event: MotionEvent)


    protected open fun updateStateByEvent(curr: MotionEvent) {
        val prev = prevEvent
        currEvent?.recycle()
        currEvent = MotionEvent.obtain(curr)


        // Delta time
        timeDelta = curr.eventTime - (prev?.eventTime ?: 0)

        // Pressure
        mCurrPressure = curr.getPressure(curr.actionIndex)
        mPrevPressure = prev?.getPressure(prev.actionIndex) ?: 0F
    }

    protected open fun resetState() {

        prevEvent?.recycle()
        prevEvent = null

        currEvent?.recycle()
        currEvent = null

        isInProgress = false
    }

    open fun onConfigurationChanged(newConfig: Configuration) {
    }

    companion object {
        /**
         * This value is the threshold ratio between the previous combined pressure
         * and the current combined pressure. When pressure decreases rapidly
         * between events the position values can often be imprecise, as it usually
         * indicates that the user is in the process of lifting a pointer off of the
         * device. This value was tuned experimentally.
         */
        const val PRESSURE_THRESHOLD = 0.67f
    }
}
