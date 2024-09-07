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
import android.widget.Toast

@TargetApi(Build.VERSION_CODES.TIRAMISU)
class ScreenCaptureService : Service() {
    /* Constant val */
    private val TAG = "ScreenCaptureService"
    private val NOTIFI_DESCRIPTION = Tr.NOTIFICATION_DESCRIPTION
    private val NOTIFI_NAME = Tr.TITLE;
    private val NOTIFI_CHANNEL_ID = "ArknightsRecruitCalc"
    private val NOTIFI_ID_RECORD = 1
    private val NOTIFI_ID_AMIYA = 2
    private val TOAST_MESSAGE_NOT_FOUND_TAGS = Tr.NOT_FOUND_TAGS
    private val TOAST_MESSAGE_CHECK_NOTIICATION = Tr.CHECK_NOTIICATION

    /* Member */

    private var mProjectionManager: MediaProjectionManager? = null
    private var mMediaProjection: MediaProjection? = null
    private var mVirtualDisplay: VirtualDisplay? = null
    private var mImageReader: ImageReader? = null
    private var mBitmapIcon : Bitmap? = null
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
        Log.d(TAG, "Trying to run service.")
    }

    private fun closeAmiya() {
        val intent = Intent(this, FloatingAmiya::class.java)
        intent.setAction("STOP")
        startService(intent)
        Log.d(TAG, "Trying to stop service.")
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
            Log.d(TAG, "capture start")
            val captureBitmap = image?.let { imageToBitmap(it) }
            captureBitmap?.let {
                ocrBitmap(it) { visionText ->
                    val matchedTag = ArrayList<String>()
                    for (block in visionText.textBlocks) {
                        val blockText: String = block.text
                        //Log.d(TAG, "recognized text: ${blockText}")
                        if (tagDictionary.contains(blockText.trim())) {
                            matchedTag.add(blockText.trim())
                        }
                    }
                    Log.d(TAG, "Complete detection.")
                    if (matchedTag.isEmpty()) {
                        Toast.makeText(this, TOAST_MESSAGE_NOT_FOUND_TAGS, Toast.LENGTH_SHORT).show()
                    }
                    startService(
                        Intent(this, FloatingAmiya::class.java).apply {
                            action = "SHOW_PANEL"
                            putExtra("tags", matchedTag)
                        })
                }
                Log.d(TAG, "capture success")
            }
            Log.d(TAG, "capture done")
        }

        return START_STICKY
    }

    private fun createNotification(bitmap: Bitmap?) {
        mBitmapIcon = bitmap;
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFI_CHANNEL_ID, "Foreground notification",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            manager.createNotificationChannel(channel)
        }

        startForeground(NOTIFI_ID_RECORD, Notification.Builder(this, NOTIFI_CHANNEL_ID)
            .build())

        val intent = Intent(this, ScreenCaptureService::class.java).apply {
            action = "STOP_SCREEN_CAPTURE"
        }
        val pendingIntent: PendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        manager.notify(NOTIFI_ID_AMIYA, Notification.Builder(this, NOTIFI_CHANNEL_ID)
            .setContentTitle(NOTIFI_NAME)
            .setContentText(NOTIFI_DESCRIPTION)
            .setContentIntent(pendingIntent)
            .setDeleteIntent(pendingIntent)
            .setAutoCancel(true)
            .setOngoing(true)
            .setSmallIcon(Icon.createWithBitmap(mBitmapIcon))
            .build())

        Toast.makeText(this, TOAST_MESSAGE_CHECK_NOTIICATION, Toast.LENGTH_SHORT).show()
    }

    private fun removeNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(NOTIFI_ID_RECORD)
        manager.cancel(NOTIFI_ID_AMIYA)
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
                Log.i(TAG, "MediaProjection stopped")
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
        Log.d(TAG, "started service");
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
        return if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(key, P::class.java)
        } else {
            getParcelableExtra(key)
        }
    }
}