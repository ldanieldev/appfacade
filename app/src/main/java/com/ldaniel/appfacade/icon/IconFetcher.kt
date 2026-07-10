package com.ldaniel.appfacade.icon

import android.content.Context
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class FetchedMeta(val title: String?, val iconPath: String?)

object IconFetcher {
    /** Best-effort favicon + title fetch. Blocking: call on Dispatchers.IO. Never throws. */
    fun fetch(context: Context, id: String, pageUrl: String): FetchedMeta {
        return try {
            val html = httpGet(pageUrl)?.toString(Charsets.UTF_8)
            val title = html?.let { SiteMeta.parseTitle(it) }
            val iconBytes = httpGet(SiteMeta.parseIconUrl(html ?: "", pageUrl))
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
