package com.ldaniel.appfacade

import android.app.ActivityManager
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.ldaniel.appfacade.data.AppGraph
import com.ldaniel.appfacade.lock.LockController
import com.ldaniel.appfacade.model.PermissionPolicy
import com.ldaniel.appfacade.model.WebAppConfig
import com.ldaniel.appfacade.model.WebOrientation
import com.ldaniel.appfacade.ui.ManagerActivity
import com.ldaniel.appfacade.web.Colors
import com.ldaniel.appfacade.web.ErrorView
import com.ldaniel.appfacade.web.Fullscreen
import com.ldaniel.appfacade.web.SitePermissions
import com.ldaniel.appfacade.web.WebRenderer
import com.ldaniel.appfacade.web.WebViewRenderer
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class WebAppActivity : FragmentActivity() {

    companion object {
        const val EXTRA_APP_ID = "app_id"

        fun launchIntent(context: Context, appId: String): Intent =
            Intent(context, WebAppActivity::class.java)
                .setAction(Intent.ACTION_VIEW)
                .setData("appfacade://app/$appId".toUri())
                .putExtra(EXTRA_APP_ID, appId)
    }

    private var renderer: WebRenderer? = null
    private var config: WebAppConfig? = null
    /** Force-closes the currently open popup dialog (and destroys its WebView), if any. */
    private var dismissPopup: (() -> Unit)? = null

    private var fileChooserCallback: ValueCallback<Array<Uri>>? = null
    private val fileChooser =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            fileChooserCallback?.onReceiveValue(
                WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
            )
            fileChooserCallback = null
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appId = intent.getStringExtra(EXTRA_APP_ID) ?: intent.data?.lastPathSegment
        // Single small local file read, once per task creation.
        val loaded = appId?.let { id ->
            runBlocking { AppGraph.configStore(this@WebAppActivity).get(id) }
        }
        if (loaded == null) {
            Toast.makeText(this, R.string.app_removed, Toast.LENGTH_LONG).show()
            startActivity(Intent(this, ManagerActivity::class.java))
            finish()
            return
        }

        config = loaded
        setUp(loaded)
    }

    override fun onStart() {
        super.onStart()
        // The task can outlive the manager's edits (documentLaunchMode="intoExisting"
        // keeps this task around indefinitely), so re-check the persisted config on
        // every return to foreground and pick up lock/url/fullscreen changes.
        val current = config ?: return
        lifecycleScope.launch {
            val fresh = AppGraph.configStore(this@WebAppActivity).get(current.id)
            when {
                fresh == null -> {
                    Toast.makeText(this@WebAppActivity, R.string.app_removed, Toast.LENGTH_LONG).show()
                    finish()
                }
                fresh != current -> recreate() // picks up lock/url/fullscreen changes on next onCreate
            }
        }
    }

    private fun setUp(config: WebAppConfig) {
        val root = FrameLayout(this)
        lateinit var errorView: ErrorView
        requestedOrientation = WebOrientation.toRequested(config.orientation)
        // Builder only accepts resource icons; this deprecated ctor is the only public way to pass a bitmap.
        @Suppress("DEPRECATION")
        setTaskDescription(
            ActivityManager.TaskDescription(config.name, config.iconPath?.let { BitmapFactory.decodeFile(it) })
        )
        val avoidCutout = config.fullscreen && config.avoidCutout
        val pullIndicator = if (config.pullToRefresh) ProgressBar(this).apply {
            isIndeterminate = true
            visibility = View.GONE
        } else null
        val pullTravel = 48f * resources.displayMetrics.density

        val r = WebViewRenderer(
            onMainFrameError = { detail ->
                runOnUiThread { if (!isDestroyed && !isFinishing) errorView.show(detail) }
            },
            onShowFileChooser = { callback, params ->
                fileChooserCallback?.onReceiveValue(null)
                fileChooserCallback = callback
                fileChooser.launch(params.createIntent())
                true
            },
            onShowPopup = { view, onUserDismiss ->
                if (isDestroyed || isFinishing) {
                    onUserDismiss()          // destroy the never-shown popup
                    val noop: () -> Unit = {}
                    noop                     // no-op close
                } else {
                    dismissPopup?.invoke()   // at most one popup at a time
                    val dialog = Dialog(this, android.R.style.Theme_DeviceDefault_NoActionBar)
                    dialog.setContentView(view)
                    dialog.window?.setLayout(MATCH_PARENT, MATCH_PARENT)
                    dialog.setOnCancelListener { onUserDismiss() }
                    dialog.setOnDismissListener { dismissPopup = null }
                    dialog.show()
                    val close = { dialog.setOnCancelListener(null); dialog.dismiss() }
                    dismissPopup = { close(); onUserDismiss() }
                    close
                }
            },
            onPull = pullIndicator?.let { bar ->
                { progress, refreshing ->
                    bar.visibility = if (progress > 0f || refreshing) View.VISIBLE else View.GONE
                    bar.alpha = if (refreshing) 1f else progress
                    bar.translationY = (if (refreshing) 1f else progress) * pullTravel
                }
            },
            onThemeColor = if (avoidCutout || !config.fullscreen) { color ->
                runOnUiThread {
                    if (!isDestroyed && !isFinishing) {
                        val bg = color ?: Color.BLACK
                        root.setBackgroundColor(bg)
                        if (!config.fullscreen) setBarIcons(bg)
                    }
                }
            } else null,
            isAllowed = { p, origin ->
                PermissionPolicy.isAllowed(p, config, SitePermissions.held(this), origin)
            },
        )
        renderer = r

        val contentView = r.createView(this)
        root.addView(contentView, MATCH_PARENT, MATCH_PARENT)
        pullIndicator?.let {
            root.addView(it, FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, Gravity.TOP or Gravity.CENTER_HORIZONTAL))
        }
        errorView = ErrorView(this, config.name) {
            errorView.hide()
            r.loadUrl(config.url)
        }
        root.addView(errorView, MATCH_PARENT, MATCH_PARENT)
        setContentView(root)

        if (config.fullscreen) Fullscreen.apply(window) else Fullscreen.clear(window)

        if (avoidCutout) {
            root.setBackgroundColor(Color.BLACK)
            ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
                val top = insets.getInsets(WindowInsetsCompat.Type.displayCutout()).top
                (contentView.layoutParams as FrameLayout.LayoutParams).topMargin = top
                contentView.requestLayout()
                pullIndicator?.let { setTopMargin(it, top) }
                insets
            }
        }

        if (!config.fullscreen) {
            root.setBackgroundColor(Color.BLACK)
            setBarIcons(Color.BLACK)
            ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
                val bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() or
                        WindowInsetsCompat.Type.displayCutout() or
                        WindowInsetsCompat.Type.ime()
                )
                (contentView.layoutParams as FrameLayout.LayoutParams).setMargins(
                    bars.left, bars.top, bars.right, bars.bottom
                )
                contentView.requestLayout()
                pullIndicator?.let { setTopMargin(it, bars.top) }
                insets
            }
        }

        onBackPressedDispatcher.addCallback(this) {
            if (r.canGoBack()) r.goBack() else {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }

        if (config.requireUnlock) LockController(this, config.name, root)

        r.loadUrl(config.url)
    }

    private fun setTopMargin(view: View, top: Int) {
        (view.layoutParams as FrameLayout.LayoutParams).topMargin = top
        view.requestLayout()
    }

    private fun setBarIcons(background: Int) {
        WindowInsetsControllerCompat(window, window.decorView).apply {
            val light = Colors.isLight(background)
            isAppearanceLightStatusBars = light
            isAppearanceLightNavigationBars = light
        }
    }

    override fun onPause() { super.onPause(); renderer?.onPause() }
    override fun onResume() { super.onResume(); renderer?.onResume() }

    override fun onStop() {
        super.onStop()
        // A Dialog is its own window above the lock overlay: never leave a popup
        // floating over the lock screen. Unlocked apps keep their popup across app
        // switches (e.g. grabbing a 2FA code) — that's deliberate.
        if (config?.requireUnlock == true) dismissPopup?.invoke()
    }

    override fun onDestroy() {
        dismissPopup?.invoke()
        // Spec §5.4: auto-clear HTTP cache on close; cookies/site data untouched.
        renderer?.clearHttpCache()
        renderer?.onDestroy()
        renderer = null
        super.onDestroy()
    }
}
