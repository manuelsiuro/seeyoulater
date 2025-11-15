package com.msa.seeyoulater.ui.components

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.msa.seeyoulater.data.preferences.ColorScheme
import com.msa.seeyoulater.data.preferences.ThemeMode
import com.msa.seeyoulater.data.preferences.ThemeSettings
import com.msa.seeyoulater.ui.theme.*

/**
 * Full-screen dialog for selecting theme settings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectionDialog(
    currentSettings: ThemeSettings,
    onSettingsChanged: (ThemeSettings) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val systemDarkTheme = isSystemInDarkTheme()
    val isDarkTheme = when (currentSettings.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemDarkTheme
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Theme Selection") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Theme Mode Selection
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Theme Mode",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Choose between light, dark, or system theme",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Theme mode selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeModeButton(
                            text = "Light",
                            icon = Icons.Default.LightMode,
                            isSelected = currentSettings.themeMode == ThemeMode.LIGHT,
                            onClick = {
                                onSettingsChanged(
                                    currentSettings.copy(themeMode = ThemeMode.LIGHT)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeModeButton(
                            text = "Dark",
                            icon = Icons.Default.DarkMode,
                            isSelected = currentSettings.themeMode == ThemeMode.DARK,
                            onClick = {
                                onSettingsChanged(
                                    currentSettings.copy(themeMode = ThemeMode.DARK)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeModeButton(
                            text = "System",
                            icon = Icons.Default.SettingsBrightness,
                            isSelected = currentSettings.themeMode == ThemeMode.SYSTEM,
                            onClick = {
                                onSettingsChanged(
                                    currentSettings.copy(themeMode = ThemeMode.SYSTEM)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Color Scheme Selection
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Color Theme",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Select a color scheme for the app",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Classic Theme
            item {
                ThemePreviewCard(
                    colorScheme = ColorScheme.CLASSIC,
                    isSelected = currentSettings.colorScheme == ColorScheme.CLASSIC,
                    isDarkTheme = isDarkTheme,
                    primaryColor = if (isDarkTheme) md_theme_dark_primary else md_theme_light_primary,
                    secondaryColor = if (isDarkTheme) md_theme_dark_secondary else md_theme_light_secondary,
                    tertiaryColor = if (isDarkTheme) md_theme_dark_tertiary else md_theme_light_tertiary,
                    backgroundColor = if (isDarkTheme) md_theme_dark_surface else md_theme_light_surface,
                    onBackgroundColor = if (isDarkTheme) md_theme_dark_onSurface else md_theme_light_onSurface,
                    onClick = {
                        onSettingsChanged(
                            currentSettings.copy(colorScheme = ColorScheme.CLASSIC)
                        )
                    }
                )
            }

            // Ocean Theme
            item {
                ThemePreviewCard(
                    colorScheme = ColorScheme.OCEAN,
                    isSelected = currentSettings.colorScheme == ColorScheme.OCEAN,
                    isDarkTheme = isDarkTheme,
                    primaryColor = if (isDarkTheme) md_theme_ocean_dark_primary else md_theme_ocean_light_primary,
                    secondaryColor = if (isDarkTheme) md_theme_ocean_dark_secondary else md_theme_ocean_light_secondary,
                    tertiaryColor = if (isDarkTheme) md_theme_ocean_dark_tertiary else md_theme_ocean_light_tertiary,
                    backgroundColor = if (isDarkTheme) md_theme_ocean_dark_surface else md_theme_ocean_light_surface,
                    onBackgroundColor = if (isDarkTheme) md_theme_ocean_dark_onSurface else md_theme_ocean_light_onSurface,
                    onClick = {
                        onSettingsChanged(
                            currentSettings.copy(colorScheme = ColorScheme.OCEAN)
                        )
                    }
                )
            }

            // Forest Theme
            item {
                ThemePreviewCard(
                    colorScheme = ColorScheme.FOREST,
                    isSelected = currentSettings.colorScheme == ColorScheme.FOREST,
                    isDarkTheme = isDarkTheme,
                    primaryColor = if (isDarkTheme) md_theme_forest_dark_primary else md_theme_forest_light_primary,
                    secondaryColor = if (isDarkTheme) md_theme_forest_dark_secondary else md_theme_forest_light_secondary,
                    tertiaryColor = if (isDarkTheme) md_theme_forest_dark_tertiary else md_theme_forest_light_tertiary,
                    backgroundColor = if (isDarkTheme) md_theme_forest_dark_surface else md_theme_forest_light_surface,
                    onBackgroundColor = if (isDarkTheme) md_theme_forest_dark_onSurface else md_theme_forest_light_onSurface,
                    onClick = {
                        onSettingsChanged(
                            currentSettings.copy(colorScheme = ColorScheme.FOREST)
                        )
                    }
                )
            }

            // Sunset Theme
            item {
                ThemePreviewCard(
                    colorScheme = ColorScheme.SUNSET,
                    isSelected = currentSettings.colorScheme == ColorScheme.SUNSET,
                    isDarkTheme = isDarkTheme,
                    primaryColor = if (isDarkTheme) md_theme_sunset_dark_primary else md_theme_sunset_light_primary,
                    secondaryColor = if (isDarkTheme) md_theme_sunset_dark_secondary else md_theme_sunset_light_secondary,
                    tertiaryColor = if (isDarkTheme) md_theme_sunset_dark_tertiary else md_theme_sunset_light_tertiary,
                    backgroundColor = if (isDarkTheme) md_theme_sunset_dark_surface else md_theme_sunset_light_surface,
                    onBackgroundColor = if (isDarkTheme) md_theme_sunset_dark_onSurface else md_theme_sunset_light_onSurface,
                    onClick = {
                        onSettingsChanged(
                            currentSettings.copy(colorScheme = ColorScheme.SUNSET)
                        )
                    }
                )
            }

            // Dynamic Theme (Material You) - only show on Android 12+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                item {
                    ThemePreviewCard(
                        colorScheme = ColorScheme.DYNAMIC,
                        isSelected = currentSettings.colorScheme == ColorScheme.DYNAMIC,
                        isDarkTheme = isDarkTheme,
                        primaryColor = MaterialTheme.colorScheme.primary,
                        secondaryColor = MaterialTheme.colorScheme.secondary,
                        tertiaryColor = MaterialTheme.colorScheme.tertiary,
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        onBackgroundColor = MaterialTheme.colorScheme.onSurface,
                        onClick = {
                            onSettingsChanged(
                                currentSettings.copy(colorScheme = ColorScheme.DYNAMIC)
                            )
                        }
                    )
                }
            }

            // Bottom padding
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * Button for selecting theme mode (Light/Dark/System)
 */
@Composable
private fun ThemeModeButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = text)
            }
        },
        modifier = modifier
    )
}
