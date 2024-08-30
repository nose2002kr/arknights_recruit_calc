package proj.ksks.arknights.arknights_calc

import android.util.Log

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
            when (key) {
                "TITLE"                    -> TITLE = value.toString()
                "NOTIFICATION_DESCRIPTION" -> NOTIFICATION_DESCRIPTION = value.toString()
                "NOT_FOUND_TAGS"           -> NOT_FOUND_TAGS = value.toString()
                "CHECK_NOTIICATION"        -> CHECK_NOTIICATION = value.toString()
                else -> Log.d(TAG, "Unknown key is found ${key}")
            }
        }
        Log.d(TAG, "install Done.")
    }
}