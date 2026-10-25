package com.ldaniel.appfacade.web

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorsTest {
    @Test fun `isLight white is true`() = assertTrue(Colors.isLight(0xFFFFFFFF.toInt()))
    @Test fun `isLight black is false`() = assertFalse(Colors.isLight(0xFF000000.toInt()))
    @Test fun `isLight orange is true`() = assertTrue(Colors.isLight(0xFFFF9800.toInt()))
    @Test fun `isLight dark slate is false`() = assertFalse(Colors.isLight(0xFF161B22.toInt()))
}
