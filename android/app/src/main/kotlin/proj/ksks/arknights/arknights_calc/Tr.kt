package proj.ksks.arknights.arknights_calc

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.KMutableProperty

object Tr {
    /* Constant value */
    val TAG = "Tr"

    /* Constant Member */
    lateinit var TITLE: String
    lateinit var NOTIFICATION_DESCRIPTION: String
    lateinit var NOT_FOUND_TAGS: String
    lateinit var CHECK_NOTIICATION: String

    fun installTranslation(translations: Map<String, Any?>) {
        translations.forEach { (key, value) ->
            val property = Tr::class.memberProperties.find { it.name == key }
            if (property is KMutableProperty<*> && value is String) {
                property.isAccessible = true
                property.setter.call(Tr, value)
            }
        }
        Log.d(TAG, "install Done.")
    }
}