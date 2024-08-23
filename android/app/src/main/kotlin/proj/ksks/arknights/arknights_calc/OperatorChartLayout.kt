package proj.ksks.arknights.arknights_calc

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.ContextThemeWrapper
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
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
    listener: Listener,
) : LinearLayout(context) {

    fun updateOperatorView(operatorMap: Map<Int, List<String>>) {
        lowerLayout.removeAllViews()
        operatorMap.forEach { (_, u) ->
            u.forEach { v ->
                lowerLayout.addView(TextView(this.context).apply {
                    text = v
                })
            }
        }

    }

    interface Listener {
        fun requestDismiss(self: OperatorChartLayout)
        fun requestUpdate(self: OperatorChartLayout, tags: List<String>)
    }

    private var lowerLayout: LinearLayout
    private var matchedOperator: ArrayList<String> = ArrayList()

    private val selectedTag: ArrayList<String> = ArrayList()

    init {
        val themedContext: Context = ContextThemeWrapper(context, R.style.Theme_MaterialComponents_Light_NoActionBar)

        selectedTag.addAll(matchedTags)
        listener.requestUpdate(this, selectedTag)

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

        val adapterVal = ArrayAdapter(
            context,
            android.R.layout.simple_list_item_1,
            selectedTag
        )

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
            addView(ListView(themedContext).apply {
                adapter = adapterVal
                orientation = HORIZONTAL
            })
            orientation = VERTICAL
        }
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
                    adapterVal.notifyDataSetChanged()
                    listener.requestUpdate(this@OperatorChartLayout, selectedTag)
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
        lowerLayout = LinearLayout(themedContext).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            orientation = HORIZONTAL
            setBackgroundColor(Color.WHITE)
        }
        lowerScrollView.addView(lowerLayout)
        addView(lowerScrollView)

    }
}
