package proj.ksks.arknights.arknights_calc.service

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
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import proj.ksks.arknights.arknights_calc.bridge.Tr
import proj.ksks.arknights.arknights_calc.ui.UIPreference
import proj.ksks.arknights.arknights_calc.util.fromIntent
import proj.ksks.arknights.arknights_calc.util.imageToBitmap
import proj.ksks.arknights.arknights_calc.util.ocrBitmap
import proj.ksks.arknights.arknights_calc.util.startForegroundService
import proj.ksks.arknights.arknights_calc.util.tagDictionary

class ScreenCaptureService : Service() {
    /* Constant val */
    private val TAG = "ScreenCaptureService"
    private val NOTIFI_DESCRIPTION = Tr.NOTIFICATION_DESCRIPTION
    private val NOTIFI_NAME = Tr.TITLE;
    private val NOTIFI_CHANNEL_ID = "ArknightsRecruitCalc"
    private val NOTIFI_ID_RECORD = 1
    private val NOTIFI_ID_AMIYA = 2
    private val TOAST_MESSAGE_NOT_FOUND_TAGS = Tr.NOT_FOUND_TAGS
    private val TOAST_MESSAGE_CHECK_NOTIICATION = Tr.CHECK_NOTIFICATION
    private val TOAST_MESSAGE_FAILED_TO_CONVERT_CAPTURE = Tr.FAILED_TO_CONVERT_CAPTURE

    /* Member */
    private var mProjectionManager: MediaProjectionManager? = null
    private var mMediaProjection: MediaProjection? = null
    private var mVirtualDisplay: VirtualDisplay? = null
    private var mImageReader: ImageReader? = null
    private var image: Image? = null

    companion object {
        private val ACTION_START_CAPTURE = "START_SCREEN_CAPTURE"
        private data class StartParam(val projectionMediaAccepted: Intent?)

        private val ACTION_STOP_CAPTURE = "STOP_SCREEN_CAPTURE"
        private val ACTION_CAPTURE = "CAPTURE"

        fun start(
            context: Context,
            projectionMediaAccepted: Intent?
        ) {
            startForegroundService(
                context,
                ScreenCaptureService::class.java,
                ACTION_START_CAPTURE,
                StartParam(projectionMediaAccepted = projectionMediaAccepted)
            )
        }

        fun stop(
            context: Context,
        ) {
            startForegroundService(
                context,
                ScreenCaptureService::class.java,
                ACTION_STOP_CAPTURE
            )
        }

        fun capture(
            context: Context,
        ) {
            startForegroundService(
                context,
                ScreenCaptureService::class.java,
                ACTION_CAPTURE
            )
        }
    }

    override fun onCreate() {
        super.onCreate()
        mProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager?
    }

    private fun launchAmiya() {
        FloatingAmiya.start(this)
    }

    private fun closeAmiya() {
        FloatingAmiya.stop(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?: return START_STICKY

        when (intent.action) {
            ACTION_START_CAPTURE -> {
                val param = StartParam::class.fromIntent(intent)
                createNotification()
                launchAmiya()
                stopScreenCapture()
                startScreenCapture(param.projectionMediaAccepted!!)
            }
            ACTION_STOP_CAPTURE -> {
                removeNotification()
                stopScreenCapture()
                closeAmiya()
                stopSelf()
            }
            ACTION_CAPTURE-> {
                captureImage()
            }
        }

        return START_STICKY
    }

    private fun createNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            NOTIFI_CHANNEL_ID, "Foreground notification",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        manager.createNotificationChannel(channel)

        startForeground(NOTIFI_ID_RECORD, Notification.Builder(this, NOTIFI_CHANNEL_ID)
            .build())

        val intent = Intent(this, ScreenCaptureService::class.java).apply {
            action = ACTION_STOP_CAPTURE
        }
        val pendingIntent: PendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        manager.notify(NOTIFI_ID_AMIYA, Notification.Builder(this, NOTIFI_CHANNEL_ID)
            .setContentTitle(NOTIFI_NAME)
            .setContentText(NOTIFI_DESCRIPTION)
            .setContentIntent(pendingIntent)
            .setDeleteIntent(pendingIntent)
            .setAutoCancel(true)
            .setOngoing(true)
            .setSmallIcon(Icon.createWithBitmap(UIPreference.icon))
            .build())

        Toast.makeText(this, TOAST_MESSAGE_CHECK_NOTIICATION, Toast.LENGTH_SHORT).show()
    }

    private fun removeNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(NOTIFI_ID_RECORD)
        manager.cancel(NOTIFI_ID_AMIYA)
    }

    private fun startScreenCapture(data: Intent) {
        if (mMediaProjection == null) {
            mMediaProjection = mProjectionManager?.getMediaProjection(
                Activity.RESULT_OK,
                data
            )
        }

        mMediaProjection?.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                super.onStop()
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
            /*Log.d(TAG, "capturing image. image:{\n" +
                    "  image.width: ${image?.width},\n" +
                    "  image.height: ${image?.height},\n" +
                    "  image.format: ${image?.format},\n" +
                    "  image.planes.size: ${image?.planes?.size},\n" +
                    "  image.timestamp: ${image?.timestamp}\n" +
                    "}")*/
        }, null)
        Log.d(TAG, "started service");
    }

    private fun stopScreenCapture() {
        mVirtualDisplay?.release()
        mMediaProjection?.stop()
        mMediaProjection = null
    }

    private fun captureImage() {
        Log.d(TAG, "capture start")
        var captureBitmap = image?.let { imageToBitmap(it) }
        if (captureBitmap == null) {
            Log.i(TAG, "caught the error. try one more again.")
            captureBitmap = image?.let { imageToBitmap(it) }
        }
        if (captureBitmap == null) {
            Toast.makeText(this, TOAST_MESSAGE_FAILED_TO_CONVERT_CAPTURE, Toast.LENGTH_SHORT).show()
            launchAmiya()
        }
        captureBitmap?.let {
            ocrBitmap(it) { visionText ->
                CoroutineScope(Dispatchers.Main).launch {
                    while (tagDictionary == null) {
                        Log.d(TAG, "yield until load tagDictionary")
                        delay(10)
                        yield()
                    }
                    val matchedTag = ArrayList<String>()
                    for (block in visionText.textBlocks) {
                        val blockText: String = block.text
                        //Log.d(TAG, "recognized text: ${blockText}")
                        if (tagDictionary!!.contains(blockText.trim())) {
                            matchedTag.add(blockText.trim())
                        }
                    }
                    Log.d(TAG, "Complete detection.")
                    if (matchedTag.isEmpty()) {
                        Toast.makeText(
                            this@ScreenCaptureService,
                            TOAST_MESSAGE_NOT_FOUND_TAGS,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    FloatingAmiya.showPanel(this@ScreenCaptureService, matchedTag)
                }
            }
            Log.d(TAG, "capture success")
        }
        Log.d(TAG, "capture done")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}