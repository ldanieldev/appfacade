package com.ldaniel.appfacade.shortcut

import android.content.Context
import android.graphics.BitmapFactory
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.ldaniel.appfacade.R
import com.ldaniel.appfacade.WebAppActivity
import com.ldaniel.appfacade.icon.LetterIcon
import com.ldaniel.appfacade.model.WebAppConfig

object Shortcuts {
    fun pin(context: Context, config: WebAppConfig) {
        val bitmap = config.iconPath?.let { BitmapFactory.decodeFile(it) }
            ?: LetterIcon.render(config.name, 192)
        val shortcut = ShortcutInfoCompat.Builder(context, config.id)
            .setShortLabel(config.name)
            .setIcon(IconCompat.createWithBitmap(bitmap))
            .setIntent(WebAppActivity.launchIntent(context, config.id))
            .build()
        ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
    }

    fun disable(context: Context, id: String) {
        ShortcutManagerCompat.disableShortcuts(
            context, listOf(id), context.getString(R.string.shortcut_removed),
        )
    }
}
