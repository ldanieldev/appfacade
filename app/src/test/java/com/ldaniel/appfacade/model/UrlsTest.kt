package com.ldaniel.appfacade.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlsTest {
    @Test fun `keeps valid http url`() =
        assertEquals("http://192.168.0.42:4567/", Urls.normalize("http://192.168.0.42:4567/"))

    @Test fun `keeps https url`() =
        assertEquals("https://example.com/path", Urls.normalize("https://example.com/path"))

    @Test fun `adds http scheme when missing`() =
        assertEquals("http://192.168.0.42:4567", Urls.normalize("192.168.0.42:4567"))

    @Test fun `trims whitespace`() =
        assertEquals("http://example.com", Urls.normalize("  http://example.com  "))

    @Test fun `rejects non-http scheme`() = assertNull(Urls.normalize("ftp://example.com"))

    @Test fun `accepts uppercase scheme`() = assertNotNull(Urls.normalize("HTTP://example.com"))

    @Test fun `rejects uppercase non-http scheme`() = assertNull(Urls.normalize("FTP://example.com"))

    @Test fun `rejects garbage`() = assertNull(Urls.normalize("not a url"))

    @Test fun `rejects empty`() = assertNull(Urls.normalize("   "))

    @Test fun `matchesAppOrigin same https origin`() {
        assertTrue(Urls.matchesAppOrigin("https://fit.example.com/app", "https://fit.example.com"))
        assertTrue(Urls.matchesAppOrigin("https://fit.example.com/app", "https://fit.example.com/"))
    }

    @Test fun `matchesAppOrigin is case-insensitive on host`() =
        assertTrue(Urls.matchesAppOrigin("https://Fit.Example.com/", "https://fit.example.com"))

    @Test fun `matchesAppOrigin allows http to https upgrade on default ports`() =
        assertTrue(Urls.matchesAppOrigin("http://fit.example.com/", "https://fit.example.com"))

    @Test fun `matchesAppOrigin allows http to https upgrade with same explicit port`() =
        assertTrue(Urls.matchesAppOrigin("http://h:8443/", "https://h:8443"))

    @Test fun `matchesAppOrigin rejects https to http downgrade`() =
        assertFalse(Urls.matchesAppOrigin("https://h/", "http://h"))

    @Test fun `matchesAppOrigin rejects other host and www variant`() {
        assertFalse(Urls.matchesAppOrigin("https://example.com/", "https://www.example.com"))
        assertFalse(Urls.matchesAppOrigin("https://a.com/", "https://b.com"))
    }

    @Test fun `matchesAppOrigin rejects different port`() =
        assertFalse(Urls.matchesAppOrigin("https://h:8443/", "https://h:9443"))

    @Test fun `matchesAppOrigin rejects missing or malformed origin`() {
        assertFalse(Urls.matchesAppOrigin("https://h/", null))
        assertFalse(Urls.matchesAppOrigin("https://h/", ""))
        assertFalse(Urls.matchesAppOrigin("https://h/", "null"))
        assertFalse(Urls.matchesAppOrigin("https://h/", "::not a uri"))
    }
}
