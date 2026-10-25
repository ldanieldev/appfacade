package com.ldaniel.appfacade.web

class PullGesture(private val triggerPx: Float, private val slopPx: Float) {
    private var downY = 0f
    private var contentAtTop: Boolean? = null
    private var wentUp = false
    private var progress = 0f
    private var cancelled = false

    fun down(y: Float) {
        reset()
        cancelled = false
        downY = y
    }

    fun contentAtTop(atTop: Boolean) { if (!cancelled) contentAtTop = atTop }

    fun move(y: Float, viewAtTop: Boolean): Float {
        if (downY - y > slopPx) wentUp = true
        val eligible = contentAtTop == true && viewAtTop && !wentUp
        progress = if (eligible) ((y - downY) / triggerPx).coerceIn(0f, 1f) else 0f
        return progress
    }

    fun up(): Boolean {
        val fire = contentAtTop == true && !wentUp && progress >= 1f
        reset()
        return fire
    }

    fun cancel() {
        reset()
        cancelled = true
    }

    private fun reset() {
        contentAtTop = null
        wentUp = false
        progress = 0f
    }
}
