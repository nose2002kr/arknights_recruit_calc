package proj.ksks.arknights.arknights_calc

import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.util.Size
import android.view.WindowManager

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