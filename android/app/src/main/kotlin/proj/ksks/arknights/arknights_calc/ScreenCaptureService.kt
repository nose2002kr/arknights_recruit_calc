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


@TargetApi(Build.VERSION_CODES.TIRAMISU)
class ScreenCaptureService : Service() {
    private var mProjectionManager: MediaProjectionManager? = null
    private var mMediaProjection: MediaProjection? = null
    private var mVirtualDisplay: VirtualDisplay? = null
    private var mImageReader: ImageReader? = null

    private var mBitmapIcon : Bitmap? = null

    override fun onCreate() {
        super.onCreate()
        mProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager?
    }

    fun launchAmiya(bitmap: Bitmap?) {
        val intent = Intent(this, FloatingAmiya::class.java)
        intent.putExtra("icon", bitmap)
        startService(intent)
        Log.i("ScreenCaptureService", "Trying to run service.")
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
        mBitmapIcon = bitmap;

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

        launchAmiya(mBitmapIcon)
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
            //Log.i("ScreenCaptureService", "captured??");
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