package proj.ksks.arknights.arknights_calc

import android.util.Log
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object ChannelManager {
    /* Constant val */
    const val ARKNIGHTS = "arknights"
    const val SCREENCAPTURE = "screen_capture"
    const val NATIVE_CHANNEL = "native_channel"
    const val TRANSLATION = "translation"

    /* Member */
    private val channelMap = mutableMapOf<String, MethodChannel>()

    fun installChannels(flutterEngine: FlutterEngine) {
        listOf(ARKNIGHTS, SCREENCAPTURE, NATIVE_CHANNEL, TRANSLATION)
            .iterator().forEach {
                v ->
                channelMap[v] =
                    MethodChannel(flutterEngine.dartExecutor.binaryMessenger, v)
            }
    }

    fun getChannelInstance(channel: String): MethodChannel {
        return channelMap[channel]!!
    }


    suspend fun callFunction(channel: MethodChannel, method: String, argument: Any?): Any? {
        return suspendCancellableCoroutine { continuation ->
            channel.invokeMethod(
                method,
                argument,
                Callback(continuation)
            )
            Log.d("ChannelManager", "resumed")
        }
    }
}

class Callback<T>(
    private val continuation: kotlinx.coroutines.CancellableContinuation<T>
) : MethodChannel.Result {

    override fun success(var1: Any?) {
        val result = var1 as? T
        Log.d("ChannelManager", "success")
        if (result != null) {
            Log.d("ChannelManager", "call resume & ${result.toString()}")
            continuation.resume(result)
        } else {
            continuation.resumeWithException(IllegalArgumentException("Unexpected result type"))
        }
    }

    override fun error(var1: String, var2: String?, var3: Any?) {
        Log.d("ChannelManager", "error")
        continuation.resumeWithException(Exception("Error: $var1, $var2, $var3"))
    }

    override fun notImplemented() {
        Log.d("ChannelManager", "notImplemented")
        continuation.resumeWithException(NotImplementedError("Method not implemented"))
    }
}