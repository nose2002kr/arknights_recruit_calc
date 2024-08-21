package proj.ksks.arknights.arknights_calc

import android.annotation.TargetApi
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.Icon
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.TextView


@TargetApi(Build.VERSION_CODES.TIRAMISU)
class ScreenCaptureService : Service() {
    private var mProjectionManager: MediaProjectionManager? = null
    private var mMediaProjection: MediaProjection? = null
    private var mVirtualDisplay: VirtualDisplay? = null
    private var mImageReader: ImageReader? = null


    private var mWindowManager: WindowManager? = null
    private var mFloatingView: View? = null

    override fun onCreate() {
        super.onCreate()
        mProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager?


        // Create the RelativeLayout
        mFloatingView = RelativeLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // Create the TextView
        val textView = TextView(this).apply {
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
            }
            text = "Floating Widget"
            textSize = 16f
            setTextColor(Color.WHITE)
        }

        // Add the TextView to the RelativeLayout
        (mFloatingView as RelativeLayout).addView(textView)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager?
        mWindowManager?.addView(mFloatingView, params)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val bitmap = intent?.getParcelableExtra("icon", Bitmap::class.java)

        createNotificationChannel(bitmap)
        if (intent != null && intent.action.equals("START_SCREEN_CAPTURE")) {
            startScreenCapture(intent)
        } else if (intent != null && intent.action.equals("STOP_SCREEN_CAPTURE")) {
            stopScreenCapture()
        }

        return START_STICKY
    }

    private fun createNotificationChannel(bitmap: Bitmap?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "ScreenRecorder", "Foreground notification",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(channel)
        }

        val notification = Notification.Builder(this, "ScreenRecorder")
            .setContentTitle("Screen Recording")
            .setContentText("Screen recording is running")
            .setSmallIcon(Icon.createWithBitmap(bitmap))
            .build()

        startForeground(1, notification)

    }
    private fun startScreenCapture(intent: Intent) {
        if (mMediaProjection == null) {
            mMediaProjection = mProjectionManager?.getMediaProjection(
                intent.getIntExtra("resultCode", Activity.RESULT_CANCELED),
                intent.getParcelableExtra<Intent>("data", Intent::class.java)!!
            )
        }

        mMediaProjection?.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                super.onStop()
                stopScreenCapture()
                Log.i("ScreenCaptureService", "MediaProjection stopped")
            }
        }, null)

        mImageReader = ImageReader.newInstance(1080, 1920, PixelFormat.RGBA_8888, 2)

        mVirtualDisplay = mMediaProjection?.createVirtualDisplay(
            "ScreenCapture",
            1080, 1920, resources.displayMetrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mImageReader!!.surface, null, null
        )

        mImageReader!!.setOnImageAvailableListener({ reader ->
            val image: Image = reader.acquireLatestImage()
            image.close()
            Log.i("ScreenCaptureService", "captured??");
        }, null)
        Log.i("ScreenCaptureService", "started service");
    }

    private fun stopScreenCapture() {
        mVirtualDisplay?.release()
        mMediaProjection?.stop()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}