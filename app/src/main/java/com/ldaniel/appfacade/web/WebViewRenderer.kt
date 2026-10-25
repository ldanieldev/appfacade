package com.ldaniel.appfacade.web

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.GeolocationPermissions
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebStorageCompat
import androidx.webkit.WebViewFeature
import com.ldaniel.appfacade.model.SitePermission

class WebViewRenderer(
    private val onMainFrameError: (String) -> Unit,
    private val onShowFileChooser: ((ValueCallback<Array<Uri>>, WebChromeClient.FileChooserParams) -> Boolean)? = null,
    private val onShowPopup: PopupPresenter? = null,
    private val onPull: ((progress: Float, refreshing: Boolean) -> Unit)? = null,
    private val onThemeColor: ((Int?) -> Unit)? = null,
    private val isAllowed: ((SitePermission, origin: String?) -> Boolean)? = null,
) : WebRenderer {

    private var webView: WebView? = null
    private var refreshing = false

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
            setGeolocationEnabled(true)
        }
        wv.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                endPull()
                if (onPull != null) view.evaluateJavascript(PULL_PROBE_JS, null)
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
                    endPull()
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

            override fun onPermissionRequest(request: PermissionRequest) {
                val check = isAllowed
                val origin = request.origin?.toString()
                val kept = if (check == null) emptyList() else request.resources.filter { r ->
                    SitePermission.fromWebResource(r)?.let { check(it, origin) } == true
                }
                if (kept.isEmpty()) request.deny() else request.grant(kept.toTypedArray())
            }

            override fun onGeolocationPermissionsShowPrompt(
                origin: String,
                callback: GeolocationPermissions.Callback,
            ) {
                callback.invoke(origin, isAllowed?.invoke(SitePermission.LOCATION, origin) == true, false)
            }

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
        if (onPull != null) attachPull(wv, onPull)
        webView = wv
        return wv
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun attachPull(wv: WebView, onPull: (Float, Boolean) -> Unit) {
        val gesture = PullGesture(
            triggerPx = 120f * wv.resources.displayMetrics.density,
            slopPx = ViewConfiguration.get(wv.context).scaledTouchSlop.toFloat(),
        )
        wv.addJavascriptInterface(object {
            @JavascriptInterface
            fun atTop(v: Boolean) { wv.post { gesture.contentAtTop(v) } }
        }, "AppFacadePull")
        val report = { p: Float -> if (!refreshing) onPull(p, false) }
        wv.setOnTouchListener { _, e ->
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> gesture.down(e.y)
                MotionEvent.ACTION_MOVE -> report(gesture.move(e.y, !wv.canScrollVertically(-1)))
                MotionEvent.ACTION_UP -> if (gesture.up() && !refreshing) {
                    refreshing = true
                    onPull(1f, true)
                    wv.reload()
                } else report(0f)
                // A second finger means pinch-zoom; end the pull so a two-finger drag can't reload.
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_POINTER_DOWN -> {
                    gesture.cancel()
                    report(0f)
                }
            }
            false
        }
    }

    private fun endPull() {
        refreshing = false
        onPull?.invoke(0f, false)
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
    }
}

private const val PULL_PROBE_JS =
    "(function(){if(window.__afPull)return;window.__afPull=1;document.addEventListener('touchstart',function(e){var p=e.composedPath&&e.composedPath(),el=p&&p.length?p[0]:e.target,top=true;while(el&&el!==document.documentElement){if(el.nodeType===1){var o=getComputedStyle(el).overflowY;if((o==='auto'||o==='scroll'||o==='overlay')&&el.scrollHeight>el.clientHeight&&(getComputedStyle(el).flexDirection==='column-reverse'?el.scrollTop>-(el.scrollHeight-el.clientHeight)+1:el.scrollTop>0)){top=false;break;}}el=el.parentNode||el.host;}var r=document.scrollingElement||document.documentElement;if(top&&r&&r.scrollTop>0)top=false;AppFacadePull.atTop(top);},{passive:true,capture:true});})()"
