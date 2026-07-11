package com.ldaniel.appfacade.web

import android.view.Window
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

object Fullscreen {
    /** Immersive: bars hidden, swipe shows them transiently (spec §5.3). */
    fun apply(window: Window) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setCutoutMode(window, WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    /** Explicitly restore normal (non-fullscreen) window state. */
    fun clear(window: Window) {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setCutoutMode(window, WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT)
        WindowInsetsControllerCompat(window, window.decorView)
            .show(WindowInsetsCompat.Type.systemBars())
    }

    private fun setCutoutMode(window: Window, mode: Int) {
        // Reassignment required: mutating window.attributes in place is not applied.
        window.attributes = window.attributes.also { it.layoutInDisplayCutoutMode = mode }
    }
}
