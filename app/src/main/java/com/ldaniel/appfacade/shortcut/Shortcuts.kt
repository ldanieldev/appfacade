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
        val shortcut = ShortcutInfoCompat.Builder(context, config.id)
            .setShortLabel(config.name)
            .setIcon(shortcutIcon(config))
            .setIntent(WebAppActivity.launchIntent(context, config.id))
            .build()
        ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
    }

    fun disable(context: Context, id: String) {
        ShortcutManagerCompat.disableShortcuts(
            context, listOf(id), context.getString(R.string.shortcut_removed),
        )
    }

    private fun shortcutIcon(config: WebAppConfig): IconCompat {
        val favicon = config.iconPath?.let { decodeBounded(it, ICON_SIZE) }
        val bitmap = favicon?.let { padForAdaptive(it) } ?: LetterIcon.render(config.name, ICON_SIZE)
        return IconCompat.createWithAdaptiveBitmap(bitmap)
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

    /** Center the favicon in the adaptive safe zone on a white plate. */
    private fun padForAdaptive(src: Bitmap): Bitmap {
        val out = Bitmap.createBitmap(ICON_SIZE, ICON_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawColor(Color.WHITE)
        val inner = (ICON_SIZE * SAFE_ZONE).toInt()
        val scaled = Bitmap.createScaledBitmap(src, inner, inner, true)
        val offset = (ICON_SIZE - inner) / 2f
        canvas.drawBitmap(scaled, offset, offset, null)
        return out
    }
}
