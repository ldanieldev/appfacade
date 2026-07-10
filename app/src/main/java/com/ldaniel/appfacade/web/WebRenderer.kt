package com.ldaniel.appfacade.web

import android.content.Context
import android.view.View

/**
 * Presents a renderer-created popup window (e.g. OAuth login).
 * Host shows [view] (e.g. in a dialog); calls [onUserDismiss] if the user closes it.
 * Returns a close function the renderer invokes when the page closes itself.
 */
typealias PopupPresenter = (view: View, onUserDismiss: () -> Unit) -> (() -> Unit)

/** Rendering seam: WebView today, possibly GeckoView later (spec §4). */
interface WebRenderer {
    fun createView(context: Context): View
    fun loadUrl(url: String)
    fun canGoBack(): Boolean
    fun goBack()
    fun clearHttpCache()
    /** Deletes ALL site data including cookies/logins. Callers must warn the user first. */
    fun deepClean(onDone: () -> Unit)
    fun onPause()
    fun onResume()
    fun onDestroy()
}
