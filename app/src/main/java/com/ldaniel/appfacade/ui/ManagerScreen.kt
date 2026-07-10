package com.ldaniel.appfacade.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ldaniel.appfacade.R
import com.ldaniel.appfacade.model.WebAppConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerScreen(
    apps: List<WebAppConfig>,
    onAdd: () -> Unit,
    onEdit: (WebAppConfig) -> Unit,
    onPin: (WebAppConfig) -> Unit,
    onClearCache: () -> Unit,
    onDeepClean: () -> Unit,
    onDelete: (WebAppConfig) -> Unit,
) {
    var confirmDeepClean by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf<WebAppConfig?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_app))
            }
        },
    ) { padding ->
        if (apps.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(padding)) {
                Text(stringResource(R.string.no_apps_yet), Modifier.padding(24.dp))
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                items(apps, key = { it.id }) { app ->
                    var menuOpen by remember { mutableStateOf(false) }
                    ListItem(
                        headlineContent = { Text(app.name) },
                        supportingContent = { Text(app.url) },
                        modifier = Modifier.clickable { onEdit(app) },
                        trailingContent = {
                            IconButton(onClick = { menuOpen = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = null)
                            }
                            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.pin_to_home)) },
                                    onClick = { menuOpen = false; onPin(app) })
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.clear_cache)) },
                                    onClick = { menuOpen = false; onClearCache() })
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.deep_clean)) },
                                    onClick = { menuOpen = false; confirmDeepClean = true })
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.delete)) },
                                    onClick = { menuOpen = false; confirmDelete = app })
                            }
                        },
                    )
                }
            }
        }
    }

    if (confirmDeepClean) {
        AlertDialog(
            onDismissRequest = { confirmDeepClean = false },
            title = { Text(stringResource(R.string.deep_clean)) },
            text = { Text(stringResource(R.string.deep_clean_warning)) },
            confirmButton = {
                TextButton(onClick = { confirmDeepClean = false; onDeepClean() }) {
                    Text(stringResource(R.string.deep_clean))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeepClean = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    confirmDelete?.let { app ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text(stringResource(R.string.delete)) },
            text = { Text(stringResource(R.string.delete_confirm, app.name)) },
            confirmButton = {
                TextButton(onClick = { confirmDelete = null; onDelete(app) }) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}
