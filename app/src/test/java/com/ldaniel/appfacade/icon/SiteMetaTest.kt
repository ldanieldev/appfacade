package com.ldaniel.appfacade.icon

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SiteMetaTest {
    @Test fun `parses title`() =
        assertEquals("Sorayomi", SiteMeta.parseTitle("<html><head><title>Sorayomi</title></head></html>"))

    @Test fun `parses multiline title and trims`() =
        assertEquals("My App", SiteMeta.parseTitle("<title>\n  My App\n</title>"))

    @Test fun `null title when absent`() = assertNull(SiteMeta.parseTitle("<html></html>"))

    @Test fun `finds link rel icon and resolves relative href`() {
        val html = """<link rel="icon" href="/icons/fav.png"><link rel="stylesheet" href="a.css">"""
        assertEquals("http://h:4567/icons/fav.png", SiteMeta.parseIconUrl(html, "http://h:4567/"))
    }

    @Test fun `finds shortcut icon rel variant`() {
        val html = """<link href="fav.ico" rel="shortcut icon">"""
        assertEquals("http://h/fav.ico", SiteMeta.parseIconUrl(html, "http://h/"))
    }

    @Test fun `keeps absolute icon href`() {
        val html = """<link rel="icon" href="http://cdn.example/i.png">"""
        assertEquals("http://cdn.example/i.png", SiteMeta.parseIconUrl(html, "http://h/"))
    }

    @Test fun `falls back to favicon ico`() =
        assertEquals("http://h:4567/favicon.ico", SiteMeta.parseIconUrl("<html></html>", "http://h:4567/"))
}
