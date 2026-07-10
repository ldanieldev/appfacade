package com.ldaniel.appfacade.model

import java.net.URI
import java.net.URISyntaxException

object Urls {
    /** Returns a normalized absolute http(s) URL, or null if [input] is not one. */
    fun normalize(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null
        val candidate = if ("://" in trimmed) trimmed else "http://$trimmed"
        val uri = try {
            URI(candidate)
        } catch (e: URISyntaxException) {
            return null
        }
        val scheme = uri.scheme?.lowercase()
        if (scheme != "http" && scheme != "https") return null
        if (uri.host.isNullOrEmpty()) return null
        return uri.toString()
    }
}
