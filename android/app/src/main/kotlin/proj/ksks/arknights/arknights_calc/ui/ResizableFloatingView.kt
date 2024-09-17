package proj.ksks.arknights.arknights_calc.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Color.rgb
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
    private val MARGIN = 20
    private val HOLDER_WIDTH = 150
    private val HOLDER_HEIGHT = 15

    /* Member */
    private var container: FrameLayout

    abstract fun minimumWidth(): Int
    abstract fun minimumHeight(): Int
    fun marginForEasierGrab(): Int = MARGIN
    fun holderWidth(): Int = HOLDER_WIDTH

    fun addSubView(child: View) {
        container.addView(child)
    }

    init {
        val backgroundLayer = FrameLayout(context).apply {
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
                cornerRadius = 50f
                setColor(Color.GRAY)
            }
        }.also {
            addView(it)
        }

        container = FrameLayout(context).apply {
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                setMargins(10)
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 50f
                setColor(rgb(244,244,244))
            }
        }.also {
            backgroundLayer.addView(it)
        }

        View(context).apply {
            layoutParams = LayoutParams(HOLDER_WIDTH, HOLDER_HEIGHT).apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                setMargins(MARGIN - 2)
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 50f
                setColor(rgb(50, 50, 50))
            }
        }.also {
            addView(it)
        }
    }
}