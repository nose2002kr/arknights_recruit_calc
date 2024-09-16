package proj.ksks.arknights.arknights_calc

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
import proj.ksks.arknights.arknights_calc.OperatorChartLayout.Listener
import kotlin.math.min


class FloatingAmiya : Service() {
    private val gestureHandler = object:FloatingWidgetGestureHandler(this) {
        override fun onClickNotDragged(v: View?) {
            removeAllViews()
            startForegroundService(
                Intent(this@FloatingAmiya, ScreenCaptureService::class.java).apply {
                    action = "CAPTURE"
                }
            )
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

    private fun registerRotationReceiver() {
        registerReceiver(
            object: BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent) {
                    if (intent.action == Intent.ACTION_CONFIGURATION_CHANGED) {
                        showIcon()
                    }
                }
            },
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

    private fun showIcon() {
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
                tags) as List<Map<String, Any>>
    }

    private fun showPanel(matchedTags : ArrayList<String>) {
        CoroutineScope(Dispatchers.Main).launch {
            with(
                ChannelManager.callFunction(
                    ChannelManager.getChannelInstance(ChannelManager.ARKNIGHTS),
                    "getAmiyaLayout",
                    null
                ) as List<Int?>
            ) {
                val layout = OperatorChartLayout(this@FloatingAmiya, matchedTags, object : Listener {
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

    @Suppress("DEPRECATION")
    private inline fun <reified P : Parcelable> Intent.getParcelable(key: String): P? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(key, P::class.java)
        } else {
            getParcelableExtra(key)
        }
    }
}
