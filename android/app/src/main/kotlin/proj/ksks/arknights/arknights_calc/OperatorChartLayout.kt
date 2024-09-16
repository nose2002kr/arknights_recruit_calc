package proj.ksks.arknights.arknights_calc

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Color.rgb
import android.graphics.drawable.GradientDrawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.core.view.setMargins
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import kotlin.math.max


@SuppressLint("NewApi", "ViewConstructor")
class OperatorChartLayout (
    context: Context,
    matchedTags: List<String>,
    private val listener: Listener,
) : LinearLayout(context),
    ResizeableFloatingWidget {

    /* Constant val */
    private val TAG = "OperatorChartLayout"
    private val MIN_PANEL_WIDTH = 400
    private val MIN_PANEL_HEIGHT = 353
    private val MARGIN = 20

    /* Member */
    private var upperView: ScrollView
    private var lastClickedChip: Chip? = null

    @SuppressLint("SetTextI18n")
    fun updateOperatorView(operatorMap: List<Map<String, Any>>) {
        matchedOperatorLayout.removeAllViews()

        var firstElement = true
        operatorMap.forEach { it ->
            val name = it["name"] as String
            val tags = it["tags"] as List<*>
            val grade = it["grade"] as Int
            val color = (
                    when (grade) {
                        1 -> rgb(234,234,234)
                        2 -> rgb(234,234,234)
                        3 -> rgb(188,188,188)
                        4 -> rgb(191,141,240)
                        5 -> rgb(238,238,1)
                        6 -> rgb(252,194,120)
                        else -> rgb(234,234,234)
                    }
                )
            val spannableString = SpannableString("${grade}★ ${name}")
            spannableString.setSpan(
                ForegroundColorSpan(color),
                0, 2,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableString.setSpan(
                ForegroundColorSpan(Color.BLACK),
                2, spannableString.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            val chip = Chip(themedContext).apply {
                text = spannableString
                chipStrokeWidth = 5.0f
                chipBackgroundColor = ColorStateList.valueOf(Color.WHITE)
                chipStrokeColor = ColorStateList.valueOf(color)
                setOnClickListener { v ->
                    lastClickedChip?.chipBackgroundColor = ColorStateList.valueOf(Color.WHITE)
                    (v as Chip).chipBackgroundColor = ColorStateList.valueOf(rgb(200, 200, 200))
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
                                ColorStateList.valueOf(rgb(244, 244, 244))
                            u.chipStrokeWidth = 7.0f
                            u.chipStrokeColor = ColorStateList.valueOf(rgb(200,200,200))


                            val startColor = rgb(255,255,210)
                            val endColor = rgb(244,244,244)

                            val colorAnimation =
                                ValueAnimator.ofObject(ArgbEvaluator(), startColor, endColor)
                            colorAnimation.setDuration(200)

                            colorAnimation.addUpdateListener { animator ->
                                val animatedValue = animator.animatedValue as Int
                                val animatedColor = ColorStateList.valueOf(animatedValue)
                                u.chipBackgroundColor = animatedColor
                            }
                            colorAnimation.start()

                        } else {
                            u.chipBackgroundColor =
                                ColorStateList.valueOf(rgb(190, 190, 190))
                            u.setTextColor(ColorStateList.valueOf(rgb(100, 100, 100)))
                            u.chipStrokeWidth = 0f
                            u.chipStrokeColor = ColorStateList.valueOf(Color.WHITE)
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
                        marginEnd = 8
                        bottomMargin = 8
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

        val backgroundLayer = FrameLayout(themedContext).apply {
            // Set background with rounded corners
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                // for easier grip
                setMargins(MARGIN)
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 50f
                setColor(Color.GRAY)
            }
        }.also {
            addView(it)
        }


        val container = LinearLayout(themedContext).apply {
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                setMargins(10,10,10,10)
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 50f
                setColor(rgb(244,244,244))
            }
            orientation = VERTICAL
        }.also {
            backgroundLayer.addView(it)
        }


        // Top Bar ------------------ //
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
                            orientation = HORIZONTAL
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
            setBackgroundColor(Color.WHITE)
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
            params.marginEnd = 8
            params.bottomMargin = 8
            chip.layoutParams = params

            upperLayout.addView(chip)
        }
        ScrollView(themedContext).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                200,
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
            setBackgroundColor(Color.WHITE)
        }
        matchedOperatorLayout = LinearLayout(themedContext).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            minimumHeight = 70
            orientation = HORIZONTAL
        }
        lowerScrollView.addView(matchedOperatorLayout)
        container.addView(lowerScrollView)

        selectTag(matchedTags)
        updateTags(selectedTag)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.d(TAG, "re-size $w, $h [$oldw, $oldh]")

        val minimumUpperViewHeight = 120
        val usedSpace = 400
        upperView.post {
            upperView.layoutParams.apply {
                height = max(h - usedSpace, minimumUpperViewHeight)
            }.also {
                upperView.layoutParams = it
            }
            upperView.visibility = if (h < 480) GONE else VISIBLE
        }
    }

    override fun minimumWidth(): Int = MIN_PANEL_WIDTH
    override fun minimumHeight(): Int = MIN_PANEL_HEIGHT
    override fun marginForEasierGrab(): Int = MARGIN
}
