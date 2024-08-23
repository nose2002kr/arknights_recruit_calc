package proj.ksks.arknights.arknights_calc

import android.annotation.TargetApi
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.os.Parcelable
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ScrollView
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.R
import com.google.android.material.chip.Chip
import io.flutter.plugin.common.MethodChannel


class FloatingAmiya : Service() {
    private lateinit var mWindowManager : WindowManager
    private lateinit var mBitmap : Bitmap
    private var mOuterLayoutParams: WindowManager.LayoutParams? = null
    private val addedViews = mutableListOf<View>()
    private var lowerLayout : LinearLayout? = null

    override fun onCreate() {
        super.onCreate()
        mWindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    private fun addView(view: View, params: WindowManager.LayoutParams) {
        mWindowManager.addView(view, params)
        addedViews.add(view)
    }

    private fun removeAllViews() {
        for (view in addedViews) {
            mWindowManager.removeView(view)
        }
        addedViews.clear()
    }

    class Callback : MethodChannel.Result {
        override fun success(var1: Any?) {
            Log.i("FloatingAmiya", "Received the result, Doctor: " + var1.toString())
        }
        override fun error(var1: String, var2: String?, var3: Any?) {
            Log.i("FloatingAmiya", "Received the error, Doctor: " + var1 + ", " + var2 + ", " + var3.toString())
        }
        override fun notImplemented() {
            Log.i("FloatingAmiya", "Received the notImplemented, Doctor")
        }
    }
    @TargetApi(Build.VERSION_CODES.TIRAMISU)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("FloatingAmiya", "Amiya, " + intent.action)
        if (intent.action.equals("STOP")) {
            Log.d("FloatingAmiya", "Hide amiya.")
            removeAllViews()
        } else if (intent.action.equals("START")) {
            Log.d("FloatingAmiya", "Show amiya.")
            mBitmap = intent.getParcelable("icon")!!
            showIcon()
        } else if (intent.action.equals("SHOW_PANEL")) {
            Log.d("FloatingAmiya", "Show panel.")
            val matchedTags : ArrayList<String> = intent.getStringArrayListExtra("tags")!!
            showPanel(matchedTags)
        }
        return START_STICKY
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun showIcon() {
        val outerLayoutParams = WindowManager.LayoutParams(
            300,
            300,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        outerLayoutParams.gravity = Gravity.CENTER
        mOuterLayoutParams?.let { param ->
            outerLayoutParams.x = param.x
            outerLayoutParams.y = param.y
        }

        val imageView = ImageView(this)
        imageView.setImageBitmap(mBitmap)

        imageView.setOnTouchListener(DragTouchListener())
        imageView.setOnClickListener(ClickListener())

        Log.i("FloatingAmiya", "showIcon")
        removeAllViews()
        addView(imageView, outerLayoutParams)
        mOuterLayoutParams = outerLayoutParams
    }

    private val selectedTag = ArrayList<String>()
    private fun requestLookingUpOperator(tags : ArrayList<String>) {
        ChannelManager.arknights.invokeMethod(
            "lookupOperator",
            tags,
            Callback())
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun showPanel(matchedTags : ArrayList<String>) {
        val themedContext: Context = ContextThemeWrapper(this, R.style.Theme_MaterialComponents_Light_NoActionBar)

        selectedTag.addAll(matchedTags)
        requestLookingUpOperator(selectedTag)

        // Set background with rounded corners
        val shape = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 50f // Adjust corner radius as needed
            setColor(Color.BLACK)
        }

        val layout = LinearLayout(themedContext).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            orientation = LinearLayout.VERTICAL
            background = shape

        }

        val adapterVal = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            selectedTag
        )

        val topBar = LinearLayout(themedContext).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(Color.GRAY)

            addView(Button(themedContext).apply {
                text = "Back"
                setOnClickListener { showIcon() }
            })
            addView(ListView(themedContext).apply {
                adapter = adapterVal
                orientation = LinearLayout.HORIZONTAL
            })
            orientation = LinearLayout.VERTICAL
        }
        layout.addView(topBar)

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
                    requestLookingUpOperator(selectedTag)
                }
            }

            val params = ViewGroup.MarginLayoutParams(
                ViewGroup.MarginLayoutParams.WRAP_CONTENT,
                ViewGroup.MarginLayoutParams.WRAP_CONTENT
            )
            params.marginEnd = 8 // 오른쪽 여백
            params.bottomMargin = 8 // 아래쪽 여백
            chip.layoutParams = params

            upperLayout.addView(chip)
        }
        upperScrollView.addView(upperLayout)
        layout.addView(upperScrollView)

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
        }
        lowerScrollView.addView(lowerLayout)
        layout.addView(lowerScrollView)

        val outerLayoutParams = WindowManager.LayoutParams(
            1200,
            700,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        outerLayoutParams.gravity = Gravity.CENTER
        mOuterLayoutParams?.let { param ->
            outerLayoutParams.x = param.x
            outerLayoutParams.y = param.y
        }
        layout.setOnTouchListener(DragTouchListener())

        Log.i("FloatingAmiya", "showPanel")
        removeAllViews()
        addView(layout, outerLayoutParams)
        mOuterLayoutParams = outerLayoutParams
    }

    private inner class DragTouchListener : View.OnTouchListener {
        private var initialX = 0
        private var initialY = 0
        private var initialTouchX = 0f
        private var initialTouchY = 0f

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = mOuterLayoutParams!!.x
                    initialY = mOuterLayoutParams!!.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    return false
                }
                MotionEvent.ACTION_MOVE -> {
                    mOuterLayoutParams!!.x = initialX + (event.rawX - initialTouchX).toInt()
                    mOuterLayoutParams!!.y = initialY + (event.rawY - initialTouchY).toInt()
                    mWindowManager.updateViewLayout(view, mOuterLayoutParams)
                    return false
                }
            }
            return false
        }
    }

    var switch: Boolean = false
    private inner class ClickListener : View.OnClickListener {
        @TargetApi(Build.VERSION_CODES.O)
        override fun onClick(v: View?) {
            Log.i("FloatingAmiya", "Clicked")
            if (!switch) {
                removeAllViews()
                startForegroundService(
                    Intent(this@FloatingAmiya, ScreenCaptureService::class.java).apply {
                        action = "CAPTURE"
                    })
            };
            else         showIcon();
            switch !=switch;
        }
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @Suppress("DEPRECATION")
    private inline fun <reified P : Parcelable> Intent.getParcelable(key: String): P? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(key, P::class.java)
        } else {
            getParcelableExtra(key)
        }
    }
}
