package proj.ksks.arknights.arknights_calc

import android.Manifest
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import proj.ksks.arknights.arknights_calc.bridge.ChannelManager
import proj.ksks.arknights.arknights_calc.bridge.Tr
import proj.ksks.arknights.arknights_calc.service.ScreenCaptureService
import proj.ksks.arknights.arknights_calc.ui.UIPreference
import proj.ksks.arknights.arknights_calc.util.tagDictionary
import kotlin.coroutines.resume
import kotlin.io.path.Path
import kotlin.io.path.exists

class MainActivity: FlutterActivity() {
    /* Constant val */
    private val TAG = "MainActivity"
    private val REQUEST_CODE_PJM = 1
    private val REQUEST_CODE_OVL = 2
    private val REQUEST_CODE_NTF = 4
    private val FULL_PERMISSION = REQUEST_CODE_PJM or REQUEST_CODE_OVL or REQUEST_CODE_NTF


    /* Member */
    private var data: Intent? = null
    private var permissionGranted = 0
    private var alreadyProgressInRequest = false
    private suspend fun requestPermissions() {
        synchronized(this) {
            if (alreadyProgressInRequest) {
                return
            }
            alreadyProgressInRequest = true
        }

        permissionGranted = 0
        requestProjectionManagerPerm()
        if (permissionGranted and REQUEST_CODE_OVL == 0)
            requestOverlaysPerm()
        if (permissionGranted and REQUEST_CODE_NTF == 0)
            requestNotificationPerm()

        alreadyProgressInRequest = false
        if (permissionGranted != FULL_PERMISSION) {
            Log.w(TAG, "Permission is not fully granted.")
            return
        }

        Log.w(TAG, "Permission is not fully granted.")
        launchService(this.data)
    }

    private fun launchService(data: Intent?) {
        ScreenCaptureService.start(this, data)
    }

    private fun stopService() {
        ScreenCaptureService.stop(this)
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        Log.d(TAG, "synchronized with the android native");
        ChannelManager.installChannels(flutterEngine)

        ChannelManager.getChannelInstance(ChannelManager.SCREENCAPTURE)
            .setMethodCallHandler { call, _ ->
                when(call.method) {
                    "stopScreenCapture" -> {
                        stopService()
                    }
                    "startProjectionRequest" -> {
                        if (UIPreference.iconIsReadied()) {
                            CoroutineScope(Dispatchers.Main).launch {
                                requestPermissions()
                            }
                        } else {
                            Log.w(TAG, "Icon is not installed yet.")
                        }
                    }
                }
            }

        ChannelManager.getChannelInstance(ChannelManager.ARKNIGHTS)
            .setMethodCallHandler { call, _ ->
                when(call.method) {
                    "listTags" -> {
                        val list = call.arguments as ArrayList<String>
                        tagDictionary = list
                        Log.d(TAG, "Debug tagDictionary: " + (tagDictionary!!.get(0)))
                    }
                    "icon" -> {
                        val byteArray = call.arguments as ByteArray
                        UIPreference.icon = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                    }
                }
            }

        ChannelManager.getChannelInstance(ChannelManager.NATIVE_CHANNEL)
            .setMethodCallHandler { call, result ->
                when (call.method) {
                    "getCacheDir" -> result.success(context.cacheDir.toString())
                    "isFileExists" -> result.success(Path(call.arguments.toString()).exists())
                }
            }

        ChannelManager.getChannelInstance(ChannelManager.TRANSLATION)
            .setMethodCallHandler { call, result ->
                when (call.method) {
                    "installTranslation" -> {
                        Tr.installTranslation(call.arguments as Map<String, Any?>)
                        result.success(null)
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

        suspendCancellableCoroutine { cont ->
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
        suspendCancellableCoroutine { cont ->
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

            suspendCancellableCoroutine { cont ->
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

    override fun onDestroy() {
        super.onDestroy()
        stopService()
    }
}
