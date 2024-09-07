package proj.ksks.arknights.arknights_calc

import android.annotation.TargetApi
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Build
import android.os.IBinder
import android.os.Parcelable
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import proj.ksks.arknights.arknights_calc.OperatorChartLayout.Listener


class FloatingAmiya : Service() {
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


    override fun onCreate() {
        super.onCreate()
        mWindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
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
        Log.d(TAG, "Amiya, " + intent.action)
        if (intent.action.equals("STOP")) {
            Log.d(TAG, "Hide amiya.")
            removeAllViews()
        } else if (intent.action.equals("START")) {
            Log.d(TAG, "Show amiya.")
            mBitmap = intent.getParcelable("icon")!!
            showIcon()
        } else if (intent.action.equals("SHOW_PANEL")) {
            Log.d(TAG, "Show panel.")
            val matchedTags : ArrayList<String> = intent.getStringArrayListExtra("tags")!!
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

        outerLayoutParams.gravity = Gravity.CENTER
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
                    ) as List<Int>
                ) {
                    outerLayoutParams.x = this[0]
                    outerLayoutParams.y = this[1]
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

        frameLayout.setOnTouchListener(DragTouchListener())
        frameLayout.setOnClickListener(ClickListener())
        frameLayout.setOnLongClickListener(ClickListener())

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

        outerLayoutParams.gravity = Gravity.CENTER
        mOuterLayoutParams?.let { param ->
            outerLayoutParams.x = param.x
            outerLayoutParams.y = param.y
        }
        layout.setOnTouchListener(DragTouchListener())

        Log.i(TAG, "showPanel")
        removeAllViews()
        addView(layout, outerLayoutParams)
        mOuterLayoutParams = outerLayoutParams
    }

    private inner class DragTouchListener : View.OnTouchListener {
        private var initialX = 0
        private var initialY = 0
        private var initialTouchX = 0f
        private var initialTouchY = 0f

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = mOuterLayoutParams!!.x
                    initialY = mOuterLayoutParams!!.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    return false
                }
                MotionEvent.ACTION_MOVE -> {
                    mOuterLayoutParams!!.x = initialX + (event.rawX - initialTouchX).toInt()
                    mOuterLayoutParams!!.y = initialY + (event.rawY - initialTouchY).toInt()
                    mWindowManager.updateViewLayout(view, mOuterLayoutParams)
                    return false
                }
                MotionEvent.ACTION_UP -> {
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
    }

    private inner class ClickListener : View.OnClickListener, View.OnLongClickListener {
        @TargetApi(Build.VERSION_CODES.O)
        override fun onClick(v: View?) {
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
