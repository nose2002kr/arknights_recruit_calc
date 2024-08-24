package proj.ksks.arknights.arknights_calc

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.ContextThemeWrapper
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.R
import com.google.android.material.chip.Chip

@SuppressLint("NewApi", "ViewConstructor")
class OperatorChartLayout (
    context: Context,
    matchedTags: List<String>,
    private val listener: Listener,
) : LinearLayout(context) {

    fun updateOperatorView(operatorMap: Map<Int, List<String>>) {
        matchedOperatorLayout.removeAllViews()
        operatorMap.forEach { (_, u) ->
            u.forEach { v ->
                val chip = Chip(themedContext).apply {
                    text = v
                }
                matchedOperatorLayout.addView(chip)
            }
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
                            selectedTag.remove(text as String)
                            updateTags(selectedTag)
                        }
                    }
                }
                layoutParams = MarginLayoutParams(
                    MarginLayoutParams.WRAP_CONTENT,
                    MarginLayoutParams.WRAP_CONTENT
                    ).apply {
                        marginEnd= 8
                        bottomMargin = 8
                    }
            }
            selectedTagLayout.addView(chip)
        }

        listener.requestUpdate(this@OperatorChartLayout, selectedTag)
    }


    init {
        // Set background with rounded corners
        val shape = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 50f
            setColor(Color.BLACK)
        }

        layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        orientation = VERTICAL
        background = shape

        val topBar = LinearLayout(themedContext).apply {
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(Color.GRAY)

            addView(Button(themedContext).apply {
                text = "Back"
                setOnClickListener { listener.requestDismiss(this@OperatorChartLayout) }
            })
            orientation = VERTICAL
        }

        selectedTagLayout = LinearLayout(themedContext).apply {
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = HORIZONTAL
        }

        topBar.addView(selectedTagLayout)
        addView(topBar)

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
                        true -> selectedTag.add(text as String)
                        false -> selectedTag.remove(text as String)
                    }
                    updateTags(selectedTag)
                }
            }

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
        addView(upperScrollView)

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
        addView(lowerScrollView)

        selectedTag.addAll(matchedTags)
        updateTags(selectedTag)
    }
}
