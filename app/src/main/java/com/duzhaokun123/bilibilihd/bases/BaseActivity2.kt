package com.duzhaokun123.bilibilihd.bases

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.DisplayCutout
import android.view.MenuItem
import android.view.View
import androidx.annotation.Keep
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.*
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.duzhaokun123.bilibilihd.R
import com.duzhaokun123.bilibilihd.utils.OtherUtils
import com.duzhaokun123.bilibilihd.utils.TipUtil

abstract class BaseActivity2<layout : ViewDataBinding> : AppCompatActivity() {
    enum class Config {
        // FIXME: 21-1-29 Activity 重构导致全屏失效
        FULLSCREEN,
        HIDE_ACTION_BAR,
        FIX_LAYOUT,
        TRANSPARENT_ACTION_BAR
    }

    val className by lazy { this::class.simpleName }
    val startIntent: Intent by lazy { intent }
    val navigationBarHeight: Int
        get() {
            val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
            return if (resourceId > 0)
                resources.getDimensionPixelSize(resourceId)
            else 0
        }
    val statusBarHeight: Int
        get() {
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            return if (resourceId > 0)
                resources.getDimensionPixelSize(resourceId)
            else 0
        }
    val actionBarHeight get() = supportActionBar?.height ?: 0
    val fixTopHeight
        get() = maxOf(statusBarHeight, displayCutout?.compatSafeInsetTop() ?: 0) + actionBarHeight
    val fixBottomHeight
        get() = if (isNavigationBarOnBottom)
            navigationBarHeight + (displayCutout?.compatSafeInsetBottom() ?: 0)
        else
            displayCutout?.compatSafeInsetBottom() ?: 0
    var isStopped = true
        private set
    var isFirstCreate = true
        private set
    lateinit var baseBind: layout
        private set

    private val config by lazy { initConfig() }
    private val windowInsetsControllerCompat by lazy {
        WindowInsetsControllerCompat(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
    private val stateBarBackground by lazy { findViewById<View?>(android.R.id.statusBarBackground) }
    private val bothABAndSBB by lazy { BothABAndSBB() }

    private var isFirstWindowForce = true
    private var layoutFixed = false
    private var displayCutout: DisplayCutout? = null
    private var isNavigationBarOnBottom = true

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) isFirstCreate = false

        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setFullscreen(Config.FULLSCREEN in config)
        setHideActionBar(Config.HIDE_ACTION_BAR in config)

        val abc = if (Config.TRANSPARENT_ACTION_BAR in config) Color.TRANSPARENT else getColor(R.color.actionBarHalfTransparent)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setBackgroundDrawable(ColorDrawable(abc))
        }

        window.statusBarColor = abc

        baseBind = DataBindingUtil.setContentView(this, initLayout())

        savedInstanceState?.let { onRestoreInstanceState2(it) }

        findViews()
        initView()
        initData()
        TipUtil.registerCoordinatorLayout(this, initRegisterCoordinatorLayout())
    }

    override fun onStart() {
        super.onStart()
        isStopped = false
    }

    override fun onResume() {
        super.onResume()
        setFullscreen(Config.FULLSCREEN in config)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            val viewHeight = displayMetrics.heightPixels
            val decorViewHeight = window.decorView.height
            isNavigationBarOnBottom = decorViewHeight != viewHeight

            if (displayCutout == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                displayCutout = window.decorView.rootWindowInsets.displayCutout
            }

            if (layoutFixed.not() && Config.FIX_LAYOUT in config) {
                updateLayoutFix()
            }

            if (isFirstWindowForce) {
                isFirstWindowForce = false
                setActionBarUp(false)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        isStopped = true
    }

    override fun onDestroy() {
        super.onDestroy()
        TipUtil.unregisterCoordinatorLayout(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun setFullscreen(v: Boolean) {
        val decorView = window.decorView
        if (v) {
            if (Build.VERSION.SDK_INT <= 29) {
                decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            } else {
                windowInsetsControllerCompat.hide(WindowInsetsCompat.Type.systemBars())
            }
        } else {
            if (Build.VERSION.SDK_INT <= 29) {
                decorView.systemUiVisibility = (decorView.systemUiVisibility
                        and (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY).inv())
            } else {
                windowInsetsControllerCompat.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    fun setHideActionBar(v: Boolean) {
        if (v)
            supportActionBar?.hide()
        else
            supportActionBar?.show()
    }

    fun setActionBarUp(v: Boolean, anima: Boolean = false) {
        val supportActionBar = supportActionBar ?: return
        val e1 = OtherUtils.dp2px(4F).toFloat()
        if (anima) {
            val start = supportActionBar.elevation
            val end = if (v) e1 else 0.01F
            if (start == end) return
            ObjectAnimator.ofFloat(bothABAndSBB, "elevation", start, end).apply {
                duration = 200
            }.start()
        } else {
            if (v)
                bothABAndSBB.setElevation(e1)
            else
                bothABAndSBB.setElevation(0.01F)
        }
    }

    fun updateLayoutFix() {
        layoutFixed = true
        baseBind.root.updatePadding(top = fixTopHeight, bottom = fixBottomHeight)
    }

    abstract fun initConfig(): Set<Config>

    @LayoutRes
    abstract fun initLayout(): Int

    open fun findViews() {}
    abstract fun initView()
    abstract fun initData()
    open fun initRegisterCoordinatorLayout() = null as CoordinatorLayout?
    open fun onRestoreInstanceState2(savedInstanceState: Bundle) {}

    private fun DisplayCutout.compatSafeInsetTop() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) this.safeInsetTop else 0

    private fun DisplayCutout.compatSafeInsetBottom() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) this.safeInsetBottom else 0

    inner class AutoSetActionBarUpListener : NestedScrollView.OnScrollChangeListener {
        override fun onScrollChange(v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
            setActionBarUp(scrollY > 0, true)
        }
    }

    inner class BothABAndSBB {
        private val supportActionBar = this@BaseActivity2.supportActionBar

        @Keep
        fun setElevation(elevation: Float) {
            supportActionBar?.elevation = elevation
            stateBarBackground?.elevation = elevation
        }
    }
}