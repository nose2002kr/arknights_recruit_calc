package proj.ksks.arknights.arknights_calc

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Color.rgb
import android.graphics.drawable.GradientDrawable
import android.view.ContextThemeWrapper
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip


@SuppressLint("NewApi", "ViewConstructor")
class OperatorChartLayout (
    context: Context,
    matchedTags: List<String>,
    private val listener: Listener,
) : LinearLayout(context) {

    fun updateOperatorView(operatorMap: List<Map<String, Any>>) {
        matchedOperatorLayout.removeAllViews()

        var firstElement = true
        operatorMap.forEach { it ->
            val name = it["name"] as String
            val tags = it["tags"] as List<*>
            val grade = it["grade"] as Int

            val chip = Chip(themedContext).apply {
                text = name
                chipStrokeWidth = 5.0f
                chipBackgroundColor = ColorStateList.valueOf(Color.WHITE)
                chipStrokeColor = (
                    when (grade) {
                        1 -> ColorStateList.valueOf(rgb(234,234,234))
                        2 -> ColorStateList.valueOf(rgb(234,234,234))
                        3 -> ColorStateList.valueOf(rgb(234,234,234))
                        4 -> ColorStateList.valueOf(rgb(191,141,240))
                        5 -> ColorStateList.valueOf(rgb(238,238,1))
                        6 -> ColorStateList.valueOf(rgb(252,194,120))
                        else -> ColorStateList.valueOf(rgb(234,234,234))
                    }
                )
                setOnClickListener {
                    selectedChipDictionary.forEach { (t, u) ->
                        if (tags.contains(t)) {
                            u.chipBackgroundColor =
                                ColorStateList.valueOf(rgb(244, 244, 244))
                            u.chipStrokeWidth = 5.0f
                            u.chipStrokeColor = ColorStateList.valueOf(rgb(233,243,215))


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
                                ColorStateList.valueOf(rgb(120, 120, 120))
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

    private fun unselectTag(tag: String) {
        selectedTag.remove(tag)
        chipDictionary[tag]?.isChecked = false
        updateTags(selectedTag)
    }

    private fun updateTags(tags: List<String>) {
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

        listener.requestUpdate(this@OperatorChartLayout, selectedTag)
    }

    init {

        // Set background with rounded corners
        val shapeGray = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 50f
            setColor(Color.GRAY)
        }

        // Set background with rounded corners
        val shapeWhite = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 50f
            setColor(rgb(244,244,244))
        }

        val container = LinearLayout(themedContext).apply {
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                setMargins(10,10,10,10)
            }
            background = shapeWhite
            orientation = VERTICAL
        }

        addView(container)

        layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        background = shapeGray

        selectedTagLayout = LinearLayout(themedContext).apply {
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = HORIZONTAL
        }

        container.addView(
            MaterialToolbar(themedContext).apply {
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
                        addView(selectedTagLayout)
                    }
                )
            }
        )

        val upperScrollView = ScrollView(themedContext).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                300
            )
        }
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

        for (tag in tagDictionary) {
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
        upperScrollView.addView(upperLayout)
        container.addView(upperScrollView)

        val lowerScrollView = ScrollView(themedContext).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        matchedOperatorLayout = LinearLayout(themedContext).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            orientation = HORIZONTAL
            setBackgroundColor(Color.WHITE)
        }
        lowerScrollView.addView(matchedOperatorLayout)
        container.addView(lowerScrollView)

        selectedTag.addAll(matchedTags)
        updateTags(selectedTag)
    }
}
