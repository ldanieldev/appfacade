package com.ldaniel.appfacade.model

import android.content.pm.ActivityInfo
import org.junit.Assert.assertEquals
import org.junit.Test

class WebOrientationTest {
    @Test fun `toRequested maps values`() {
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED, WebOrientation.toRequested("auto"))
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT, WebOrientation.toRequested("portrait"))
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE, WebOrientation.toRequested("landscape"))
    }

    @Test fun `toRequested falls back to unspecified for unknown`() {
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED, WebOrientation.toRequested("sideways"))
    }
}
