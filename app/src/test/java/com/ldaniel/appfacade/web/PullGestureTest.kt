package com.ldaniel.appfacade.web

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PullGestureTest {
    private fun gesture() = PullGesture(triggerPx = 100f, slopPx = 8f)

    @Test fun `refreshes when content reports top and pulled past trigger`() {
        val g = gesture()
        g.down(0f)
        g.contentAtTop(true)
        assertEquals(1f, g.move(120f, true), 0f)
        assertTrue(g.up())
    }

    @Test fun `no refresh when content not at top`() {
        val g = gesture()
        g.down(0f)
        g.contentAtTop(false)
        assertEquals(0f, g.move(120f, true), 0f)
        assertFalse(g.up())
    }

    @Test fun `no refresh when content never reported`() {
        val g = gesture()
        g.down(0f)
        assertEquals(0f, g.move(120f, true), 0f)
        assertFalse(g.up())
    }

    @Test fun `no refresh below trigger`() {
        val g = gesture()
        g.down(0f)
        g.contentAtTop(true)
        assertEquals(0.6f, g.move(60f, true), 0.0001f)
        assertFalse(g.up())
    }

    @Test fun `no refresh when webview itself not at top`() {
        val g = gesture()
        g.down(0f)
        g.contentAtTop(true)
        assertEquals(0f, g.move(120f, false), 0f)
        assertFalse(g.up())
    }

    @Test fun `moving up past slop disqualifies the gesture`() {
        val g = gesture()
        g.down(100f)
        g.contentAtTop(true)
        g.move(80f, true)
        assertEquals(0f, g.move(250f, true), 0f)
        assertFalse(g.up())
    }

    @Test fun `cancel resets`() {
        val g = gesture()
        g.down(0f)
        g.contentAtTop(true)
        g.move(120f, true)
        g.cancel()
        assertFalse(g.up())
    }

    @Test fun `cancel stays in effect until next down`() {
        val g = gesture()
        g.down(0f)
        g.contentAtTop(true)
        g.cancel()
        g.contentAtTop(true)
        assertEquals(0f, g.move(120f, true), 0f)
        assertFalse(g.up())
    }

    @Test fun `new down clears previous at-top report`() {
        val g = gesture()
        g.contentAtTop(true)
        g.down(0f)
        assertEquals(0f, g.move(120f, true), 0f)
    }
}
