package proj.ksks.arknights.arknights_calc

import android.animation.ArgbEvaluator
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Build
import android.os.IBinder
import android.os.Parcelable
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup.LayoutParams
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import proj.ksks.arknights.arknights_calc.OperatorChartLayout.Listener
import kotlin.math.hypot


class FloatingAmiya : Service() {
    private val gestureHandler = GestureHandler()

    /* Constant val */
    private val TAG = "FloatingAmiya"
    private val ICON_SIZE = 200
    private val ICON_SHADOW_MARGIN = 20
    private val ICON_ELEVATION = 10f
    private val PANEL_WIDTH = 1200
    private val PANEL_HEIGHT = 700


    /* Member */
    private lateinit var mWindowManager : WindowManager
    private lateinit var mBitmap : Bitmap
    private var mOuterLayoutParams: WindowManager.LayoutParams? = null
    private val addedViews = mutableListOf<View>()

    private var screenWidth: Int = 0
    private var screenHeight: Int = 0

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate() {
        super.onCreate()
        mWindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        gestureHandler.buildTextView()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            screenWidth = mWindowManager.currentWindowMetrics.bounds.width()
            screenHeight = mWindowManager.currentWindowMetrics.bounds.height()
        } else {
            @Suppress("DEPRECATION")
            with (mWindowManager.defaultDisplay) {
                getMetrics(DisplayMetrics())
                with (Point()) {
                    getRealSize(this)
                    screenWidth = this.x
                    screenHeight = this.y
                }
            }
        }
    }

    private fun addView(view: View, params: WindowManager.LayoutParams) {
        mWindowManager.addView(view, params)
        addedViews.add(view)
    }

    private fun removeAllViews() {
        for (view in addedViews) {
            mWindowManager.removeView(view)
        }
        addedViews.clear()
    }

    @TargetApi(Build.VERSION_CODES.TIRAMISU)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Amiya, " + intent?.action)
        if (intent?.action.equals("STOP")) {
            Log.d(TAG, "Hide amiya.")
            removeAllViews()
        } else if (intent?.action.equals("START")) {
            Log.d(TAG, "Show amiya.")
            mBitmap = intent?.getParcelable("icon")!!
            showIcon()
        } else if (intent?.action.equals("SHOW_PANEL")) {
            Log.d(TAG, "Show panel.")
            val matchedTags: ArrayList<String> =
                intent?.getStringArrayListExtra("tags") ?: arrayListOf()
            showPanel(matchedTags)
        }
        return START_STICKY
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun showIcon() {
        val outerLayoutParams = WindowManager.LayoutParams(
            ICON_SIZE,
            ICON_SIZE,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        outerLayoutParams.gravity = Gravity.TOP or Gravity.LEFT;
        val frameLayout = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams (
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
        }

        if (mOuterLayoutParams != null) {
            outerLayoutParams.x = mOuterLayoutParams!!.x
            outerLayoutParams.y = mOuterLayoutParams!!.y
        } else {
            CoroutineScope(Dispatchers.Main).launch {
                with(
                    ChannelManager.callFunction(
                        ChannelManager.getChannelInstance(ChannelManager.ARKNIGHTS),
                        "getAmiyaPosition",
                        null
                    ) as List<Int?>
                ) {
                    this[0]?.let { outerLayoutParams.x = it }
                    this[1]?.let { outerLayoutParams.y = it }
                    mOuterLayoutParams = outerLayoutParams;
                    mWindowManager.updateViewLayout(frameLayout, outerLayoutParams)
                }
            }
        }

        val backgroundView = View(this)
        backgroundView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ).apply {
            setMargins(
                ICON_SHADOW_MARGIN,
                ICON_SHADOW_MARGIN,
                ICON_SHADOW_MARGIN,
                ICON_SHADOW_MARGIN)
        }

        backgroundView.background = ShapeDrawable(OvalShape()).apply {
            paint.color = Color.WHITE
        }
        backgroundView.elevation = ICON_ELEVATION

        val imageView = ImageView(this)
        imageView.setImageBitmap(mBitmap)
        imageView.elevation = ICON_ELEVATION+1
        imageView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ).apply {
            setMargins(
                ICON_SHADOW_MARGIN,
                ICON_SHADOW_MARGIN,
                ICON_SHADOW_MARGIN,
                ICON_SHADOW_MARGIN)
        }

        
        frameLayout.setOnTouchListener(gestureHandler)
        frameLayout.setOnClickListener(gestureHandler)
        frameLayout.setOnLongClickListener(gestureHandler)

        frameLayout.addView(backgroundView)
        frameLayout.addView(imageView)
        Log.i(TAG, "showIcon")
        removeAllViews()
        addView(frameLayout, outerLayoutParams)
        mOuterLayoutParams = outerLayoutParams
    }

    private suspend fun requestLookingUpOperator(tags: List<String>): List<Map<String, Any>> {
        return ChannelManager.callFunction(
            ChannelManager.getChannelInstance(ChannelManager.ARKNIGHTS),
                "lookupOperator",
                tags) as List<Map<String, Any>>
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun showPanel(matchedTags : ArrayList<String>) {
        val layout = OperatorChartLayout(this, matchedTags, object : Listener {
            override fun requestDismiss(self: OperatorChartLayout) {
                showIcon()
            }

            override fun requestUpdate(self: OperatorChartLayout, tags: List<String>) {
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val dataDeferred = async { requestLookingUpOperator(tags) }
                        val operatorMap = dataDeferred.await()
                        println("Received operator data: $operatorMap")

                        self.updateOperatorView(operatorMap)

                    } catch (e: NotImplementedError) {
                        println("The method is not implemented")
                    } catch (e: Exception) {
                        println("An error occurred: ${e.message}")
                    }
                }
            }
        })

        val outerLayoutParams = WindowManager.LayoutParams(
            PANEL_WIDTH,
            PANEL_HEIGHT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        outerLayoutParams.gravity = Gravity.TOP or Gravity.LEFT;
        mOuterLayoutParams?.let { param ->
            outerLayoutParams.x = param.x
            outerLayoutParams.y = param.y
        }
        layout.setOnTouchListener(gestureHandler)

        Log.i(TAG, "showPanel")
        removeAllViews()
        addView(layout, outerLayoutParams)
        mOuterLayoutParams = outerLayoutParams
    }

    private inner class GestureHandler : View.OnTouchListener, View.OnClickListener, View.OnLongClickListener {
        private var initialX = 0
        private var initialY = 0
        private var initialTouchX = 0f
        private var initialTouchY = 0f
        private var dragged = false
        private lateinit var terminateIndicator: TerminateIndicator

        @SuppressLint("AppCompatCustomView")
        @RequiresApi(Build.VERSION_CODES.R)
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
                        setColor(ArgbEvaluator().evaluate(
                            fraction,
                            startValue.color!!.defaultColor,
                            endValue.color!!.defaultColor) as Int
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
                    ContextCompat.getDrawable(this@FloatingAmiya, android.R.drawable.ic_delete)
                        ?.apply { setBounds(0, 0, 60, 60)
                            colorFilter = PorterDuffColorFilter(
                                Color.WHITE, PorterDuff.Mode.SRC_ATOP
                            )
                        }
                    ,null, null, null) // Set icon to the left

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
                    addUpdateListener {
                        animator ->
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
                    addUpdateListener {
                        animator ->
                            this@TerminateIndicator.background = animator.animatedValue as Drawable
                    }
                }.start()
            }

            fun show() {
                hide()
                // do not insert `addedViews`
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
                        with (mWindowManager.defaultDisplay) {
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

        @RequiresApi(Build.VERSION_CODES.R)
        fun buildTextView() {
            terminateIndicator = TerminateIndicator(this@FloatingAmiya)
        }

        private val mLongPressed = Runnable {
            Log.d(TAG, "onLongPressed")
            terminateIndicator.show()
        }

        @RequiresApi(Build.VERSION_CODES.R)
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    view.handler.postDelayed(mLongPressed, ViewConfiguration.getLongPressTimeout().toLong())

                    initialX = mOuterLayoutParams!!.x
                    initialY = mOuterLayoutParams!!.y

                    initialX = initialX.coerceIn(
                        0,
                        screenWidth - (mOuterLayoutParams!!.width)
                    )
                    initialY = initialY.coerceIn(
                        0,
                        screenHeight - (mOuterLayoutParams!!.height)
                    )

                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    dragged = false

                    return false
                }
                MotionEvent.ACTION_MOVE -> {
                    mOuterLayoutParams!!.x = initialX + (event.rawX - initialTouchX).toInt()
                    mOuterLayoutParams!!.y = initialY + (event.rawY - initialTouchY).toInt()
                    mWindowManager.updateViewLayout(view, mOuterLayoutParams)


                    val pos = IntArray(2)
                    terminateIndicator.getLocationOnScreen(pos)
                    if (Rect(pos[0], pos[1],
                        pos[0] + terminateIndicator.width,
                        pos[1] + terminateIndicator.height
                    ).contains(event.rawX.toInt(), event.rawY.toInt())) {
                        terminateIndicator.active()
                    } else {
                        terminateIndicator.inactive()
                    }
                    return false
                }
                MotionEvent.ACTION_UP -> {
                    view.handler.removeCallbacks(mLongPressed)

                    if (terminateIndicator.activated) {
                        val intent = Intent(this@FloatingAmiya, ScreenCaptureService::class.java)
                        intent.setAction("STOP_SCREEN_CAPTURE")
                        startForegroundService(intent)
                    }

                    val distance = hypot((initialTouchX - event.rawX).toDouble(), (initialTouchY - event.rawY).toDouble())
                    if (distance > ICON_SIZE / 4) {
                        dragged = true
                    }
                    Log.d(TAG, "distance: $distance, dragged: $dragged")
                    terminateIndicator.hide()

                    CoroutineScope(Dispatchers.Main).launch {
                        ChannelManager.callFunction(
                            ChannelManager.getChannelInstance(ChannelManager.ARKNIGHTS),
                            "amiyaPositionIsChanged",
                            listOf(mOuterLayoutParams!!.x, mOuterLayoutParams!!.y)
                        )
                    }
                }
            }
            return false
        }

        @TargetApi(Build.VERSION_CODES.O)
        override fun onClick(v: View?) {
            if (dragged) {
                return
            }

            removeAllViews()
            startForegroundService(
                Intent(this@FloatingAmiya, ScreenCaptureService::class.java).apply {
                    action = "CAPTURE"
                }
            )
        }

        override fun onLongClick(v: View?): Boolean {
            // Consume the event.
            return true
        }
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @Suppress("DEPRECATION")
    private inline fun <reified P : Parcelable> Intent.getParcelable(key: String): P? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(key, P::class.java)
        } else {
            getParcelableExtra(key)
        }
    }
}
