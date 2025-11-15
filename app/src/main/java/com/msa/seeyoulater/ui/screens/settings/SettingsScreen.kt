package com.msa.seeyoulater.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Preview // Example icon for preview setting
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // Added import for Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.msa.seeyoulater.LinkManagerApp
import com.msa.seeyoulater.R
import com.msa.seeyoulater.data.export.ExportManager
import com.msa.seeyoulater.ui.components.ThemeSelectionDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCollections: () -> Unit = {},
    onNavigateToTags: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {}
) {
    val context = LocalContext.current
    val application = context.applicationContext as LinkManagerApp
    val scope = rememberCoroutineScope()

    var showClearConfirmationDialog by remember { mutableStateOf(false) }
    var showThemeSelectionDialog by remember { mutableStateOf(false) }
    var exportMessage by remember { mutableStateOf<String?>(null) }

    // Collect theme settings from ViewModel
    val themeSettings by viewModel.themeSettings.collectAsState()

    // Collect URL preview enabled state from ViewModel
    val urlPreviewEnabled by viewModel.urlPreviewEnabled.collectAsState()

    // Create ExportManager
    val exportManager = remember {
        ExportManager(
            context = context,
            linkDao = application.database.linkDao(),
            tagDao = application.database.tagDao(),
            collectionDao = application.database.collectionDao()
        )
    }

    // Snackbar for export messages
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(exportMessage) {
        exportMessage?.let {
            snackbarHostState.showSnackbar(it)
            exportMessage = null
        }
    }


    // Clear confirmation dialog
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

    // Theme selection dialog
    if (showThemeSelectionDialog) {
        Dialog(
            onDismissRequest = { showThemeSelectionDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            ThemeSelectionDialog(
                currentSettings = themeSettings,
                onSettingsChanged = { newSettings ->
                    viewModel.updateThemeSettings(newSettings)
                },
                onDismiss = { showThemeSelectionDialog = false }
            )
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars),
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                description = "Automatically fetch title, description, and images for saved links.",
                onClick = { viewModel.setUrlPreviewEnabled(!urlPreviewEnabled) }
            ) {
                Switch(
                    checked = urlPreviewEnabled,
                    onCheckedChange = { isChecked -> viewModel.setUrlPreviewEnabled(isChecked) }
                )
            }

             Divider(modifier = Modifier.padding(vertical = 8.dp))

             // --- Collections Management ---
             SettingItem(
                 icon = Icons.Default.Folder,
                 title = "Manage Collections",
                 description = "Create and organize your collections",
                 onClick = onNavigateToCollections
             )

             Divider(modifier = Modifier.padding(vertical = 8.dp))

             // --- Tags Management ---
             SettingItem(
                 icon = Icons.Default.BookmarkBorder,
                 title = "Manage Tags",
                 description = "Create and organize your tags",
                 onClick = onNavigateToTags
             )

             Divider(modifier = Modifier.padding(vertical = 8.dp))

             // --- Statistics ---
             SettingItem(
                 icon = Icons.Default.BarChart,
                 title = "Statistics",
                 description = "View your bookmarks insights and analytics",
                 onClick = onNavigateToStatistics
             )

             Divider(modifier = Modifier.padding(vertical = 8.dp))

             // --- Check Link Health ---
             SettingItem(
                 icon = Icons.Default.HealthAndSafety,
                 title = "Check Link Health",
                 description = "Verify all links are still accessible",
                 onClick = {
                     scope.launch {
                         exportMessage = "Checking all links... This may take a while"
                         viewModel.checkAllLinksHealth()
                         exportMessage = "Health check completed"
                     }
                 }
             )

             Divider(modifier = Modifier.padding(vertical = 8.dp))

             // --- Theme Setting ---
             SettingItem(
                 icon = Icons.Default.Palette,
                 title = stringResource(R.string.settings_theme),
                 description = "Color: ${themeSettings.colorScheme.displayName()} • Mode: ${themeSettings.themeMode.name.lowercase().replaceFirstChar { it.uppercase() }}",
                 onClick = { showThemeSelectionDialog = true }
             )

             Divider(modifier = Modifier.padding(vertical = 8.dp))

            // --- Export to HTML ---
            SettingItem(
                icon = Icons.Default.FileDownload,
                title = "Export to HTML",
                description = "Export bookmarks in browser-compatible format",
                onClick = {
                    scope.launch {
                        val filePath = viewModel.exportToHtml(exportManager)
                        exportMessage = if (filePath != null) {
                            "Bookmarks exported to HTML successfully"
                        } else {
                            "Failed to export bookmarks"
                        }
                    }
                }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // --- Export to JSON ---
            SettingItem(
                icon = Icons.Default.FileDownload,
                title = "Export to JSON",
                description = "Export all data as backup",
                onClick = {
                    scope.launch {
                        val filePath = viewModel.exportToJson(exportManager)
                        exportMessage = if (filePath != null) {
                            "Data exported to JSON successfully"
                        } else {
                            "Failed to export data"
                        }
                    }
                }
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
