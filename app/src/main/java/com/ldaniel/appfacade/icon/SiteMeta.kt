package com.ldaniel.appfacade.icon

import java.net.URI

object SiteMeta {
    private val titleRegex =
        Regex("<title[^>]*>(.*?)</title>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
    private val linkTagRegex = Regex("<link[^>]+>", RegexOption.IGNORE_CASE)
    private val relIconRegex = Regex("rel=[\"'][^\"']*icon[^\"']*[\"']", RegexOption.IGNORE_CASE)
    private val hrefRegex = Regex("href=[\"']([^\"']+)[\"']", RegexOption.IGNORE_CASE)

    fun parseTitle(html: String): String? =
        titleRegex.find(html)?.groupValues?.get(1)?.trim()?.takeIf { it.isNotEmpty() }

    fun parseIconUrl(html: String, baseUrl: String): String {
        val href = linkTagRegex.findAll(html)
            .filter { relIconRegex.containsMatchIn(it.value) }
            .mapNotNull { hrefRegex.find(it.value)?.groupValues?.get(1) }
            .firstOrNull()
        return URI(baseUrl).resolve(href ?: "/favicon.ico").toString()
    }
}
