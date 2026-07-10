package com.ldaniel.appfacade.lock

import android.os.Build
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
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

    private fun promptUnlock() {
        Authenticator.authenticate(
            activity,
            activity.getString(R.string.unlock_title, appName),
            onSuccess = {
                locked = false
                overlay.visibility = View.GONE
            },
            // On cancel/error the overlay stays; its Unlock button re-prompts.
        )
    }
}
