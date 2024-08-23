package proj.ksks.arknights.arknights_calc

import android.annotation.TargetApi
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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
import android.os.Build.VERSION.SDK_INT
import android.os.IBinder
import android.os.Parcelable
import android.util.Log

@TargetApi(Build.VERSION_CODES.TIRAMISU)
class ScreenCaptureService : Service() {
    private var mProjectionManager: MediaProjectionManager? = null
    private var mMediaProjection: MediaProjection? = null
    private var mVirtualDisplay: VirtualDisplay? = null
    private var mImageReader: ImageReader? = null
    private var mBitmapIcon : Bitmap? = null
    private val notificationChannelId : String = "Arknights recruit calc"
    private val notificationRecorderId = 1
    private val notificationAmiyaId = 2
    private var image: Image? = null

    override fun onCreate() {
        super.onCreate()
        mProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager?
    }

    private fun launchAmiya(bitmap: Bitmap?) {
        val intent = Intent(this, FloatingAmiya::class.java)
        intent.setAction("START")
        intent.putExtra("icon", bitmap)
        startService(intent)
        Log.d("ScreenCaptureService", "Trying to run service.")
    }

    private fun closeAmiya() {
        val intent = Intent(this, FloatingAmiya::class.java)
        intent.setAction("STOP")
        startService(intent)
        Log.d("ScreenCaptureService", "Trying to stop service.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val bitmap: Bitmap? = intent?.getParcelable("icon")

        if (intent != null && intent.action.equals("START_SCREEN_CAPTURE")) {
            createNotification(bitmap)
            launchAmiya(mBitmapIcon)
            stopScreenCapture()
            startScreenCapture(intent)
        } else if (intent != null && intent.action.equals("STOP_SCREEN_CAPTURE")) {
            removeNotification()
            stopScreenCapture()
            closeAmiya()
            stopSelf()
        } else if (intent != null && intent.action.equals("CAPTURE")) {
            Log.d("ScreenCaptureService", "capture start")
            val captureBitmap = image?.let { imageToBitmap(it) }
            captureBitmap?.let {
                ocrBitmap(it, { visionText ->
                    val matchedTag = ArrayList<String>()
                    for (block in visionText.textBlocks) {
                        val blockText : String = block.text
                        if (tagDictionary.contains(blockText.trim())) {
                            matchedTag.add(blockText.trim())
                        }
                    }
                    Log.d("ScreenCaptureService", "Complete detection.")
                    startService(
                        Intent(this, FloatingAmiya::class.java).apply {
                            action = "SHOW_PANEL"
                            putExtra("tags", matchedTag)
                    })
                })
                Log.d("ScreenCaptureService", "capture success")
            }
            Log.d("ScreenCaptureService", "capture done")
        }

        return START_STICKY
    }

    private fun createNotification(bitmap: Bitmap?) {
        mBitmapIcon = bitmap;
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannelId, "Foreground notification",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            manager.createNotificationChannel(channel)
        }

        val notification = Notification.Builder(this, notificationChannelId)
            .build()

        startForeground(notificationRecorderId, notification)

        val intent = Intent(this, ScreenCaptureService::class.java).apply {
            action = "STOP_SCREEN_CAPTURE"
        }
        val pendingIntent: PendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val noti: Notification = Notification.Builder(this, notificationChannelId)
            .setContentTitle("Arknights calculator")
            .setContentText("Tap to close.")
            .setContentIntent(pendingIntent)
            .setDeleteIntent(pendingIntent)
            .setAutoCancel(true)
            .setOngoing(true)
            .setSmallIcon(Icon.createWithBitmap(mBitmapIcon))
            .build()

        manager.notify(notificationAmiyaId, noti)
    }

    private fun removeNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(notificationRecorderId)
        manager.cancel(notificationAmiyaId)
    }

    private fun startScreenCapture(intent: Intent) {
        if (mMediaProjection == null) {
            mMediaProjection = mProjectionManager?.getMediaProjection(
                Activity.RESULT_OK,
                intent.getParcelable("data")!!
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
            image?.close()
            image = reader.acquireLatestImage()
        }, null)
        Log.d("ScreenCaptureService", "started service");
    }

    private fun stopScreenCapture() {
        mVirtualDisplay?.release()
        mMediaProjection?.stop()
        mMediaProjection = null
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