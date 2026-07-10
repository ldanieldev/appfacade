package com.ldaniel.appfacade.icon

import android.content.Context
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class FetchedMeta(val title: String?, val iconPath: String?)

object IconFetcher {
    /**
     * Best-effort icon + title fetch. Blocking: call on Dispatchers.IO. Never throws.
     *
     * Icon byte resolution order (first non-null wins):
     * 1. [iconSource] set and an http(s) URL -> fetched directly
     * 2. [iconSource] set otherwise (a slug/name) -> selfh.st CDN PNG for that slug
     * 3. [name] non-null/non-blank -> selfh.st CDN PNG for that name
     * 4. favicon parsed from the page HTML
     *
     * The page HTML is only fetched when needed: for the title (when [name] is null)
     * or as the last-resort favicon source (when steps 1-3 produced no icon bytes).
     */
    fun fetch(context: Context, id: String, pageUrl: String, name: String?, iconSource: String?): FetchedMeta {
        return try {
            var iconBytes: ByteArray? = when {
                iconSource != null && (iconSource.startsWith("http://") || iconSource.startsWith("https://")) ->
                    httpGet(iconSource)
                iconSource != null -> httpGet(SelfhstIcons.pngUrl(iconSource))
                else -> null
            }
            if (iconBytes == null && !name.isNullOrBlank()) {
                iconBytes = httpGet(SelfhstIcons.pngUrl(name))
            }

            var title: String? = null
            if (name == null || iconBytes == null) {
                val html = httpGet(pageUrl)?.toString(Charsets.UTF_8)
                title = html?.let { SiteMeta.parseTitle(it) }
                if (iconBytes == null) {
                    iconBytes = httpGet(SiteMeta.parseIconUrl(html ?: "", pageUrl))
                }
            }

            val iconPath = iconBytes?.let { bytes ->
                val file = File(context.filesDir, "icons/$id.img")
                file.parentFile?.mkdirs()
                file.writeBytes(bytes)
                file.absolutePath
            }
            FetchedMeta(title, iconPath)
        } catch (e: Exception) {
            FetchedMeta(null, null)
        }
    }

    private fun httpGet(url: String): ByteArray? {
        var conn: HttpURLConnection? = null
        return try {
            conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            if (conn.responseCode != 200) return null
            conn.inputStream.use { it.readBytes() }
        } catch (e: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }
}
