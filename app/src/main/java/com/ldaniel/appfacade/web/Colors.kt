package com.ldaniel.appfacade.web

object Colors {
    fun isLight(argb: Int): Boolean {
        val r = (argb shr 16 and 0xFF) / 255.0
        val g = (argb shr 8 and 0xFF) / 255.0
        val b = (argb and 0xFF) / 255.0
        return 0.299 * r + 0.587 * g + 0.114 * b > 0.5
    }
}
