package com.ldaniel.appfacade.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.ldaniel.appfacade.R
import com.ldaniel.appfacade.data.AppGraph
import com.ldaniel.appfacade.icon.IconFetcher
import com.ldaniel.appfacade.lock.Authenticator
import com.ldaniel.appfacade.model.SitePermission
import com.ldaniel.appfacade.model.WebAppConfig
import com.ldaniel.appfacade.shortcut.Shortcuts
import com.ldaniel.appfacade.web.CacheOps
import com.ldaniel.appfacade.web.SitePermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/** null-sentinel wrapper: Editing(null) = adding a new app. */
private data class Editing(val original: WebAppConfig?)

class ManagerActivity : FragmentActivity() {
    private var held by mutableStateOf<Set<SitePermission>>(emptySet())

    private var blocked by mutableStateOf<Set<SitePermission>>(emptySet())

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        held = SitePermissions.held(this)
        blocked = blocked - held
        val permanentlyDenied = results.isNotEmpty() && results.values.none { it } &&
            results.keys.none { shouldShowRequestPermissionRationale(it) }
        if (permanentlyDenied) {
            val requested = SitePermission.entries.filter { p ->
                SitePermissions.manifestPermissions(p).any { it in results.keys }
            }
            blocked = (blocked + requested) - held
        }
    }

    override fun onResume() {
        super.onResume()
        held = SitePermissions.held(this)
        blocked = blocked - held
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = AppGraph.configStore(this)

        setContent {
            val scheme = if (isSystemInDarkTheme()) dynamicDarkColorScheme(LocalContext.current)
                         else dynamicLightColorScheme(LocalContext.current)
            MaterialTheme(colorScheme = scheme) {
                val apps by store.apps.collectAsState(initial = emptyList())
                var editing by remember { mutableStateOf<Editing?>(null) }

                val current = editing
                if (current == null) {
                    ManagerScreen(
                        apps = apps,
                        onAdd = { editing = Editing(null) },
                        onEdit = { editing = Editing(it) },
                        onPin = { Shortcuts.pin(this, it) },
                        onClearCache = {
                            CacheOps.clearHttpCache(this)
                            Toast.makeText(this, R.string.cache_cleared, Toast.LENGTH_SHORT).show()
                        },
                        onDeepClean = {
                            CacheOps.deepClean(this) {
                                Toast.makeText(this, R.string.deep_clean_done, Toast.LENGTH_SHORT).show()
                            }
                        },
                        onDelete = { app -> guardedDelete(app) },
                        held = held,
                        blocked = blocked,
                        onRequestPermission = { permission ->
                            permissionLauncher.launch(SitePermissions.manifestPermissions(permission))
                        },
                        onOpenSettings = {
                            startActivity(
                                Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", packageName, null),
                                )
                            )
                        },
                    )
                } else {
                    BackHandler { editing = null }
                    EditScreen(
                        original = current.original,
                        canLock = Authenticator.canAuthenticate(this),
                        onCancel = { editing = null },
                        onDisableLockRequested = { apply ->
                            Authenticator.authenticate(
                                this, getString(R.string.unlock_to_change), onSuccess = apply,
                            )
                        },
                        held = held,
                        onSave = { draft ->
                            lifecycleScope.launch {
                                val id = current.original?.id ?: UUID.randomUUID().toString()
                                val name = draft.name
                                val url = draft.url
                                val iconSource = draft.iconSource
                                var config = draft.copy(id = id, iconPath = current.original?.iconPath)
                                val sourceChanged = iconSource != current.original?.iconSource
                                if (config.name.isBlank() || config.iconPath == null || sourceChanged) {
                                    val meta = withContext(Dispatchers.IO) {
                                        IconFetcher.fetch(
                                            this@ManagerActivity, id, url,
                                            name.ifBlank { null }, iconSource,
                                        )
                                    }
                                    config = config.copy(
                                        name = config.name.ifBlank {
                                            meta.title ?: java.net.URI(url).host
                                        },
                                        // Don't clobber a working icon with a failed fetch.
                                        iconPath = meta.iconPath ?: config.iconPath,
                                    )
                                }
                                store.upsert(config)
                                Shortcuts.update(this@ManagerActivity, config)
                                editing = null
                            }
                        },
                    )
                }
            }
        }
    }

    /** Spec §5.2: deleting a locked app requires authentication first. */
    private fun guardedDelete(app: WebAppConfig) {
        val doDelete = {
            // Disable the shortcut first (synchronous, can't be interrupted), then
            // delete the config under NonCancellable so activity teardown can't
            // cancel between the two side effects and leave a live dangling shortcut.
            Shortcuts.disable(this, app.id)
            lifecycleScope.launch {
                withContext(NonCancellable) {
                    AppGraph.configStore(this@ManagerActivity).delete(app.id)
                }
            }
            Unit
        }
        if (app.requireUnlock) {
            Authenticator.authenticate(this, getString(R.string.unlock_to_change), onSuccess = doDelete)
        } else {
            doDelete()
        }
    }
}
