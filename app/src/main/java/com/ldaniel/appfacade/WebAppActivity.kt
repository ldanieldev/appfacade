package com.ldaniel.appfacade

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import com.ldaniel.appfacade.data.AppGraph
import com.ldaniel.appfacade.model.WebAppConfig
import com.ldaniel.appfacade.ui.ManagerActivity
import com.ldaniel.appfacade.web.ErrorView
import com.ldaniel.appfacade.web.Fullscreen
import com.ldaniel.appfacade.web.WebRenderer
import com.ldaniel.appfacade.web.WebViewRenderer
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
        val config = appId?.let { id ->
            runBlocking { AppGraph.configStore(this@WebAppActivity).get(id) }
        }
        if (config == null) {
            Toast.makeText(this, R.string.app_removed, Toast.LENGTH_LONG).show()
            startActivity(Intent(this, ManagerActivity::class.java))
            finish()
            return
        }

        setUp(config)
    }

    private fun setUp(config: WebAppConfig) {
        val root = FrameLayout(this)
        lateinit var errorView: ErrorView

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
        )
        renderer = r

        root.addView(r.createView(this), MATCH_PARENT, MATCH_PARENT)
        errorView = ErrorView(this, config.name) {
            errorView.hide()
            r.loadUrl(config.url)
        }
        root.addView(errorView, MATCH_PARENT, MATCH_PARENT)
        setContentView(root)

        if (config.fullscreen) Fullscreen.apply(window)

        onBackPressedDispatcher.addCallback(this) {
            if (r.canGoBack()) r.goBack() else {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }

        // Task 7 wires LockController here, before loadUrl (no content flash).

        r.loadUrl(config.url)
    }

    override fun onPause() { super.onPause(); renderer?.onPause() }
    override fun onResume() { super.onResume(); renderer?.onResume() }

    override fun onDestroy() {
        // Spec §5.4: auto-clear HTTP cache on close; cookies/site data untouched.
        renderer?.clearHttpCache()
        renderer?.onDestroy()
        renderer = null
        super.onDestroy()
    }
}
