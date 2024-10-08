package proj.ksks.arknights.arknights_calc.ui

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import proj.ksks.arknights.arknights_calc.util.tagDictionary
import kotlin.math.max


@SuppressLint("NewApi", "ViewConstructor")
class OperatorChartLayout (
    context: Context,
    matchedTags: List<String>,
    private val listener: Listener,
): ResizableFloatingView(context) {

    /* Constant val */
    private val TAG = "OperatorChartLayout"
    private val preference = UIPreference.OperatorChart
    private val preferenceChip = UIPreference.OperatorChart.Chip

    /* Member */
    private var upperView: ScrollView
    private var lastClickedChip: Chip? = null

    @SuppressLint("SetTextI18n")
    fun updateOperatorView(operatorMap: List<Map<String, Any>>) {

        matchedOperatorLayout.removeAllViews()

        var firstElement = true
        operatorMap.forEach {
            val name = it["name"] as String
            val tags = it["tags"] as List<*>
            val grade = it["grade"] as Int
            val color = (
                    when (grade) {
                        6 -> preferenceChip.GRADE6_STROKE_COLOR
                        5 -> preferenceChip.GRADE5_STROKE_COLOR
                        4 -> preferenceChip.GRADE4_STROKE_COLOR
                        3 -> preferenceChip.GRADE3_STROKE_COLOR
                        else -> preferenceChip.GRADE2_STROKE_COLOR
                    }
                )
            val spannableString = SpannableString("${grade}★ ${name}")
            spannableString.setSpan(
                ForegroundColorSpan(color),
                0, 2,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableString.setSpan(
                ForegroundColorSpan(UIPreference.FONT_COLOR),
                2, spannableString.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            val chip = Chip(themedContext).apply {
                text = spannableString
                chipStrokeWidth = preferenceChip.THIN_STROKE_WIDTH
                chipBackgroundColor = ColorStateList.valueOf(preferenceChip.BG_COLOR)
                chipStrokeColor = ColorStateList.valueOf(color)
                layoutParams = MarginLayoutParams(
                    MarginLayoutParams.WRAP_CONTENT,
                    MarginLayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = preference.CHIP_BETWEEN
                }

                setOnClickListener { v ->
                    lastClickedChip?.chipBackgroundColor = ColorStateList.valueOf(preferenceChip.BG_COLOR)
                    (v as Chip).chipBackgroundColor = ColorStateList.valueOf(preferenceChip.BG_SELECTED_COLOR)
                    lastClickedChip = v

                    selectedTag.sortWith { first, second ->
                        if (tags.contains(first))
                            -1
                        else if (tags.contains(second))
                            0
                        else
                            1
                    }
                    updateTags(selectedTag, true)

                    selectedChipDictionary.forEach { (t, u) ->
                        if (tags.contains(t)) {
                            u.chipBackgroundColor =
                                ColorStateList.valueOf(preferenceChip.BG_HIGHLIGHT_COLOR)
                            u.chipStrokeWidth = preferenceChip.STROKE_WIDTH
                            u.chipStrokeColor = ColorStateList.valueOf(preferenceChip.STROKE_COLOR)

                            val colorAnimation =
                                ValueAnimator.ofObject(ArgbEvaluator(),
                                    preferenceChip.BG_HIGHLIGHT_ANI_START_COLOR,
                                    preferenceChip.BG_HIGHLIGHT_COLOR)
                            colorAnimation.setDuration(200)

                            colorAnimation.addUpdateListener { animator ->
                                val animatedValue = animator.animatedValue as Int
                                val animatedColor = ColorStateList.valueOf(animatedValue)
                                u.chipBackgroundColor = animatedColor
                            }
                            colorAnimation.start()

                        } else {
                            u.chipBackgroundColor =
                                ColorStateList.valueOf(preferenceChip.BG_DISABLED_COLOR)
                            u.setTextColor(ColorStateList.valueOf(UIPreference.FONT_DISABLED_COLOR))
                        }
                    }
                }
            }
            if (firstElement) {
                if (grade > 3) {
                    chip.performClick()
                }
                firstElement = false
            }
            matchedOperatorLayout.addView(chip)
        }
    }

    interface Listener {
        fun requestDismiss(self: OperatorChartLayout)
        fun requestUpdate(self: OperatorChartLayout, tags: List<String>)
    }

    private val themedContext: Context = ContextThemeWrapper(context, R.style.Theme_MaterialComponents_Light_NoActionBar)
    private val matchedOperatorLayout: LinearLayout

    private val selectedTagLayout: LinearLayout
    private val selectedTag: ArrayList<String> = ArrayList()
    private val chipDictionary: MutableMap<String, Chip> = mutableMapOf()
    private val selectedChipDictionary: MutableMap<String, Chip> = mutableMapOf()

    private fun selectTag(tag: String) {
        selectedTag.add(tag)
        chipDictionary[tag]?.isChecked = true
        updateTags(selectedTag)
    }

    private fun selectTag(tags: List<String>) {
        tags.forEach {
            selectedTag.add(it)
            chipDictionary[it]?.isChecked = true
        }
        updateTags(selectedTag)
    }

    private fun unselectTag(tag: String) {
        selectedTag.remove(tag)
        chipDictionary[tag]?.isChecked = false
        updateTags(selectedTag)
    }

    private fun updateTags(tags: List<String>, doNotRequestUpdate: Boolean = false) {
        selectedTagLayout.removeAllViews()

        tags.forEach { v ->
            val chip = Chip(themedContext).apply {
                text = v
                isClickable = true
                isCloseIconEnabled = true
                setOnClickListener {
                    when (isChecked) {
                        true -> {}
                        false -> {
                            unselectTag(text as String)
                        }
                    }
                }
                layoutParams = MarginLayoutParams(
                    MarginLayoutParams.WRAP_CONTENT,
                    MarginLayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = preference.CHIP_BETWEEN
                    bottomMargin = preference.CHIP_BETWEEN
                }
            }
            selectedChipDictionary[v] = chip
            selectedTagLayout.addView(chip)
        }

        if (!doNotRequestUpdate)
            listener.requestUpdate(this@OperatorChartLayout, selectedTag)
    }


    inner class AllowTouchToolbar(context: Context) : MaterialToolbar(context) {
        override fun onTouchEvent(ev: MotionEvent?): Boolean {
            return false
        }
    }

    init {
        // Top Bar ------------------ //
        val container = LinearLayout(themedContext).apply {
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            orientation = LinearLayout.VERTICAL
        }.also {
            addSubView(it)
        }

        container.addView(
            AllowTouchToolbar(themedContext).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                navigationIcon = themedContext.getDrawable(R.drawable.ic_m3_chip_close)
                setNavigationOnClickListener {
                    listener.requestDismiss(this@OperatorChartLayout)
                }
                addView(
                    HorizontalScrollView(themedContext).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        addView(LinearLayout(themedContext).apply {
                            layoutParams = LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            orientation = LinearLayout.HORIZONTAL
                        }.also {
                            selectedTagLayout = it
                        })
                    }
                )
            }
        )
        // ------------------ Top Bar //

        // Upper White Panel -------- //
        val upperLayout = FlexboxLayout(themedContext).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            clipToPadding = false
            clipChildren = false
            flexWrap = FlexWrap.WRAP
            setBackgroundColor(preference.PANEL_LIGHT_COLOR)
        }

        for (tag in tagDictionary!!) {
            val chip = Chip(themedContext).apply {
                text = tag
                isClickable = true
                isCheckable = true
                setOnClickListener {
                    when (isChecked) {
                        true -> selectTag(text as String)
                        false -> unselectTag(text as String)
                    }
                }
            }
            chipDictionary[tag] = chip

            val params = MarginLayoutParams(
                MarginLayoutParams.WRAP_CONTENT,
                MarginLayoutParams.WRAP_CONTENT
            )
            params.marginEnd = preference.CHIP_BETWEEN
            params.bottomMargin = preference.CHIP_BETWEEN
            chip.layoutParams = params

            upperLayout.addView(chip)
        }
        ScrollView(themedContext).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
            addView(upperLayout)
        }.also {
            upperView = it
            container.addView(it)
        }
        // -------- Upper White Panel //

        val lowerScrollView = HorizontalScrollView(themedContext).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(preference.PANEL_LIGHT_COLOR)
        }
        matchedOperatorLayout = LinearLayout(themedContext).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            minimumHeight = preference.MIN_OPERATORS_VIEW_HEIGHT
            orientation = LinearLayout.HORIZONTAL
        }
        lowerScrollView.addView(matchedOperatorLayout)
        container.addView(lowerScrollView)

        selectTag(matchedTags)
        updateTags(selectedTag)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.d(TAG, "re-size $w, $h [$oldw, $oldh]")

        upperView.post {
            upperView.layoutParams.apply {
                height = max(h - preference.CONTAINER_SIZE, preference.MIN_TAGS_VIEW_HEIGHT)
            }.also {
                upperView.layoutParams = it
            }
            upperView.visibility = if (h < preference.HIDING_OF_TAGS_VIEW_THRESHOLD)
                GONE else VISIBLE
        }
    }

    override fun minimumWidth(): Int = preference.MIN_WIDTH
    override fun minimumHeight(): Int = preference.MIN_HEIGHT
}
