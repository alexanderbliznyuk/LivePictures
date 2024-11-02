package com.blizniuk.livepictures.ui.colorpicker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.FrameLayout
import com.blizniuk.livepictures.R
import kotlin.math.abs


class ColorPickerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    private val touchSlop: Int = ViewConfiguration.get(context).scaledTouchSlop

    private val colorBarSize = resources.getDimensionPixelSize(R.dimen.color_bar_height)
    private val thumbRadius = resources.getDimension(R.dimen.color_bar_thumb_radius)

    private val thumbOffset = thumbRadius / 3
    private val thumbWidth = 2 * thumbRadius + thumbOffset

    private val thumbTrianglePath = Path()
    private var thumbColor = context.getColor(R.color.accent_color)

    private val colorBar: ColorBarView

    private var downAxis: Float = 0F
    private var isInDragMode = false

    var onColorChangeListener: ((Int) -> Unit)? = null

    init {
        setWillNotDraw(false)
        colorBar = ColorBarView(context)
        val lp = LayoutParams(LayoutParams.MATCH_PARENT, colorBarSize)
        lp.marginStart = thumbRadius.toInt()
        lp.marginEnd = thumbRadius.toInt()
        lp.gravity = Gravity.BOTTOM

        addView(colorBar, lp)
    }

    private val pickerSize: Int
        get() = ((measuredWidth) - thumbRadius).toInt()

    private var positionPercentage: Float = 0.25f
        set(value) {
            field = value.coerceIn(0F, 1F)
            val newColor = (positionPercentage * pickerSize + thumbRadius)
                .coerceIn(thumbRadius, pickerSize.toFloat())

            position = newColor
        }

    private var position: Float = thumbRadius
        set(value) {
            if (field != value) {
                field = value
                onColorChangeListener?.invoke(getColor())
                invalidate()
            }
        }

    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        strokeWidth = resources.getDimension(R.dimen.color_bar_thumb_stroke_size)
    }

    fun getColor(): Int = colorBar.getColor(positionPercentage)

    fun setColor(color: Int) {
        val part = colorBar.getPart(color)
        positionPercentage = part
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = position
        val end = measuredHeight - colorBarSize
        val cy = end - thumbWidth + thumbRadius

        paint.color = thumbColor
        canvas.drawCircle(cx, cy, thumbRadius, paint)

        thumbTrianglePath.reset()
        thumbTrianglePath.moveTo(cx - thumbRadius, cy)
        thumbTrianglePath.lineTo(cx + thumbRadius, cy)
        thumbTrianglePath.lineTo(cx, cy + thumbRadius + thumbOffset)
        thumbTrianglePath.close()

        canvas.drawPath(thumbTrianglePath, paint)
        val color = getColor()

        paint.color = color
        canvas.drawCircle(cx, cy, thumbRadius / 2, paint)
    }


    override fun onTouchEvent(ev: MotionEvent): Boolean {
        handleTouch(ev)
        return true
    }


    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        handleTouch(ev)
        return isInDragMode
    }

    private fun handleTouch(ev: MotionEvent) {
        val action = ev.action
        val touchAxis = ev.x

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                downAxis = touchAxis

                positionPercentage = (downAxis - thumbRadius) / pickerSize
            }

            MotionEvent.ACTION_MOVE -> {
                if (!isInDragMode) {
                    if (abs(downAxis - touchAxis) > touchSlop) {
                        isInDragMode = true
                        requestDisallowInterceptTouchEvent(true)
                    }
                }

                if (isInDragMode) {
                    positionPercentage = (touchAxis - thumbRadius) / pickerSize
                }
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> isInDragMode = false
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        positionPercentage = positionPercentage
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredSize = (colorBarSize + thumbWidth + 30).toInt()
        val measuredSpec = MeasureSpec.makeMeasureSpec(desiredSize, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, measuredSpec)
    }
}