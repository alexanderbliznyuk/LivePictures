package com.blizniuk.livepictures.util.gesture

import android.content.Context
import android.content.res.Configuration
import android.view.MotionEvent
import android.view.ViewConfiguration
import kotlin.math.sqrt

internal abstract class TwoFingerGestureDetector(context: Context) : BaseGestureDetector(context) {

    private var edgeSlop: Float = 0F
    private var rightSlopEdge: Float = 0F
    private var bottomSlopEdge: Float = 0F

    protected var prevFingerDiffX: Float = 0F
    protected var prevFingerDiffY: Float = 0F
    protected var currFingerDiffX: Float = 0F
    protected var currFingerDiffY: Float = 0f

    private var currLen: Float = 0F
    private var prevLen: Float = 0F

    /**
     * Return the current distance between the two pointers forming the
     * gesture in progress.
     *
     * @return Distance between pointers in pixels.
     */
    val currentSpan: Float
        get() {
            if (currLen == -1f) {
                val cvx = currFingerDiffX
                val cvy = currFingerDiffY
                currLen = sqrt((cvx * cvx + cvy * cvy).toDouble()).toFloat()
            }
            return currLen
        }

    /**
     * Return the previous distance between the two pointers forming the
     * gesture in progress.
     *
     * @return Previous distance between pointers in pixels.
     */
    val previousSpan: Float
        get() {
            if (prevLen == -1f) {
                val pvx = prevFingerDiffX
                val pvy = prevFingerDiffY
                prevLen = sqrt((pvx * pvx + pvy * pvy).toDouble()).toFloat()
            }
            return prevLen
        }

    init {
        reCalculateSlopEdges(context)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        reCalculateSlopEdges(context)
    }

    private fun reCalculateSlopEdges(context: Context) {
        val config = ViewConfiguration.get(context)
        edgeSlop = config.scaledEdgeSlop.toFloat()

        val metrics = context.resources.displayMetrics
        rightSlopEdge = metrics.widthPixels - edgeSlop
        bottomSlopEdge = metrics.heightPixels - edgeSlop
    }

    abstract override fun handleStartProgressEvent(actionCode: Int, event: MotionEvent)

    abstract override fun handleInProgressEvent(actionCode: Int, event: MotionEvent)

    override fun updateStateByEvent(curr: MotionEvent) {
        super.updateStateByEvent(curr)

        val prev = prevEvent

        currLen = -1f
        prevLen = -1f

        // Previous
        val px0 = prev!!.getX(0)
        val py0 = prev.getY(0)
        val px1 = prev.getX(1)
        val py1 = prev.getY(1)
        val pvx = px1 - px0
        val pvy = py1 - py0

        prevFingerDiffX = pvx
        prevFingerDiffY = pvy

        // Current
        val cx0 = curr.getX(0)
        val cy0 = curr.getY(0)
        val cx1 = curr.getX(1)
        val cy1 = curr.getY(1)
        val cvx = cx1 - cx0
        val cvy = cy1 - cy0

        currFingerDiffX = cvx
        currFingerDiffY = cvy
    }

    /**
     * Check if we have a sloppy gesture. Sloppy gestures can happen if the edge
     * of the user's hand is touching the screen, for example.
     *
     * @param event
     * @return
     */
    protected fun isSloppyGesture(event: MotionEvent): Boolean {
        val leftSlop = edgeSlop
        val topSlop = edgeSlop
        val rightSlop = rightSlopEdge
        val bottomSlop = bottomSlopEdge

        val x0 = event.rawX
        val y0 = event.rawY
        val x1 = getRawX(event, 1)
        val y1 = getRawY(event, 1)

        val p0sloppy = x0 < leftSlop || y0 < topSlop || x0 > rightSlop || y0 > bottomSlop
        val p1sloppy = x1 < leftSlop || y1 < topSlop || x1 > rightSlop || y1 > bottomSlop

        return p0sloppy || p1sloppy
    }

    companion object {

        /**
         * MotionEvent has no getRawX(int) method; simulate it pending future API approval.
         *
         * @param event
         * @param pointerIndex
         * @return
         */
        protected fun getRawX(event: MotionEvent, pointerIndex: Int): Float {
            val offset = event.x - event.rawX
            return if (pointerIndex < event.pointerCount) {
                event.getX(pointerIndex) + offset
            } else {
                0f
            }
        }

        /**
         * MotionEvent has no getRawY(int) method; simulate it pending future API approval.
         *
         * @param event
         * @param pointerIndex
         * @return
         */
        protected fun getRawY(event: MotionEvent, pointerIndex: Int): Float {
            val offset = event.y - event.rawY
            return if (pointerIndex < event.pointerCount) {
                event.getY(pointerIndex) + offset
            } else {
                0f
            }
        }
    }
}
