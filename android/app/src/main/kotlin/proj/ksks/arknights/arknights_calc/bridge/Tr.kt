package proj.ksks.arknights.arknights_calc.bridge

import android.util.Log
import kotlin.properties.ReadOnlyProperty

object Tr {
    /* Constant value */
    private const val TAG = "Tr"

    val TITLE: String by translate()
    val NOTIFICATION_DESCRIPTION: String by translate()
    val NOT_FOUND_TAGS: String by translate()
    val CHECK_NOTIFICATION: String by translate()
    val FAILED_TO_CONVERT_CAPTURE: String by translate()
    val QUIT : String by translate()


    private fun translate(): ReadOnlyProperty<Any?, String> {
        return ReadOnlyProperty { _, property ->
            translations?.get(property.name)?.toString() ?: property.name
        }
    }

    private var translations: Map<String, Any?>? = null
    fun installTranslation(translations: Map<String, Any?>) {
        Tr.translations = translations
        Log.d(TAG, "install Done.")
    }
}