package proj.ksks.arknights.arknights_calc.ui

import android.graphics.Bitmap
import android.graphics.Color

object UIPreference {
    object OperatorChart {
        val DEFAULT_WIDTH = 1200
        val DEFAULT_HEIGHT = 700

        val MIN_WIDTH = 400
        val MIN_HEIGHT = 353

        val TAG_BETWEEN = 8

        val MIN_TAGS_VIEW_HEIGHT = 120
        val CONTAINER_SIZE = 400
        val THRESHOLD_FOR_HIDING_OF_TAGS_VIEW = 480

        val COLOR_PANEL_LIGHT: Int = Color.WHITE

        object Chip {
            val STROKE_THIN_WIDTH: Float = 5.0f
            val STROKE_WIDTH: Float = 7.0f

            val COLOR_BACKGROUND: Int = Color.WHITE
            val COLOR_BACKGROUND_SELECTED: Int = Color.parseColor("#C8C8C8")
            val COLOR_BACKGROUND_DISABLED: Int = Color.parseColor("#BEBEBE")
            val COLOR_BACKGROUND_HIGHLIGHT: Int = Color.parseColor("#F4F4F4")
            val COLOR_BACKGROUND_HIGHLIGHT_ANI_START: Int = Color.parseColor("#FFFFD2")
            val COLOR_STROKE: Int = Color.parseColor("#C8C8C8")

            val COLOR_GRADE6_STROKE: Int = Color.parseColor("#FCC278")
            val COLOR_GRADE5_STROKE: Int = Color.parseColor("#EEEE01")
            val COLOR_GRADE4_STROKE: Int = Color.parseColor("#BF8DF0")
            val COLOR_GRADE3_STROKE: Int = Color.parseColor("#BCBCBC")
            val COLOR_GRADE2_STROKE: Int = Color.parseColor("#EAEAEA")
        }
    }

    object Icon {
        val SIZE = 200
        val SHADOW_MARGIN = 20
        val ELEVATION = 10f
    }

    val COLOR_FONT: Int = Color.BLACK
    val COLOR_FONT_DISABLED: Int = Color.parseColor("#646464")

    lateinit var icon: Bitmap
    fun iconIsReadied() = this::icon.isInitialized
}