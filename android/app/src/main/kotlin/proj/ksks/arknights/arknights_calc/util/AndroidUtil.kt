package proj.ksks.arknights.arknights_calc.util

import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.WindowManager
import java.io.Serializable
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

private val TAG = "AndroidUtil"

fun takeScreenSize(windowManager: WindowManager): Size {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Size(
            windowManager.currentWindowMetrics.bounds.width(),
            windowManager.currentWindowMetrics.bounds.height()
        )
    } else {
        @Suppress("DEPRECATION")
        with (windowManager.defaultDisplay) {
            getMetrics(DisplayMetrics())
            with (Point()) {
                getRealSize(this)
                Size(
                    this.x,
                    this.y
                )
            }
        }
    }
}

inline fun <reified T : Any> T.asMap() : Map<String, Any?> {
    return this::class.memberProperties
        .map { it as KProperty1<T, Any?> }
        .associate { prop -> prop.name to prop.get(this) }
}


fun startForegroundService(context: Context, clazz: Class<*>, action: String, params: Any? = null) {
    with (Intent(context, clazz)) {
        this.action = action
        Log.d(TAG, "params: ${params.toString()}")
        params?.let {
            params.asMap().run {
                Log.d(TAG, "params [map]: $this")
                this.forEach { (t, u) ->
                    this@with.putExtra(t, u)
                }
            }
        }
        Log.d(TAG, "extras! $extras")
        context.startForegroundService(this)
    }
}

fun startService(context: Context, clazz: Class<*>, action: String, params: Any? = null) {
    with (Intent(context, clazz)) {
        this.action = action
        params?.let {
            params.asMap().forEach { (t, u) ->
                this.putExtra(t, u)
            }
        }
        context.startService(this)
    }
}

private fun Intent.putExtra(t: String, u: Any?): Any {
    Log.d(TAG, "putExtra.. $t: $u")
    when (u) {
        is Boolean -> putExtra(t, u)
        is Bundle -> putExtra(t, u)
        is Parcelable -> putExtra(t, u)
        is String -> putExtra(t, u)
        is Array<*> -> putExtra(t, u)
        is BooleanArray -> putExtra(t, u)
        is Byte -> putExtra(t, u)
        is ByteArray -> putExtra(t, u)
        is Char -> putExtra(t, u)
        is CharArray -> putExtra(t, u)
        is CharSequence -> putExtra(t, u)
        is Double -> putExtra(t, u)
        is DoubleArray -> putExtra(t, u)
        is Float -> putExtra(t, u)
        is FloatArray -> putExtra(t, u)
        is Int -> putExtra(t, u)
        is IntArray -> putExtra(t, u)
        is Long -> putExtra(t, u)
        is LongArray -> putExtra(t, u)
        is Short -> putExtra(t, u)
        is ShortArray -> putExtra(t, u)
        is Serializable -> putExtra(t, u)
        else -> Log.w(TAG, "unknown type of parameter: $u ${u?.javaClass?.name}")
    }
    return this
}
