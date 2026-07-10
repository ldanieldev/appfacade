package com.ldaniel.appfacade.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ldaniel.appfacade.model.WebAppConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class ConfigStore(private val dataStore: DataStore<Preferences>) {
    private val key = stringPreferencesKey("apps")
    private val json = Json { ignoreUnknownKeys = true }

    val apps: Flow<List<WebAppConfig>> = dataStore.data.map { prefs -> decode(prefs[key]) }

    suspend fun get(id: String): WebAppConfig? = apps.first().find { it.id == id }

    suspend fun upsert(config: WebAppConfig) =
        edit { list -> list.filterNot { it.id == config.id } + config }

    suspend fun delete(id: String) = edit { list -> list.filterNot { it.id == id } }

    private suspend fun edit(transform: (List<WebAppConfig>) -> List<WebAppConfig>) {
        dataStore.edit { prefs ->
            prefs[key] = json.encodeToString(transform(decode(prefs[key])))
        }
    }

    private fun decode(raw: String?): List<WebAppConfig> =
        raw?.let { json.decodeFromString<List<WebAppConfig>>(it) } ?: emptyList()
}
