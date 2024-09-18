package proj.ksks.arknights.arknights_calc.util

import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Parcelable
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.WindowManager
import java.io.Serializable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

private val TAG = "AndroidUtil"

fun takeScreenSize(windowManager: WindowManager): Size {
    return if (SDK_INT >= Build.VERSION_CODES.R) {
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

inline fun <reified T : KClass<R>, R> T.fromIntent(intent: Intent): R {
    val properties = (this as KClass<*>).memberProperties.toMutableList()
    with (this.java.declaredConstructors.maxByOrNull { t -> t.parameterCount }) {
        //Log.d("AVoid", "parameterCount: ${this?.parameterCount}")
        this?.parameters?.toList()?.map {

            val prop = properties.find {
                prop ->
                    //Log.d("AVoid", "properties.find: ${prop.name} [${prop.returnType}]")
                    (prop.returnType.classifier as KClass<*>).java == it.type
            }
            prop ?: throw RuntimeException("Not found with ${it.type}")
            properties.remove(prop)

            if (intent.hasExtra(prop.name)) {
                intent.getExtra(prop.name, it.type)
            } else {
                null
            }
        }.run {
            //Log.d("AVoid", "do instantiate ${this!!.toTypedArray()}")
            return this@with?.newInstance(*this!!.toTypedArray()) as R
        }
    }
}

fun startForegroundService(context: Context, clazz: Class<*>, action: String, params: Any? = null) {
    with (Intent(context, clazz)) {
        this.action = action
        params?.let {
            params.asMap().forEach { (t, u) ->
                this.putExtra(t, u)
            }
        }
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

fun Intent.putExtra(t: String, u: Any?): Any {
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

@Suppress("DEPRECATION")
inline fun <reified P : Parcelable> Intent.getParcelable(key: String): P? {
    return if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(key, P::class.java)
    } else {
        getParcelableExtra(key)
    }
}

@Suppress("DEPRECATION")
fun Intent.getExtra(t: String, typeIndicator: Class<*>): Any? {
    val map = mapOf (
        Boolean::class.java to { getBooleanExtra(t, false) },
        Bundle::class.java to { getBundleExtra(t) },
        Parcelable::class.java to { getParcelable(t) },
        String::class.java to { getStringExtra(t) },
        BooleanArray::class.java to { getBooleanArrayExtra(t) },
        Byte::class.java to { getByteExtra(t, 0) },
        ByteArray::class.java to { getByteArrayExtra(t) },
        Char::class.java to { getCharExtra(t, '\u0000') },
        CharArray::class.java to { getCharArrayExtra(t) },
        CharSequence::class.java to { getCharSequenceExtra(t) },
        Double::class.java to { getDoubleExtra(t, 0.0) },
        DoubleArray::class.java to { getDoubleArrayExtra(t) },
        Float::class.java to { getFloatExtra(t, 0f) },
        FloatArray::class.java to { getFloatArrayExtra(t) },
        Int::class.java to { getIntExtra(t, 0) },
        IntArray::class.java to { getIntArrayExtra(t) },
        Long::class.java to { getLongExtra(t, 0L) },
        LongArray::class.java to { getLongArrayExtra(t) },
        Short::class.java to { getShortExtra(t, 0) },
        ShortArray::class.java to { getShortArrayExtra(t) },
        Serializable::class.java to { getSerializableExtra(t) }
    )

    return map.firstNotNullOfOrNull { entry ->
        if (entry.key.isAssignableFrom(typeIndicator)) {
            entry.value
        } else {
            null
        }
    }?.invoke() ?: {
        Log.w(TAG, "Unknown type of parameter: $t ${typeIndicator.name}")
        null
    }
}
