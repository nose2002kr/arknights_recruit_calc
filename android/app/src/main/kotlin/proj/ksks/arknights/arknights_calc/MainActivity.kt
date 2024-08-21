package proj.ksks.arknights.arknights_calc

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context.MEDIA_PROJECTION_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel


class MainActivity: FlutterActivity() {
    private var data: Intent? = null
    private var resultCode: Int = 0
    private val REQUEST_CODE = 1000
    private val REQUEST_CODE2 = 1001
    private var resultCallback: MethodChannel.Result? = null
    private var bitmap: Bitmap? = null

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun startProjectionRequest() {
        val projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val captureIntent = projectionManager.createScreenCaptureIntent()
        startActivityForResult(captureIntent, REQUEST_CODE)
    }

    private fun request()
    {
        if (!Settings.canDrawOverlays(this) || data == null) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, REQUEST_CODE2)
        } else {
            launch(resultCode, data)
        }
    }

    private fun launch(resultCode: Int, data: Intent?) {
        val intent: Intent = Intent(this, ScreenCaptureService::class.java)
        intent.setAction("START_SCREEN_CAPTURE")
        intent.putExtra("resultCode", resultCode)
        intent.putExtra("data", data)
        intent.putExtra("icon", bitmap)
        startForegroundService(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            this.resultCode = resultCode
            this.data = data
            request()
        } else if (requestCode == REQUEST_CODE2) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                resultCallback?.success(true)

                launch(this.resultCode, this.data)
            } else {
                resultCallback?.success(false)
            }
        }
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        Log.i("MainActivity", "synchronized the app2");

        MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), "screen_capture")
            .setMethodCallHandler { call, result ->
                if (call.method.equals("stopScreenCapture")) {
                    val intent: Intent = Intent(this, ScreenCaptureService::class.java)
                    intent.setAction("STOP_SCREEN_CAPTURE")
                    stopService(intent)
                } else if (call.method.equals("startProjectionRequest")) {
                    val byteArray = call.arguments as ByteArray
                    bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                    startProjectionRequest()
                }
            }
    }
}
