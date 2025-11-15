package com.msa.seeyoulater.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Preview // Example icon for preview setting
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // Added import for Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.msa.seeyoulater.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    var showClearConfirmationDialog by remember { mutableStateOf(false) }
    // Example state holders for settings (replace with actual persisted state later)
    var urlPreviewEnabled by remember { mutableStateOf(true) }
     // In a real app, load/save these from DataStore or SharedPreferences via ViewModel
    // val urlPreviewEnabled by viewModel.previewEnabled.collectAsState()


    if (showClearConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmationDialog = false },
            title = { Text(stringResource(R.string.clear_all_links_title)) },
            text = { Text(stringResource(R.string.clear_all_links_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllLinks()
                        showClearConfirmationDialog = false
                        // Optionally show a confirmation toast/snackbar
                    },
                     colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.clear_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmationDialog = false }) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back_button))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // --- URL Preview Setting ---
             SettingItem(
                icon = Icons.Default.Preview,
                title = stringResource(R.string.settings_enable_url_preview),
                description = "Automatically fetch title, description, and images for saved links.", // Optional description
                onClick = { urlPreviewEnabled = !urlPreviewEnabled /* TODO: Persist change via ViewModel */ }
            ) {
                Switch(
                    checked = urlPreviewEnabled,
                    onCheckedChange = { isChecked -> urlPreviewEnabled = isChecked /* TODO: Persist */ }
                )
            }

             Divider(modifier = Modifier.padding(vertical = 8.dp))

             // --- Theme Setting (Example - Needs proper implementation) ---
             SettingItem(
                 icon = Icons.Default.Palette,
                 title = stringResource(R.string.settings_theme),
                 description = "Current: System Default", // Replace with actual theme state
                 onClick = { /* TODO: Show theme selection dialog/navigate */ }
             )

             Divider(modifier = Modifier.padding(vertical = 8.dp))


            // --- Clear All Links ---
            SettingItem(
                 icon = Icons.Default.ClearAll,
                 title = stringResource(R.string.settings_clear_all_links),
                 iconTint = MaterialTheme.colorScheme.error, // Indicate destructive action
                 titleColor = MaterialTheme.colorScheme.error,
                 onClick = { showClearConfirmationDialog = true }
            )


            Spacer(modifier = Modifier.weight(1f)) // Push About section to bottom

            // --- About/Info Section ---
             Divider(modifier = Modifier.padding(vertical = 8.dp))
             SettingItem(
                 icon = Icons.Default.Info,
                 title = "About Link Manager", // Replace with actual App Info
                 description = "Version 1.0", // Replace with dynamic version name
                 onClick = { /* TODO: Navigate to an About screen or show info dialog */ }
             )
             // Add links for Privacy Policy, Licenses etc. here

        }
    }
}

@Composable
fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color = LocalContentColor.current,
    title: String,
    titleColor: Color = LocalContentColor.current,
    description: String? = null,
    onClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null, // Title serves as description
            modifier = Modifier.size(24.dp),
            tint = iconTint
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, color = titleColor)
            if (description != null) {
                 Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalContentColor.current.copy(alpha = 0.6f) // Material 3 medium emphasis
                )
            }
        }
        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(16.dp))
            trailingContent()
        }
    }
}
