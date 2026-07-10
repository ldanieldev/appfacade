package com.ldaniel.appfacade.lock

import android.os.Build
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.ldaniel.appfacade.R

/**
 * Lock-on-every-resume (spec §5.2): any loss of foreground re-locks; on return
 * the opaque overlay is up before content and the prompt fires automatically.
 */
class LockController(
    private val activity: FragmentActivity,
    private val appName: String,
    root: FrameLayout,
) : DefaultLifecycleObserver {

    private val overlay = LockOverlayView(activity) { promptUnlock() }
    private var locked = true

    init {
        if (Build.VERSION.SDK_INT >= 33) activity.setRecentsScreenshotEnabled(false)
        root.addView(overlay, MATCH_PARENT, MATCH_PARENT)
        activity.lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        if (locked) {
            overlay.visibility = View.VISIBLE
            promptUnlock()
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        locked = true
        overlay.visibility = View.VISIBLE
    }

    override fun onPause(owner: LifecycleOwner) {
        // Belt and suspenders below API 33: setRecentsScreenshotEnabled(false) is
        // API 33+ only, so on Android 12/12L the Recents thumbnail can still
        // capture the last rendered frame. FLAG_SECURE blocks that snapshot too.
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    private fun promptUnlock() {
        Authenticator.authenticate(
            activity,
            activity.getString(R.string.unlock_title, appName),
            onSuccess = {
                // Guard against a callback landing after the activity left the
                // foreground (e.g. user backgrounds the app mid-prompt).
                if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    locked = false
                    overlay.visibility = View.GONE
                    activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            },
            // On cancel/error the overlay stays; its Unlock button re-prompts.
        )
    }
}
