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

    fun matchesAppOrigin(appUrl: String, origin: String?): Boolean {
        if (origin == null) return false
        val app = try { URI(appUrl) } catch (e: URISyntaxException) { return false }
        val other = try { URI(origin) } catch (e: URISyntaxException) { return false }
        val appScheme = app.scheme?.lowercase() ?: return false
        val otherScheme = other.scheme?.lowercase() ?: return false
        val appHost = app.host?.lowercase() ?: return false
        val otherHost = other.host?.lowercase() ?: return false
        if (appHost != otherHost) return false
        val appPort = effectivePort(appScheme, app.port) ?: return false
        val otherPort = effectivePort(otherScheme, other.port) ?: return false
        if (appScheme == otherScheme) return appPort == otherPort
        val upgrade = appScheme == "http" && otherScheme == "https"
        val portsAligned = (app.port == -1 && other.port == -1) || (app.port != -1 && app.port == other.port)
        return upgrade && portsAligned
    }

    private fun effectivePort(scheme: String, port: Int): Int? = when {
        port != -1 -> port
        scheme == "http" -> 80
        scheme == "https" -> 443
        else -> null
    }
}
