package proj.ksks.arknights.arknights_calc.ui

import android.graphics.Bitmap
import android.graphics.Color

object UIPreference {
    object OperatorChart {
        val DEFAULT_WIDTH = 1200
        val DEFAULT_HEIGHT = 700

        val MIN_WIDTH = 400
        val MIN_HEIGHT = 353

        val CHIP_BETWEEN = 8

        val MIN_TAGS_VIEW_HEIGHT = 120
        val MIN_OPERATORS_VIEW_HEIGHT = 80
        val CONTAINER_SIZE = 400
        val HIDING_OF_TAGS_VIEW_THRESHOLD = 480

        val PANEL_LIGHT_COLOR: Int = Color.WHITE

        object Chip {
            val THIN_STROKE_WIDTH: Float = 5.0f
            val STROKE_WIDTH: Float = 7.0f

            val BG_COLOR: Int = Color.WHITE
            val BG_SELECTED_COLOR: Int = Color.parseColor("#C8C8C8")
            val BG_DISABLED_COLOR: Int = Color.parseColor("#BEBEBE")
            val BG_HIGHLIGHT_COLOR: Int = Color.parseColor("#F4F4F4")
            val BG_HIGHLIGHT_ANI_START_COLOR: Int = Color.parseColor("#FFFFD2")
            val STROKE_COLOR: Int = Color.parseColor("#C8C8C8")

            val GRADE6_STROKE_COLOR: Int = Color.parseColor("#FCC278")
            val GRADE5_STROKE_COLOR: Int = Color.parseColor("#EEEE01")
            val GRADE4_STROKE_COLOR: Int = Color.parseColor("#BF8DF0")
            val GRADE3_STROKE_COLOR: Int = Color.parseColor("#BCBCBC")
            val GRADE2_STROKE_COLOR: Int = Color.parseColor("#EAEAEA")
        }
    }

    object Icon {
        val SIZE = 200
        val SHADOW_MARGIN = 20
        val ELEVATION = 10f
        val BG_COLOR = Color.WHITE
    }

    object TerminateIndicator {
        val BG_NORMAL_STATE_COLOR = Color.parseColor("#FF444444")
        val BG_ACTIVE_STATE_COLOR = Color.parseColor("#AAD63A31")
        val CORNER_RADIUS = 34f

        val FONT_SIZE = 22f
        val FONT_COLOR = Color.WHITE

        val PADDING_LEFT = 26
        val PADDING_TOP = 16
        val PADDING_RIGHT = 46
        val PADDING_BOTTOM = 26

        val LEADING_ICON_SIZE = 60
        val ICON_BETWEEN = 16
    }

    object RubberBand {
        val FILL_COLOR = Color.parseColor("#12818589")
        val STROKE_COLOR = Color.parseColor("#FF708090")
        val STROKE_WIDTH = 9f
    }

    object ResizeableFloatingView {
        val MARGIN = 20
        val HOLDER_WIDTH = 150
        val HOLDER_HEIGHT = 15
        val HOLDER_COLOR: Int = Color.parseColor("#323232")

        val PANEL_LIGHT_GRAY_COLOR: Int = Color.parseColor("#F4F4F4")
        val BORDER_WIDTH: Int = 10
        val BORDER_COLOR: Int = Color.GRAY
    }

    val FONT_COLOR: Int = Color.BLACK
    val FONT_DISABLED_COLOR: Int = Color.parseColor("#646464")
    val CORNER_RADIUS = 50f

    lateinit var icon: Bitmap
    fun iconIsReadied() = this::icon.isInitialized
}