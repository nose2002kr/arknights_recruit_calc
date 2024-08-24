package proj.ksks.arknights.arknights_calc

import android.annotation.TargetApi
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.os.Parcelable
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import proj.ksks.arknights.arknights_calc.OperatorChartLayout.Listener
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


class FloatingAmiya : Service() {
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

    inner class Callback(
        private val continuation: kotlinx.coroutines.CancellableContinuation<List<Map<String, Any>>>
    ) : MethodChannel.Result {

        override fun success(var1: Any?) {
            val operator = var1 as? List<Map<String, Any>>
            if (operator != null) {
                continuation.resume(operator)
            } else {
                continuation.resumeWithException(IllegalArgumentException("Unexpected result type"))
            }
        }

        override fun error(var1: String, var2: String?, var3: Any?) {
            continuation.resumeWithException(Exception("Error: $var1, $var2, $var3"))
        }

        override fun notImplemented() {
            continuation.resumeWithException(NotImplementedError("Method not implemented"))
        }
    }


    @TargetApi(Build.VERSION_CODES.TIRAMISU)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("FloatingAmiya", "Amiya, " + intent.action)
        if (intent.action.equals("STOP")) {
            Log.d("FloatingAmiya", "Hide amiya.")
            removeAllViews()
        } else if (intent.action.equals("START")) {
            Log.d("FloatingAmiya", "Show amiya.")
            mBitmap = intent.getParcelable("icon")!!
            showIcon()
        } else if (intent.action.equals("SHOW_PANEL")) {
            Log.d("FloatingAmiya", "Show panel.")
            val matchedTags : ArrayList<String> = intent.getStringArrayListExtra("tags")!!
            showPanel(matchedTags)
        }
        return START_STICKY
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun showIcon() {
        val outerLayoutParams = WindowManager.LayoutParams(
            300,
            300,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        outerLayoutParams.gravity = Gravity.CENTER
        mOuterLayoutParams?.let { param ->
            outerLayoutParams.x = param.x
            outerLayoutParams.y = param.y
        }

        val imageView = ImageView(this)
        imageView.setImageBitmap(mBitmap)

        imageView.setOnTouchListener(DragTouchListener())
        imageView.setOnClickListener(ClickListener())

        Log.i("FloatingAmiya", "showIcon")
        removeAllViews()
        addView(imageView, outerLayoutParams)
        mOuterLayoutParams = outerLayoutParams
    }

    private suspend fun requestLookingUpOperator(tags: List<String>): List<Map<String, Any>> {
        return suspendCancellableCoroutine { continuation ->
            ChannelManager.arknights.invokeMethod(
                "lookupOperator",
                tags,
                Callback(continuation)
            )
        }
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
            1200,
            700,
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

        Log.i("FloatingAmiya", "showPanel")
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
            }
            return false
        }
    }

    var switch: Boolean = false
    private inner class ClickListener : View.OnClickListener {
        @TargetApi(Build.VERSION_CODES.O)
        override fun onClick(v: View?) {
            Log.i("FloatingAmiya", "Clicked")
            if (!switch) {
                removeAllViews()
                startForegroundService(
                    Intent(this@FloatingAmiya, ScreenCaptureService::class.java).apply {
                        action = "CAPTURE"
                    })
            }
            else         showIcon()
            switch !=switch
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
