package com.ldaniel.appfacade.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
}
