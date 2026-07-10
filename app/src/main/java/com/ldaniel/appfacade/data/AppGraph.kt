package com.ldaniel.appfacade.data

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile

object AppGraph {
    @Volatile private var store: ConfigStore? = null

    fun configStore(context: Context): ConfigStore = store ?: synchronized(this) {
        store ?: ConfigStore(
            PreferenceDataStoreFactory.create(
                produceFile = { context.applicationContext.preferencesDataStoreFile("appfacade") }
            )
        ).also { store = it }
    }
}
