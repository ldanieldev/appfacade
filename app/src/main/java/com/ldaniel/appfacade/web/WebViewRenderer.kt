package com.ldaniel.appfacade.web

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.view.View
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.webkit.WebStorageCompat
import androidx.webkit.WebViewFeature

class WebViewRenderer(
    private val onMainFrameError: (String) -> Unit,
    private val onShowFileChooser: ((ValueCallback<Array<Uri>>, WebChromeClient.FileChooserParams) -> Boolean)? = null,
    private val onShowPopup: PopupPresenter? = null,
    private val pullToRefresh: Boolean = true,
    private val onThemeColor: ((Int?) -> Unit)? = null,
) : WebRenderer {

    private var webView: WebView? = null
    private var swipe: SwipeRefreshLayout? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun createView(context: Context): View {
        val wv = WebView(context)
        wv.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_YES
        wv.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            setSupportMultipleWindows(true)
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
        }
        wv.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                swipe?.isRefreshing = false
                if (onThemeColor != null) {
                    view.evaluateJavascript(
                        "(function(){var m=document.querySelector('meta[name=\"theme-color\"]');return m?m.content:'';})()"
                    ) { raw ->
                        val value = raw?.trim('"')?.takeIf { it.isNotBlank() && it != "null" }
                        val color = value?.let { runCatching { Color.parseColor(it) }.getOrNull() }
                        onThemeColor.invoke(color)
                    }
                }
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError,
            ) {
                if (request.isForMainFrame) {
                    swipe?.isRefreshing = false
                    onMainFrameError(error.description.toString())
                }
            }
        }
        wv.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                view: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams,
            ): Boolean = onShowFileChooser?.invoke(filePathCallback, fileChooserParams) ?: false

            @SuppressLint("SetJavaScriptEnabled")
            override fun onCreateWindow(
                view: WebView,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: android.os.Message,
            ): Boolean {
                val present = onShowPopup ?: return false
                val popup = WebView(view.context)
                popup.settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                }
                popup.webViewClient = WebViewClient()
                var destroyed = false
                val destroyOnce = { if (!destroyed) { destroyed = true; popup.destroy() } }
                lateinit var close: () -> Unit
                popup.webChromeClient = object : WebChromeClient() {
                    override fun onCloseWindow(window: WebView) {
                        close()               // dismiss the host dialog
                        destroyOnce()
                    }
                }
                close = present(popup) { destroyOnce() }   // user dismissed -> destroy
                (resultMsg.obj as WebView.WebViewTransport).webView = popup
                resultMsg.sendToTarget()
                return true
            }
        }
        webView = wv
        return if (!pullToRefresh) wv else SwipeRefreshLayout(context).also { layout ->
            layout.addView(wv)
            layout.setOnRefreshListener { wv.reload() }
            swipe = layout
        }
    }

    override fun loadUrl(url: String) { webView?.loadUrl(url) }
    override fun canGoBack(): Boolean = webView?.canGoBack() == true
    override fun goBack() { webView?.goBack() }
    override fun clearHttpCache() { webView?.clearCache(true) }

    override fun deepClean(onDone: () -> Unit) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.DELETE_BROWSING_DATA)) {
            WebStorageCompat.deleteBrowsingData(WebStorage.getInstance()) { onDone() }
        } else {
            WebStorage.getInstance().deleteAllData()
            webView?.clearCache(true)
            CookieManager.getInstance().removeAllCookies { onDone() }
        }
    }

    override fun onPause() { webView?.onPause() }
    override fun onResume() { webView?.onResume() }
    override fun onDestroy() {
        webView?.destroy()
        webView = null
        swipe = null
    }
}
