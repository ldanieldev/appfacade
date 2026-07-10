package com.ldaniel.appfacade.icon

import org.junit.Assert.assertEquals
import org.junit.Test

class SelfhstIconsTest {
    @Test fun `slugify lowercases`() =
        assertEquals("lubelogger", SelfhstIcons.slugify("LubeLogger"))

    @Test fun `slugify trims whitespace`() =
        assertEquals("mealie", SelfhstIcons.slugify("  Mealie "))

    @Test fun `slugify collapses non-alphanumerics to hyphens`() =
        assertEquals("my-app-2", SelfhstIcons.slugify("My App 2"))

    @Test fun `slugify of only symbols is empty`() =
        assertEquals("", SelfhstIcons.slugify("__"))

    @Test fun `pngUrl builds cdn url from slugified name`() =
        assertEquals(
            "https://cdn.jsdelivr.net/gh/selfhst/icons/png/lube-logger.png",
            SelfhstIcons.pngUrl("Lube Logger"),
        )
}
