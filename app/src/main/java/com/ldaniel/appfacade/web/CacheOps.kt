package com.ldaniel.appfacade.web

import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import androidx.webkit.WebStorageCompat
import androidx.webkit.WebViewFeature

object CacheOps {
    /** Clears the app-wide WebView HTTP cache. Cookies and site storage untouched. */
    fun clearHttpCache(context: Context) {
        val throwaway = WebView(context)
        try { throwaway.clearCache(true) } finally { throwaway.destroy() }
    }

    /** Deletes ALL site data including cookies. Callers must warn the user first. */
    fun deepClean(context: Context, onDone: () -> Unit) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.DELETE_BROWSING_DATA)) {
            WebStorageCompat.deleteBrowsingData(WebStorage.getInstance()) { onDone() }
        } else {
            WebStorage.getInstance().deleteAllData()
            clearHttpCache(context)
            CookieManager.getInstance().removeAllCookies { onDone() }
        }
    }
}
