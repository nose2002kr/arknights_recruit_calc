package proj.ksks.arknights.arknights_calc.ui

import android.graphics.Bitmap
import android.graphics.Color

object UIPreference {
    val COLOR_FRAME_BORDER: Int = Color.parseColor("#000000")
    val COLOR_PANEL_LIGHT_GRAY: Int = Color.parseColor("#000000")
    val COLOR_PANEL_WHITE: Int = Color.parseColor("#000000")

    val COLOR_CHIP_BACKGROUND: Int = Color.parseColor("#000000")
    val COLOR_CHIP_BACKGROUND_SELECTED: Int = Color.parseColor("#000000")
    val COLOR_CHIP_BACKGROUND_DISABLED: Int = Color.parseColor("#000000")
    val COLOR_CHIP_BACKGROUND_HIGHLIGHT: Int = Color.parseColor("#000000")

    val COLOR_GRADE6_STROKE: Int = Color.parseColor("#000000")
    val COLOR_GRADE5_STROKE: Int = Color.parseColor("#000000")
    val COLOR_GRADE4_STROKE: Int = Color.parseColor("#000000")
    val COLOR_GRADE3_STROKE: Int = Color.parseColor("#000000")
    val COLOR_GRADE2_STROKE: Int = Color.parseColor("#000000")

    val ICON_SIZE: Int = 240
    val FRAME_WIDTH: Int = 600

    val STROKE_WIDTH: Int = 8

    lateinit var icon: Bitmap
    fun iconIsReadied() = this::icon.isInitialized
}