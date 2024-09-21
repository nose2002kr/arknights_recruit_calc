package proj.ksks.arknights.arknights_calc.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.setMargins
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import proj.ksks.arknights.arknights_calc.bridge.ChannelManager
import proj.ksks.arknights.arknights_calc.ui.FloatingWidgetGestureHandler
import proj.ksks.arknights.arknights_calc.ui.OperatorChartLayout
import proj.ksks.arknights.arknights_calc.ui.OperatorChartLayout.Listener
import proj.ksks.arknights.arknights_calc.util.fromIntent
import proj.ksks.arknights.arknights_calc.util.startService
import proj.ksks.arknights.arknights_calc.util.takeScreenSize
import kotlin.math.min


class FloatingAmiya : Service() {
    private val gestureHandler = object: FloatingWidgetGestureHandler(this) {
        override fun onClickNotDragged(v: View?) {
            removeAllViews()
            ScreenCaptureService.capture(this@FloatingAmiya)
        }
    }

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
    private val addedViews = mutableListOf<View>()

    private var screenWidth = 0
    private var screenHeight = 0

    private val rotationReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.action == Intent.ACTION_CONFIGURATION_CHANGED) {
                showIcon()
            }
        }
    }
    
    companion object {
        private val ACTION_START = "START"
        private data class StartParam(val icon:Bitmap?)

        private val ACTION_STOP = "STOP"

        private val ACTION_SHOW_PANEL = "SHOW_PANEL"
        private data class ShowPanelParam(val tags: ArrayList<String>?)

        fun start(
            context: Context,
            icon: Bitmap?
        ) {
            startService(
                context,
                FloatingAmiya::class.java,
                ACTION_START,
                StartParam(icon))
        }

        fun stop(
            context: Context
        ) {
            startService(
                context,
                FloatingAmiya::class.java,
                ACTION_STOP)
        }
        fun showPanel(
            context: Context,
            tags: ArrayList<String>?
        ) {
            startService(
                context,
                FloatingAmiya::class.java,
                ACTION_SHOW_PANEL,
                ShowPanelParam(tags))
        }
    }

    override fun onCreate() {
        super.onCreate()
        mWindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        gestureHandler.buildViews()

        registerRotationReceiver()
        takeScreenSize(mWindowManager).let {
            screenWidth = it.width
            screenHeight = it.height
        }
    }

    override fun onDestroy() {
        unregisterReceiver(rotationReceiver)
        removeAllViews()
    }

    private fun registerRotationReceiver() {
        registerReceiver(
            rotationReceiver,
            IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED))
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Amiya, " + intent?.action)
        intent?: return START_STICKY

        when (intent.action) {
            ACTION_START -> showIcon(StartParam::class.fromIntent(intent).icon!!)
            ACTION_STOP -> stopSelf()
            ACTION_SHOW_PANEL -> showPanel(ShowPanelParam::class.fromIntent(intent).tags)
        }
        return START_STICKY
    }

    private fun showIcon(bitmap: Bitmap? = null) {
        CoroutineScope(Dispatchers.Main).launch {
            with(
                ChannelManager.callFunction(
                    ChannelManager.getChannelInstance(ChannelManager.ARKNIGHTS),
                    "getAmiyaLayout",
                    null
                ) as List<Int?>
            ) {
                val outerLayoutParams = WindowManager.LayoutParams(
                    ICON_SIZE,
                    ICON_SIZE,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    x = (screenWidth / 2) - (ICON_SIZE / 2)
                    y = (screenHeight / 2) - (ICON_SIZE / 2)
                }

                outerLayoutParams.gravity = Gravity.TOP or Gravity.LEFT;
                val frameLayout = FrameLayout(this@FloatingAmiya).apply {
                    layoutParams = FrameLayout.LayoutParams (
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT
                    )
                }

                this[0]?.let { outerLayoutParams.x = it }
                this[1]?.let { outerLayoutParams.y = it }

                val backgroundView = View(this@FloatingAmiya)
                backgroundView.layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                ).apply {
                    setMargins(ICON_SHADOW_MARGIN)
                }

                backgroundView.background = ShapeDrawable(OvalShape()).apply {
                    paint.color = Color.WHITE
                }
                backgroundView.elevation = ICON_ELEVATION

                val imageView = ImageView(this@FloatingAmiya)
                bitmap?.let { mBitmap = it }
                imageView.setImageBitmap(mBitmap)
                imageView.elevation = ICON_ELEVATION+1
                imageView.layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                ).apply {
                    setMargins(ICON_SHADOW_MARGIN)
                }

                frameLayout.setOnTouchListener(gestureHandler)
                frameLayout.setOnClickListener(gestureHandler)
                frameLayout.setOnLongClickListener(gestureHandler)

                frameLayout.addView(backgroundView)
                frameLayout.addView(imageView)
                Log.i(TAG, "showIcon")
                removeAllViews()
                addView(frameLayout, outerLayoutParams)
            }
        }
    }

    private suspend fun requestLookingUpOperator(tags: List<String>): List<Map<String, Any>> {
        return ChannelManager.callFunction(
            ChannelManager.getChannelInstance(ChannelManager.ARKNIGHTS),
            "lookupOperator",
            tags
        ) as List<Map<String, Any>>
    }

    private fun showPanel(matchedTags : ArrayList<String>?) {
        CoroutineScope(Dispatchers.Main).launch {
            with(
                ChannelManager.callFunction(
                    ChannelManager.getChannelInstance(ChannelManager.ARKNIGHTS),
                    "getAmiyaLayout",
                    null
                ) as List<Int?>
            ) {
                val layout = OperatorChartLayout(this@FloatingAmiya, matchedTags ?: arrayListOf(),
                    object : Listener {
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
                    }
                )

                val outerLayoutParams = WindowManager.LayoutParams(
                    min(PANEL_WIDTH, screenWidth),
                    min(PANEL_HEIGHT, screenHeight),
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                )

                outerLayoutParams.gravity = Gravity.TOP or Gravity.LEFT;

                this[0]?.let { outerLayoutParams.x = it }
                this[1]?.let { outerLayoutParams.y = it }
                this[2]?.let { outerLayoutParams.width = it.coerceIn(layout.minimumWidth(), screenWidth) }
                this[3]?.let { outerLayoutParams.height = it.coerceIn(layout.minimumHeight(), screenHeight) }

                layout.setOnTouchListener(gestureHandler)

                Log.i(TAG, "showPanel")
                removeAllViews()
                addView(layout, outerLayoutParams)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
