package proj.ksks.arknights.arknights_calc.ui

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.setMargins

abstract class ResizableFloatingView(
    context: Context
): FrameLayout(context) {

    /* Constant val */
    private val TAG = "ResizableFloatingView"
    private val preference = UIPreference.ResizeableFloatingView

    /* Member */
    private var container: FrameLayout

    abstract fun minimumWidth(): Int
    abstract fun minimumHeight(): Int
    fun marginForEasierGrab(): Int = preference.MARGIN
    fun holderWidth(): Int = preference.HOLDER_WIDTH

    fun addSubView(child: View) {
        container.addView(child)
    }

    init {
        val grabLayer = FrameLayout(context).apply {
            // Set background with rounded corners
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                // for easier grip
                setMargins(marginForEasierGrab())
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = UIPreference.CORNER_RADIUS
                setColor(preference.BORDER_COLOR)
            }
        }.also {
            addView(it)
        }

        container = FrameLayout(context).apply {
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                setMargins(preference.BORDER_WIDTH)
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = UIPreference.CORNER_RADIUS
                setColor(preference.PANEL_LIGHT_GRAY_COLOR)
            }
        }.also {
            grabLayer.addView(it)
        }

        View(context).apply {
            layoutParams = LayoutParams(preference.HOLDER_WIDTH, preference.HOLDER_HEIGHT).apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                setMargins(preference.MARGIN - 2)
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = UIPreference.CORNER_RADIUS
                setColor(preference.HOLDER_COLOR)
            }
        }.also {
            addView(it)
        }
    }
}