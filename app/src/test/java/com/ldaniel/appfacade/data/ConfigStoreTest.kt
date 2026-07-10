package com.ldaniel.appfacade.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.ldaniel.appfacade.model.WebAppConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ConfigStoreTest {
    @get:Rule val tmp = TemporaryFolder()

    private fun runStoreTest(block: suspend (ConfigStore) -> Unit) = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler) + Job())
        val store = ConfigStore(
            PreferenceDataStoreFactory.createWithPath(scope = scope) {
                tmp.newFolder().resolve("test.preferences_pb").absolutePath.toPath()
            }
        )
        try { block(store) } finally { scope.cancel() }
    }

    private val sorayomi = WebAppConfig(id = "a1", name = "Sorayomi", url = "http://192.168.69.109:4567/")

    @Test fun `starts empty`() = runStoreTest { store ->
        assertEquals(emptyList<WebAppConfig>(), store.apps.first())
    }

    @Test fun `upsert adds then updates`() = runStoreTest { store ->
        store.upsert(sorayomi)
        assertEquals(listOf(sorayomi), store.apps.first())
        val renamed = sorayomi.copy(name = "Manga")
        store.upsert(renamed)
        assertEquals(listOf(renamed), store.apps.first())
    }

    @Test fun `get finds by id`() = runStoreTest { store ->
        store.upsert(sorayomi)
        assertEquals(sorayomi, store.get("a1"))
        assertNull(store.get("missing"))
    }

    @Test fun `delete removes only the matching id`() = runStoreTest { store ->
        val other = sorayomi.copy(id = "b2", name = "TrueNAS")
        store.upsert(sorayomi)
        store.upsert(other)
        store.delete("a1")
        assertEquals(listOf(other), store.apps.first())
    }
}
