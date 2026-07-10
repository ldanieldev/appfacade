package com.ldaniel.appfacade.web

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.ldaniel.appfacade.R

/** Friendly full-screen "can't reach server" panel with Retry (spec §7). */
class ErrorView(
    context: Context,
    appName: String,
    onRetry: () -> Unit,
) : LinearLayout(context) {

    private val detailView = TextView(context).apply {
        setTextColor(Color.LTGRAY)
        gravity = Gravity.CENTER
        setPadding(48, 16, 48, 32)
    }

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        setBackgroundColor(Color.BLACK)
        visibility = GONE
        addView(TextView(context).apply {
            text = context.getString(R.string.cant_reach, appName)
            setTextColor(Color.WHITE)
            textSize = 20f
            gravity = Gravity.CENTER
        })
        addView(detailView)
        addView(Button(context).apply {
            text = context.getString(R.string.retry)
            setOnClickListener { onRetry() }
        })
    }

    fun show(detail: String) {
        detailView.text = detail
        visibility = VISIBLE
    }

    fun hide() { visibility = GONE }
}
