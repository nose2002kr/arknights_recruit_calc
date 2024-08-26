package proj.ksks.arknights.arknights_calc

import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

object ChannelManager {
    /* Constant val */
    const val ARKNIGHTS = "arknights"
    const val SCREENCAPTURE = "screen_capture"
    const val NATIVE_CHANNEL = "native_channel"

    /* Member */
    private val channelMap = mutableMapOf<String, MethodChannel>()

    fun installChannels(flutterEngine: FlutterEngine) {
        listOf(ARKNIGHTS, SCREENCAPTURE, NATIVE_CHANNEL)
            .iterator().forEach {
                v ->
                channelMap[v] =
                    MethodChannel(flutterEngine.dartExecutor.binaryMessenger, v)
            }
    }

    fun getChannelInstance(channel: String): MethodChannel {
        return channelMap[channel]!!
    }
}