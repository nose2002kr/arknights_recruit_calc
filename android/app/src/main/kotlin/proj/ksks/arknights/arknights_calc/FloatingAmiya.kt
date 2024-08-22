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
import io.flutter.embedding.android.FlutterView
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.view.FlutterMain

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

    @TargetApi(Build.VERSION_CODES.O)
    private fun showPanel(matchedTags : ArrayList<String>?) {
        val flutterEngine = FlutterEngine(this)
        flutterEngine.dartExecutor.executeDartEntrypoint(
            DartExecutor.DartEntrypoint(
                FlutterMain.findAppBundlePath(),
                "redirect_recruit_calc_view"
            )
        )
        val flutterView = FlutterView(this)
        flutterView.attachToFlutterEngine(flutterEngine)

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
        flutterView.setOnTouchListener(DragTouchListener())
        flutterView.setOnClickListener(ClickListener())

        Log.i("FloatingAmiya", "showPanel")
        removeAllViews()
        addView(flutterView, outerLayoutParams)
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
            };
            else         showIcon();
            switch !=switch;
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
