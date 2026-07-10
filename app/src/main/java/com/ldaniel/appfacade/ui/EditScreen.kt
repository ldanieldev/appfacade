package com.ldaniel.appfacade.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ldaniel.appfacade.R
import com.ldaniel.appfacade.model.Urls
import com.ldaniel.appfacade.model.WebAppConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    original: WebAppConfig?,             // null = adding a new app
    canLock: Boolean,                    // Authenticator.canAuthenticate
    onSave: (name: String, url: String, requireUnlock: Boolean, fullscreen: Boolean) -> Unit,
    onCancel: () -> Unit,
    onDisableLockRequested: (apply: () -> Unit) -> Unit, // side-door guard hook
) {
    var name by remember { mutableStateOf(original?.name ?: "") }
    var url by remember { mutableStateOf(original?.url ?: "") }
    var requireUnlock by remember { mutableStateOf(original?.requireUnlock ?: false) }
    var fullscreen by remember { mutableStateOf(original?.fullscreen ?: true) }
    var urlError by remember { mutableStateOf(false) }

    Scaffold(topBar = {
        TopAppBar(title = {
            Text(stringResource(if (original == null) R.string.add_app else R.string.edit_app))
        })
    }) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
        ) {
            OutlinedTextField(
                value = url,
                onValueChange = { url = it; urlError = false },
                label = { Text(stringResource(R.string.url)) },
                isError = urlError,
                supportingText = {
                    when {
                        urlError -> Text(stringResource(R.string.invalid_url))
                        Urls.normalize(url)?.startsWith("http://") == true ->
                            Text(stringResource(R.string.http_hint))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.name)) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
            Row(
                Modifier.fillMaxWidth().padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.require_unlock))
                    if (!canLock) Text(stringResource(R.string.no_lockscreen_warning))
                }
                Switch(
                    checked = requireUnlock,
                    enabled = canLock,
                    onCheckedChange = { wanted ->
                        // Spec §5.2 side-door guard: turning an existing lock OFF
                        // requires authentication first.
                        if (!wanted && original?.requireUnlock == true) {
                            onDisableLockRequested { requireUnlock = false }
                        } else {
                            requireUnlock = wanted
                        }
                    },
                )
            }
            Row(
                Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.fullscreen), Modifier.weight(1f))
                Switch(checked = fullscreen, onCheckedChange = { fullscreen = it })
            }
            Row(Modifier.fillMaxWidth().padding(top = 24.dp)) {
                TextButton(onClick = onCancel) { Text(stringResource(R.string.cancel)) }
                Button(
                    onClick = {
                        val normalized = Urls.normalize(url)
                        if (normalized == null) urlError = true
                        else onSave(name.trim(), normalized, requireUnlock, fullscreen)
                    },
                    modifier = Modifier.padding(start = 8.dp),
                ) { Text(stringResource(R.string.save)) }
            }
        }
    }
}
