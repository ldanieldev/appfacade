package com.ldaniel.appfacade.web

import android.annotation.SuppressLint
import android.content.Context
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
import androidx.webkit.WebStorageCompat
import androidx.webkit.WebViewFeature

class WebViewRenderer(
    private val onMainFrameError: (String) -> Unit,
    private val onShowFileChooser: ((ValueCallback<Array<Uri>>, WebChromeClient.FileChooserParams) -> Boolean)? = null,
) : WebRenderer {

    private var webView: WebView? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun createView(context: Context): View = WebView(context).also { wv ->
        wv.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_YES
        wv.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
        }
        wv.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError,
            ) {
                if (request.isForMainFrame) onMainFrameError(error.description.toString())
            }
        }
        wv.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                view: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams,
            ): Boolean = onShowFileChooser?.invoke(filePathCallback, fileChooserParams) ?: false
        }
        webView = wv
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
