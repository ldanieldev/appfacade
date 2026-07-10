package com.ldaniel.appfacade.lock

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.ldaniel.appfacade.R

/** Opaque cover shown while locked — content must never flash through. */
class LockOverlayView(context: Context, onUnlockClick: () -> Unit) : LinearLayout(context) {
    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        setBackgroundColor(Color.BLACK)
        isClickable = true // swallow touches aimed at the content below
        addView(TextView(context).apply {
            text = context.getString(R.string.locked)
            setTextColor(Color.WHITE)
            textSize = 22f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 32)
        })
        addView(Button(context).apply {
            text = context.getString(R.string.unlock)
            setOnClickListener { onUnlockClick() }
        })
    }
}
