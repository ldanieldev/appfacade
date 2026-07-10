package com.ldaniel.appfacade.icon

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

object LetterIcon {
    private val palette = listOf(
        0xFF3949AB, 0xFF00897B, 0xFFD81B60, 0xFF6D4C41, 0xFF546E7A, 0xFFF4511E,
    ).map { it.toInt() }

    fun letterFor(name: String): String =
        name.firstOrNull { it.isLetterOrDigit() }?.uppercase() ?: "?"

    fun colorFor(name: String): Int = palette[Math.floorMod(name.hashCode(), palette.size)]

    /** Device-only (android.graphics): not covered by JVM unit tests. */
    fun render(name: String, sizePx: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(colorFor(name))
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = sizePx * 0.5f
            textAlign = Paint.Align.CENTER
        }
        val baseline = sizePx / 2f - (paint.descent() + paint.ascent()) / 2f
        canvas.drawText(letterFor(name), sizePx / 2f, baseline, paint)
        return bitmap
    }
}
