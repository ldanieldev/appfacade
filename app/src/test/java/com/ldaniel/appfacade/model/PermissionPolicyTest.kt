package com.ldaniel.appfacade.model

import android.webkit.PermissionRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PermissionPolicyTest {
    private val config = WebAppConfig(id = "a", name = "n", url = "https://fit.example.com/", allowCamera = true)
    private val held = setOf(SitePermission.CAMERA)
    private val origin = "https://fit.example.com"

    @Test fun `allowed when toggle on held and origin matches`() =
        assertTrue(PermissionPolicy.isAllowed(SitePermission.CAMERA, config, held, origin))

    @Test fun `denied when toggle off`() =
        assertFalse(PermissionPolicy.isAllowed(SitePermission.CAMERA, config.copy(allowCamera = false), held, origin))

    @Test fun `denied when not held`() =
        assertFalse(PermissionPolicy.isAllowed(SitePermission.CAMERA, config, emptySet(), origin))

    @Test fun `denied when origin differs`() =
        assertFalse(PermissionPolicy.isAllowed(SitePermission.CAMERA, config, held, "https://evil.example.com"))

    @Test fun `fromWebResource maps camera and mic, rejects others`() {
        assertEquals(SitePermission.CAMERA, SitePermission.fromWebResource(PermissionRequest.RESOURCE_VIDEO_CAPTURE))
        assertEquals(SitePermission.MICROPHONE, SitePermission.fromWebResource(PermissionRequest.RESOURCE_AUDIO_CAPTURE))
        assertNull(SitePermission.fromWebResource(PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID))
        assertNull(SitePermission.fromWebResource(PermissionRequest.RESOURCE_MIDI_SYSEX))
    }
}
