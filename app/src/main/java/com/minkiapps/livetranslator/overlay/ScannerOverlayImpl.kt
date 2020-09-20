package com.minkiapps.livetranslator.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Size
import android.view.MotionEvent
import android.view.View
import com.minkiapps.livetranslator.utils.px
import kotlin.math.max
import kotlin.math.min

class ScannerOverlayImpl @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr), ScannerOverlay {

    private val transparentPaint: Paint by lazy {
        Paint().apply {
            isAntiAlias = true
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }
    }

    private val strokePaint: Paint by lazy {
        Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#aaffffff")
            strokeWidth = sWidth
            style = Paint.Style.STROKE
        }
    }

    private val linePaint : Paint by lazy {
        Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#aaffffff")
            strokeWidth = context.px(1f)
            style = Paint.Style.STROKE
        }
    }

    private val sWidth = context.px(3f)
    private val resizeTriggerThreshHold = context.px(24)
    private var touchAction : TouchAction = TouchAction.None
    private val scanRectF: RectF = RectF()

    private var lastX: Float = 0f
    private var lastY: Float = 0f

    init {
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val w = measuredWidth
        val h = measuredHeight

        val l = w * (1 -  MIN_WIDTH_PERCENTAGE) / 2
        val t = h * 0.2f
        scanRectF.set(l, t, l + w * MIN_WIDTH_PERCENTAGE, t + h * MIN_HEIGHT_PERCENTAGE)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.parseColor("#aa000000"))

        val rectF = scanRect

        canvas.drawRect(rectF, transparentPaint)
        canvas.drawRect(rectF, strokePaint)

        canvas.drawMarginPaths()
        if(touchAction !is TouchAction.None) {
            canvas.drawInnerDividers()
        }
    }

    private fun Canvas.drawMarginPaths() {
        val rectF = scanRect
        val p = px(6f)
        val s = 3 * p
        val path = Path()

        //left top corner
        path.moveTo(rectF.left + p, rectF.top + p)
        path.rLineTo(- sWidth / 2, 0f)
        path.rLineTo(s + sWidth / 2, 0f)
        path.moveTo(rectF.left + p, rectF.top + p)
        path.rLineTo(0f, s)

        //right top corner
        path.moveTo(rectF.right - p, rectF.top + p)
        path.rLineTo(sWidth / 2, 0f)
        path.rLineTo(- (s + sWidth / 2), 0f)
        path.moveTo(rectF.right - p, rectF.top + p)
        path.rLineTo(0f, s)

        //left bottom corner
        path.moveTo(rectF.left + p, rectF.bottom - p)
        path.rLineTo(- sWidth / 2, 0f)
        path.rLineTo(s + sWidth / 2, 0f)
        path.moveTo(rectF.left + p, rectF.bottom - p)
        path.rLineTo(0f, -s)

        //right bottom corner
        path.moveTo(rectF.right - p, rectF.bottom - p)
        path.rLineTo(sWidth / 2, 0f)
        path.rLineTo(- (s + sWidth / 2), 0f)
        path.moveTo(rectF.right - p, rectF.bottom - p)
        path.rLineTo(0f, -s)

        drawPath(path, strokePaint)
    }

    private fun Canvas.drawInnerDividers() {
        val rect = scanRectF
        val top = rect.top
        val left = rect.left
        val bottom = rect.bottom
        val right = rect.right

        val xL1 = rect.width() / 3f + left
        val xL2 = rect.width() * 2 / 3f + left

        val yL3 = rect.height() / 3f + top
        val yL4 = rect.height() * 2 / 3f + top

        val linePoints = floatArrayOf(xL1, top, xL1, bottom, xL2, top, xL2, bottom, left, yL3, right, yL3, left, yL4, right, yL4)
        drawLines(linePoints, linePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if(!isEnabled)
            return false

        when(event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val rect = scanRectF

                val x = event.x
                val y = event.y
                when {
                    x in (rect.left + resizeTriggerThreshHold) .. (rect.right - resizeTriggerThreshHold) &&
                            y in (rect.top + resizeTriggerThreshHold) .. (rect.bottom - resizeTriggerThreshHold) -> {
                        touchAction = TouchAction.Move
                        invalidate()
                    }
                    x in (rect.left - resizeTriggerThreshHold) .. (rect.left + resizeTriggerThreshHold) &&
                            y in (rect.top - resizeTriggerThreshHold) .. (rect.top + resizeTriggerThreshHold) -> {
                        touchAction = TouchAction.Resizing(TouchAction.Resizing.ResizingFrom.TOP_LEFT)
                        invalidate()
                    }
                    x in (rect.right - resizeTriggerThreshHold) .. (rect.right + resizeTriggerThreshHold) &&
                            y in (rect.top - resizeTriggerThreshHold) .. (rect.top + resizeTriggerThreshHold) -> {
                        touchAction = TouchAction.Resizing(TouchAction.Resizing.ResizingFrom.TOP_RIGHT)
                        invalidate()
                    }
                    x in (rect.left - resizeTriggerThreshHold) .. (rect.left + resizeTriggerThreshHold) &&
                            y in (rect.bottom - resizeTriggerThreshHold) .. (rect.bottom + resizeTriggerThreshHold) -> {
                        touchAction = TouchAction.Resizing(TouchAction.Resizing.ResizingFrom.BOTTOM_LEFT)
                        invalidate()
                    }
                    x in (rect.right - resizeTriggerThreshHold) .. (rect.right + resizeTriggerThreshHold) &&
                            y in (rect.bottom - resizeTriggerThreshHold) .. (rect.bottom + resizeTriggerThreshHold) -> {
                        touchAction = TouchAction.Resizing(TouchAction.Resizing.ResizingFrom.BOTTOM_RIGHT)
                        invalidate()
                    }
                }

                lastX = event.x
                lastY = event.y
            }

            MotionEvent.ACTION_MOVE -> {
                val x = event.x
                val y = event.y

                val deltaX = x - lastX
                val deltaY = y - lastY

                when (val action = touchAction) {
                    is TouchAction.Resizing -> {
                        val rect = scanRectF

                        val minW = MIN_WIDTH_PERCENTAGE * width
                        val minH = MIN_HEIGHT_PERCENTAGE * height

                        when (action.resizingFrom) {
                            TouchAction.Resizing.ResizingFrom.TOP_LEFT -> {
                                val newLeft = min(max(0f, rect.left + deltaX), rect.right - minW)
                                val newTop = min(max(0f, rect.top + deltaY), rect.bottom - minH)
                                scanRectF.left = newLeft
                                scanRectF.top = newTop
                            }
                            TouchAction.Resizing.ResizingFrom.TOP_RIGHT -> {
                                val newRight = max(min(width * 1f, rect.right + deltaX), rect.left + minW)
                                val newTop = min(max(0f, rect.top + deltaY), rect.bottom - minH)
                                scanRectF.right = newRight
                                scanRectF.top = newTop
                            }
                            TouchAction.Resizing.ResizingFrom.BOTTOM_LEFT -> {
                                val newLeft = min(max(0f, rect.left + deltaX), rect.right - minW)
                                val newBottom = max(min(height * MAX_DRAGGING_BOTTOM_PERCENTAGE, rect.bottom + deltaY), rect.top + minH)
                                scanRectF.left = newLeft
                                scanRectF.bottom = newBottom
                            }
                            TouchAction.Resizing.ResizingFrom.BOTTOM_RIGHT -> {
                                val newRight = max(min(width * 1f, rect.right + deltaX), rect.left + minW)
                                val newBottom = max(min(height * MAX_DRAGGING_BOTTOM_PERCENTAGE, rect.bottom + deltaY), rect.top + minH)
                                scanRectF.right = newRight
                                scanRectF.bottom = newBottom
                            }
                        }
                        invalidate()
                    }
                    is TouchAction.Move -> {
                        val rect = scanRectF
                        val newLeft = min(max(0f, rect.left + deltaX), width - rect.width())
                        val newTop = min(
                            max(0f, rect.top + deltaY),
                            height * MAX_DRAGGING_BOTTOM_PERCENTAGE - rect.height()
                        )

                        scanRectF.set(
                            newLeft,
                            newTop,
                            newLeft + scanRectF.width(),
                            newTop + scanRectF.height()
                        )
                        invalidate()
                    }
                    TouchAction.None -> {
                    }
                }

                lastX = x
                lastY = y
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                touchAction = TouchAction.None
                invalidate()
            }
        }

        return touchAction !is TouchAction.None
    }

    override val size: Size
        get() = Size(width, height)

    override val scanRect: RectF
        get() = scanRectF

    private sealed class TouchAction {
        object None : TouchAction()
        object Move : TouchAction()
        class Resizing(val resizingFrom : ResizingFrom) : TouchAction() {
            enum class ResizingFrom {
                TOP_LEFT,
                TOP_RIGHT,
                BOTTOM_LEFT,
                BOTTOM_RIGHT
            }
        }
    }

    companion object {
        private const val MIN_WIDTH_PERCENTAGE = 0.3f
        private const val MIN_HEIGHT_PERCENTAGE = 0.1f

        private const val MAX_DRAGGING_BOTTOM_PERCENTAGE = 0.6f
    }
}