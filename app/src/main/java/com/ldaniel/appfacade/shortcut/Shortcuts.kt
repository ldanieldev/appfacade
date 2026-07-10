package com.ldaniel.appfacade.shortcut

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.ldaniel.appfacade.R
import com.ldaniel.appfacade.WebAppActivity
import com.ldaniel.appfacade.icon.LetterIcon
import com.ldaniel.appfacade.model.WebAppConfig

object Shortcuts {
    private const val ICON_SIZE = 256
    // Adaptive icons mask to the center ~61% (66/108dp); keep favicon content inside it.
    private const val SAFE_ZONE = 0.6f

    fun pin(context: Context, config: WebAppConfig) {
        ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo(context, config), null)
    }

    fun disable(context: Context, id: String) {
        ShortcutManagerCompat.disableShortcuts(
            context, listOf(id), context.getString(R.string.shortcut_removed),
        )
    }

    /** Refreshes an already-pinned shortcut's label/icon in place. No-op if never pinned. */
    fun update(context: Context, config: WebAppConfig) {
        ShortcutManagerCompat.updateShortcuts(context, listOf(shortcutInfo(context, config)))
    }

    private fun shortcutInfo(context: Context, config: WebAppConfig): ShortcutInfoCompat =
        ShortcutInfoCompat.Builder(context, config.id)
            .setShortLabel(config.name)
            .setIcon(shortcutIcon(config))
            .setIntent(WebAppActivity.launchIntent(context, config.id))
            .build()

    private fun shortcutIcon(config: WebAppConfig): IconCompat {
        val favicon = config.iconPath?.let { decodeBounded(it, ICON_SIZE) }
        val bitmap = favicon?.let { styledIcon(it, config.iconStyle) } ?: LetterIcon.render(config.name, ICON_SIZE)
        return IconCompat.createWithAdaptiveBitmap(bitmap)
    }

    private fun styledIcon(src: Bitmap, style: String): Bitmap = when (style) {
        "full" -> Bitmap.createScaledBitmap(src, ICON_SIZE, ICON_SIZE, true)
        "white" -> padOnPlate(src, Color.WHITE)
        "black" -> padOnPlate(src, Color.BLACK)
        else -> padOnPlate(src, plateColor(src))
    }

    /** Decode with inSampleSize so a huge favicon can't allocate tens of MB. */
    private fun decodeBounded(path: String, target: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= target && bounds.outHeight / (sample * 2) >= target) sample *= 2
        return BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
    }

    /** Center the favicon in the adaptive safe zone on a plate of the given background color. */
    private fun padOnPlate(src: Bitmap, bg: Int): Bitmap {
        val out = Bitmap.createBitmap(ICON_SIZE, ICON_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawColor(bg)
        val inner = (ICON_SIZE * SAFE_ZONE).toInt()
        val scaled = Bitmap.createScaledBitmap(src, inner, inner, true)
        val offset = (ICON_SIZE - inner) / 2f
        canvas.drawBitmap(scaled, offset, offset, null)
        return out
    }

    /** Plate color that blends with the favicon: opaque corners first, then overall tint. */
    private fun plateColor(src: Bitmap): Int {
        val corners = listOf(
            src.getPixel(0, 0),
            src.getPixel(src.width - 1, 0),
            src.getPixel(0, src.height - 1),
            src.getPixel(src.width - 1, src.height - 1),
        )
        averageOpaque(corners)?.let { return it }
        // Transparent corners (floating logo): tint from the artwork itself.
        val thumb = Bitmap.createScaledBitmap(src, 8, 8, true)
        val pixels = IntArray(64).also { thumb.getPixels(it, 0, 8, 0, 0, 8, 8) }
        return averageOpaque(pixels.toList()) ?: Color.WHITE
    }

    private fun averageOpaque(pixels: List<Int>): Int? {
        val opaque = pixels.filter { Color.alpha(it) > 200 }
        if (opaque.isEmpty()) return null
        return Color.rgb(
            opaque.map { Color.red(it) }.average().toInt(),
            opaque.map { Color.green(it) }.average().toInt(),
            opaque.map { Color.blue(it) }.average().toInt(),
        )
    }
}
