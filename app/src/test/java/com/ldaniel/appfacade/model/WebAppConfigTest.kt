package com.ldaniel.appfacade.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class WebAppConfigTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test fun `round-trips through json`() {
        val config = WebAppConfig(
            id = "abc", name = "Sorayomi", url = "http://192.168.0.42:4567/",
            iconPath = "/data/icons/abc.img", requireUnlock = true, fullscreen = true,
            iconSource = "sorayomi", iconStyle = "white",
        )
        val restored = json.decodeFromString<WebAppConfig>(json.encodeToString(WebAppConfig.serializer(), config))
        assertEquals(config, restored)
    }

    @Test fun `decodes with missing optional fields`() {
        val restored = json.decodeFromString<WebAppConfig>(
            """{"id":"x","name":"n","url":"http://h/"}"""
        )
        assertEquals(WebAppConfig(id = "x", name = "n", url = "http://h/"), restored)
        assertEquals(null, restored.iconSource)
        assertEquals("auto", restored.iconStyle)
    }
}
