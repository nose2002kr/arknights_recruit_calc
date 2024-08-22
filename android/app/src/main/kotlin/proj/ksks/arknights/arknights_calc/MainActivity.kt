package proj.ksks.arknights.arknights_calc

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class MainActivity: FlutterActivity() {
    private var data: Intent? = null
    private val REQUEST_CODE_PJM = 1
    private val REQUEST_CODE_OVL = 2
    private val REQUEST_CODE_NTF = 4
    private val fullPermission = REQUEST_CODE_PJM or REQUEST_CODE_OVL or REQUEST_CODE_NTF
    private var permissionGranted = 0
    private var bitmap: Bitmap? = null

    private suspend fun requestPermissions() {
        permissionGranted = 0
        requestProjectionManagerPerm()
        if (permissionGranted and REQUEST_CODE_OVL == 0)
            requestOverlaysPerm()
        if (permissionGranted and REQUEST_CODE_NTF == 0)
            requestNotificationPerm()

        if (permissionGranted != fullPermission) {
            Log.w("ArknightsCalc", "Permission is not fully granted.")
            return
        }

        launchService(Activity.RESULT_OK,this.data)

    }

    private fun launchService(resultCode: Int, data: Intent?) {
        val intent: Intent = Intent(this, ScreenCaptureService::class.java)
        intent.setAction("START_SCREEN_CAPTURE")
        intent.putExtra("resultCode", resultCode)
        intent.putExtra("data", data)
        intent.putExtra("icon", bitmap)
        startForegroundService(intent)
    }

    private fun stopService() {
        val intent: Intent = Intent(this, ScreenCaptureService::class.java)
        intent.setAction("STOP_SCREEN_CAPTURE")
        startForegroundService(intent)
    }

    @TargetApi(Build.VERSION_CODES.O)
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        Log.d("ArknightsCalc", "synchronized the app2");

        MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), "screen_capture")
            .setMethodCallHandler { call, result ->
                if (call.method.equals("stopScreenCapture")) {
                    val intent: Intent = Intent(this, ScreenCaptureService::class.java)
                    intent.setAction("STOP_SCREEN_CAPTURE")
                    stopService()
                } else if (call.method.equals("startProjectionRequest")) {
                    val byteArray = call.arguments as ByteArray
                    bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                    GlobalScope.launch {
                        requestPermissions()
                    }
                }
            }
    }


    private suspend fun requestOverlaysPerm() = withContext(Dispatchers.Main) {
        if (Settings.canDrawOverlays(this@MainActivity)) {
            permissionGranted = permissionGranted or REQUEST_CODE_OVL
            return@withContext
        }

        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivityForResult(intent, REQUEST_CODE_OVL)

        suspendCancellableCoroutine<Unit> { cont ->
            onActivityResultListener = { requestCode, resultCode, _ ->
                if (requestCode == REQUEST_CODE_OVL) {
                    if (resultCode == Activity.RESULT_OK) {
                        permissionGranted = permissionGranted or REQUEST_CODE_OVL
                    }
                    cont.resume(Unit)
                }
            }
        }
    }

    private suspend fun requestProjectionManagerPerm() = withContext(Dispatchers.Main) {
        val projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val captureIntent = projectionManager.createScreenCaptureIntent()
        startActivityForResult(captureIntent, REQUEST_CODE_PJM)
        suspendCancellableCoroutine<Unit> { cont ->
            onActivityResultListener = { requestCode, resultCode, data ->
                if (requestCode == REQUEST_CODE_PJM) {
                    if (resultCode == Activity.RESULT_OK) {
                        permissionGranted = permissionGranted or REQUEST_CODE_PJM
                    }
                    this@MainActivity.data = data
                    cont.resume(Unit)
                }
            }
        }
    }

    private suspend fun requestNotificationPerm() = withContext(Dispatchers.Main) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13 (API level 33) and above
            if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
                permissionGranted = permissionGranted or REQUEST_CODE_NTF
                return@withContext
            }

            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_CODE_NTF
            )

            suspendCancellableCoroutine<Unit> { cont ->
                onRequestPermissionsResultListener = { requestCode, _, grantResults ->
                    if (requestCode == REQUEST_CODE_NTF) {
                        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            permissionGranted = permissionGranted or REQUEST_CODE_NTF
                        }
                        cont.resume(Unit)
                    }
                }
            }
        } else {
            permissionGranted = permissionGranted or REQUEST_CODE_NTF
        }
    }

    // Function references to handle activity results and permission results in coroutines
    private var onActivityResultListener: ((Int, Int, Intent?) -> Unit)? = null
    private var onRequestPermissionsResultListener: ((Int, Array<out String>, IntArray) -> Unit)? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onActivityResultListener?.invoke(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResultListener?.invoke(requestCode, permissions, grantResults)
    }
}
