package proj.ksks.arknights.arknights_calc.ui

import android.animation.ArgbEvaluator
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup.LayoutParams
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import proj.ksks.arknights.arknights_calc.bridge.ChannelManager
import proj.ksks.arknights.arknights_calc.bridge.Tr
import proj.ksks.arknights.arknights_calc.service.ScreenCaptureService
import proj.ksks.arknights.arknights_calc.util.takeScreenSize
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

open class FloatingWidgetGestureHandler(private val context: Context):
    View.OnTouchListener,
    View.OnClickListener,
    View.OnLongClickListener {

    /* Constant val */
    private val TAG = "FloatingWidgetGestureHandler"
    private val LEN_DETERMINED_BY_DRAG = 50

    /* Member */
    private lateinit var mWindowManager : WindowManager

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var dragged = false
    private var resizing = false
    private var screenSize = Size(0,0)

    private lateinit var terminateIndicator: TerminateIndicator
    private lateinit var rubberBand: RubberBand

    @SuppressLint("AppCompatCustomView")
    private inner class TerminateIndicator(context: Context) : TextView(context) {
        @JvmField
        var activated = false
        private val normalStateBackgroundColor = GradientDrawable().apply {
            setColor(Color.parseColor("#FF444444"))
            cornerRadius = 34f
        }

        private val activeStateBackgroundColor = GradientDrawable().apply {
            setColor(Color.parseColor("#AAD63A31"))
            cornerRadius = 34f
        }

        inner class GradientDrawableEvaluator : TypeEvaluator<GradientDrawable> {
            override fun evaluate(
                fraction: Float,
                startValue: GradientDrawable,
                endValue: GradientDrawable
            ): GradientDrawable {

                return GradientDrawable().apply {
                    setColor(
                        ArgbEvaluator().evaluate(
                            fraction,
                            startValue.color!!.defaultColor,
                            endValue.color!!.defaultColor
                        ) as Int
                    )
                    cornerRadius = 34f
                }
            }
        }

        init {
            text = Tr.QUIT
            textSize = 22F
            background = normalStateBackgroundColor
            setPadding(26, 16, 46, 26)

            setCompoundDrawables(
                ContextCompat.getDrawable(context, android.R.drawable.ic_delete)
                    ?.apply {
                        setBounds(0, 0, 60, 60)
                        colorFilter = PorterDuffColorFilter(
                            Color.WHITE, PorterDuff.Mode.SRC_ATOP
                        )
                    }, null, null, null
            ) // Set icon to the left

            compoundDrawablePadding = 16 // Padding between the text and the drawable

            setTextColor(ColorStateList.valueOf(Color.WHITE))
        }

        fun active() {
            if (activated) {
                return
            }

            activated = true

            ValueAnimator.ofObject(
                GradientDrawableEvaluator(),
                normalStateBackgroundColor,
                activeStateBackgroundColor
            ).apply {
                duration = 200
                addUpdateListener { animator ->
                    this@TerminateIndicator.background = animator.animatedValue as Drawable
                }
            }.start()
        }

        fun inactive() {
            if (!activated) {
                return
            }
            activated = false

            ValueAnimator.ofObject(
                GradientDrawableEvaluator(),
                activeStateBackgroundColor,
                normalStateBackgroundColor
            ).apply {
                duration = 200
                addUpdateListener { animator ->
                    this@TerminateIndicator.background = animator.animatedValue as Drawable
                }
            }.start()
        }

        fun show() {
            hide()
            mWindowManager.addView(this, WindowManager.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    this.y = mWindowManager.currentWindowMetrics.bounds.height() / 4
                } else {
                    @Suppress("DEPRECATION")
                    with(mWindowManager.defaultDisplay) {
                        this.getMetrics(DisplayMetrics())
                        val size = Point()
                        this.getRealSize(size)
                        this@apply.y = size.y / 3
                    }
                }
            })
        }

        fun hide() {
            this.parent?.let {
                mWindowManager.removeView(this)
            }
        }
    }

    private inner class RubberBand(context: Context): View(context) {

        private var rect: Rect = Rect()
        var floatingWidget: ResizableFloatingView? = null

        @SuppressLint("DrawAllocation")
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val rectWithMargin = RectF(
                (rect.left + (floatingWidget?.marginForEasierGrab() ?: 0)).toFloat(),
                (rect.top + (floatingWidget?.marginForEasierGrab() ?: 0)).toFloat(),
                (rect.right - (floatingWidget?.marginForEasierGrab() ?: 0)).toFloat(),
                (rect.bottom - (floatingWidget?.marginForEasierGrab() ?: 0)).toFloat()
            )
            canvas.drawRoundRect(
                rectWithMargin, 50f, 50f, Paint().apply {
                    color = Color.parseColor("#12818589")
                    style = Paint.Style.FILL
                }
            )

            canvas.drawRoundRect(
                rectWithMargin, 50f, 50f, Paint().apply {
                    color = Color.parseColor("#FF708090")
                    strokeWidth = 9f
                    style = Paint.Style.STROKE
                }
            )
        }

        fun show(rect: Rect = Rect()) {
            hide()
            mWindowManager.addView(this, WindowManager.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.LEFT;
            })

            this.rect = rect
        }

        fun hide() {
            this.parent?.let {
                mWindowManager.removeView(this)
            }
            this.rect = Rect()
        }

        fun stretchLeft(left: Int) {
            rect.left = min(rect.right - (floatingWidget?.minimumWidth() ?: 0), left)
            invalidate()
        }

        fun stretchTop(top: Int) {
            rect.top = min(rect.bottom - (floatingWidget?.minimumHeight() ?: 0), top)
            invalidate()
        }

        fun stretchRight(right: Int) {
            rect.right = max(rect.left + (floatingWidget?.minimumWidth() ?: 0), right)
            invalidate()
        }

        fun stretchBottom(bottom: Int) {
            rect.bottom = max(rect.top + (floatingWidget?.minimumHeight() ?: 0), bottom)
            invalidate()
        }

        fun getRect(): Rect = rect
    }

    private fun buildTerminateIndicator() {
        terminateIndicator = TerminateIndicator(context)
    }

    private fun buildRubberBand() {
        rubberBand = RubberBand(context)
    }

    fun buildViews() {
        mWindowManager = getSystemService(context, WindowManager::class.java)!!
        buildTerminateIndicator()
        buildRubberBand()
    }

    private val mLongPressed = Runnable {
        Log.d(TAG, "onLongPressed")
        terminateIndicator.show()
    }

    inner class EdgeTouched {
        var left: Boolean = false
        var right: Boolean = false
        var top: Boolean = false
        var bottom: Boolean = false
        fun it(): Boolean = left or right or top or bottom
    }

    private var edgeTouched: EdgeTouched? = null
    private fun isEdgeTouched(view: ResizableFloatingView, event: MotionEvent): EdgeTouched {

        val layoutParams = view.layoutParams as WindowManager.LayoutParams
        val pos = IntArray(2)
        view.getLocationOnScreen(pos)

        val x = event.rawX.toInt()
        val y = event.rawY.toInt()

        val viewRect = Rect(
            pos[0], pos[1],
            pos[0] + layoutParams.width,
            pos[1] + layoutParams.height
        )

        fun isTouchedAt(at: Int, what: Int): Boolean {
            val gap = view.marginForEasierGrab() * 3
            return (at - gap..at + gap).contains(what)
        }

        val touched = EdgeTouched()
        touched.apply {
            left = isTouchedAt(viewRect.left, x)
            top = isTouchedAt(viewRect.top, y)
            right = isTouchedAt(viewRect.right, x)
            bottom = isTouchedAt(viewRect.bottom, y)
        }

        if (touched.top)
            if (((viewRect.width() / 2 - view.holderWidth() / 2) ..
                (viewRect.width() / 2 + view.holderWidth() / 2)).contains(event.x.toInt())) {
                touched.top = false
            }

        Log.d(TAG, "touchedEdge: ${touched.it()}")
        return touched
    }

    private fun dumpTouchEvent(view: View, event: MotionEvent) {
        val pos = IntArray(2)
        view.getLocationOnScreen(pos)
        val layoutParams = view.layoutParams as WindowManager.LayoutParams
        Log.d(TAG, "event: [${event.rawX}, ${event.rawY}], " +

                "frame: [" +
                "${layoutParams.x}, " +
                "${layoutParams.y} - " +
                "${layoutParams.x + layoutParams.width}, " +
                "${layoutParams.y + layoutParams.height}], " +

                "frame-on-the-screen: [" +
                "${pos[0]}, " +
                "${pos[1]} - " +
                "${pos[0] + (layoutParams.width)}, " +
                "${pos[1] + (layoutParams.height)}]")
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        //dumpTouchEvent(view, event)
        val layoutParams = view.layoutParams as WindowManager.LayoutParams
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                screenSize = takeScreenSize(mWindowManager)
                edgeTouched = if (view is ResizableFloatingView) {
                    isEdgeTouched(view, event)
                } else {
                    null
                }
                resizing = edgeTouched?.it() == true

                initialX = layoutParams.x
                initialY = layoutParams.y

                initialX = initialX.coerceIn(
                    0,
                    screenSize.width - (layoutParams.width)
                )
                initialY = initialY.coerceIn(
                    0,
                    screenSize.height - (layoutParams.height)
                )

                initialTouchX = event.rawX
                initialTouchY = event.rawY

                dragged = false

                if (resizing) {
                    rubberBand.floatingWidget = view as ResizableFloatingView
                    rubberBand.show(
                        Rect(
                            initialX, initialY,
                            initialX + layoutParams.width,
                            initialY + layoutParams.height,
                        )
                    )
                } else {
                    view.handler.postDelayed(
                        mLongPressed,
                        ViewConfiguration.getLongPressTimeout().toLong()
                    )
                }
                return false
            }

            MotionEvent.ACTION_MOVE -> {
                if (resizing) {
                    /*Log.d(TAG, "initialTouch[$initialTouchX, $initialTouchY]," +
                            " event.raw[${event.rawX}, ${event.rawY}]")*/

                    if (edgeTouched?.left == true) {
                        rubberBand.stretchLeft(
                            initialX
                                    + (event.rawX - initialTouchX).toInt()
                        )
                    }
                    if (edgeTouched?.top == true) {
                        rubberBand.stretchTop(
                            initialY
                                    + (event.rawY - initialTouchY).toInt()
                        )
                    }
                    if (edgeTouched?.right == true) {
                        rubberBand.stretchRight(
                            initialX + layoutParams.width
                                    + (event.rawX - initialTouchX).toInt()
                        )
                    }
                    if (edgeTouched?.bottom == true) {
                        rubberBand.stretchBottom(
                            initialY + layoutParams.height
                                    + (event.rawY - initialTouchY).toInt()
                        )
                    }
                } else {
                    layoutParams.x = initialX + (event.rawX - initialTouchX).toInt()
                    layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        layoutParams.setCanPlayMoveAnimation(true)
                    }
                    mWindowManager.updateViewLayout(view, layoutParams)

                    val pos = IntArray(2)
                    terminateIndicator.getLocationOnScreen(pos)
                    if (Rect(
                            pos[0], pos[1],
                            pos[0] + terminateIndicator.width,
                            pos[1] + terminateIndicator.height
                        ).contains(event.rawX.toInt(), event.rawY.toInt())
                    ) {
                        terminateIndicator.active()
                    } else {
                        terminateIndicator.inactive()
                    }
                }
                return false
            }

            MotionEvent.ACTION_UP -> {
                if (resizing) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        layoutParams.setCanPlayMoveAnimation(false)
                    }
                    val rect = rubberBand.getRect()
                    layoutParams.apply {
                        x = rect.left
                        y = rect.top
                        width = min(rect.width(), screenSize.width)
                        height = min(rect.height(), screenSize.height)
                    }
                    mWindowManager.updateViewLayout(view, layoutParams)

                    rubberBand.hide()
                } else {
                    view.handler.removeCallbacks(mLongPressed)

                    if (terminateIndicator.activated) {
                        val intent = Intent(context, ScreenCaptureService::class.java)
                        intent.setAction("STOP_SCREEN_CAPTURE")
                        ContextCompat.startForegroundService(context, intent)
                    }

                    terminateIndicator.hide()
                }
                val distance = hypot(
                    (initialTouchX - event.rawX).toDouble(),
                    (initialTouchY - event.rawY).toDouble()
                )
                if (distance > LEN_DETERMINED_BY_DRAG) {
                    dragged = true
                }
                Log.d(TAG, "distance: $distance, dragged: $dragged")

                CoroutineScope(Dispatchers.Main).launch {
                    ChannelManager.callFunction(
                        ChannelManager.getChannelInstance(ChannelManager.ARKNIGHTS),
                        "amiyaLayoutIsChanged",
                        listOf(layoutParams.x, layoutParams.y)
                                + if (resizing) listOf(
                            layoutParams.width,
                            layoutParams.height
                        ) else listOf()
                    )
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                if (resizing) rubberBand.hide()
                else terminateIndicator.hide()
            }

            else -> Log.d(TAG, "Collect touch else ${event.action}")
        }
        return false
    }

    override fun onClick(v: View?) {
        if (dragged) {
            return
        }

        onClickNotDragged(v)
    }

    open fun onClickNotDragged(v: View?) {
        // open the method
    }

    override fun onLongClick(v: View?): Boolean {
        // Consume the event.
        return true
    }
}