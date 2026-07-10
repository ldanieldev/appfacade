package com.ldaniel.appfacade.icon

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LetterIconTest {
    @Test fun `letter is first alphanumeric uppercased`() {
        assertEquals("S", LetterIcon.letterFor("sorayomi"))
        assertEquals("T", LetterIcon.letterFor("  truenas"))
        assertEquals("9", LetterIcon.letterFor("9gag"))
    }

    @Test fun `letter falls back for empty or symbol-only names`() {
        assertEquals("?", LetterIcon.letterFor(""))
        assertEquals("?", LetterIcon.letterFor("---"))
    }

    @Test fun `color is stable for a given name`() =
        assertEquals(LetterIcon.colorFor("Sorayomi"), LetterIcon.colorFor("Sorayomi"))

    @Test fun `color is a valid opaque argb int`() =
        assertTrue(LetterIcon.colorFor("x") and 0xFF000000.toInt() == 0xFF000000.toInt())
}
