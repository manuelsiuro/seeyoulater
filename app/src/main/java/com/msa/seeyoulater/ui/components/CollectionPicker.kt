package com.msa.seeyoulater.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.msa.seeyoulater.data.local.entity.Collection

/**
 * Collection picker component for selecting collections
 *
 * @param currentCollections The list of collections the link is currently in
 * @param allCollections The list of all available collections
 * @param onCollectionsChanged Callback when collections are modified
 * @param modifier Optional modifier
 */
@Composable
fun CollectionPicker(
    currentCollections: List<Collection>,
    allCollections: List<Collection>,
    onCollectionsChanged: (List<Collection>) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        // Display current collections
        if (currentCollections.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                currentCollections.forEach { collection ->
                    CollectionChip(
                        collection = collection,
                        onRemove = {
                            onCollectionsChanged(currentCollections.filter { it.id != collection.id })
                        }
                    )
                }
            }
        }

        // Add to collection button
        OutlinedButton(
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add to collection",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add to Collection")
        }
    }

    // Collection selection dialog
    if (showDialog) {
        CollectionSelectionDialog(
            currentCollections = currentCollections,
            allCollections = allCollections,
            onCollectionsChanged = { newCollections ->
                onCollectionsChanged(newCollections)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

/**
 * Chip component for displaying a collection
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollectionChip(
    collection: Collection,
    onRemove: () -> Unit
) {
    val backgroundColor = collection.color?.let { parseColor(it) }
        ?: MaterialTheme.colorScheme.tertiaryContainer
    val contentColor = if (collection.color != null && parseColor(collection.color) != null) {
        getContrastColor(parseColor(collection.color)!!)
    } else {
        MaterialTheme.colorScheme.onTertiaryContainer
    }

    FilterChip(
        selected = false,
        onClick = { /* Collections are not clickable by default */ },
        label = {
            Text(
                text = collection.name,
                style = MaterialTheme.typography.labelMedium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = contentColor
            )
        },
        trailingIcon = {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(18.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Remove from collection",
                    modifier = Modifier.size(16.dp),
                    tint = contentColor
                )
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = backgroundColor,
            labelColor = contentColor,
            iconColor = contentColor
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * Dialog for selecting collections
 */
@Composable
private fun CollectionSelectionDialog(
    currentCollections: List<Collection>,
    allCollections: List<Collection>,
    onCollectionsChanged: (List<Collection>) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCollections by remember { mutableStateOf(currentCollections.toSet()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                // Title
                Text(
                    text = "Add to Collections",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Selected collections
                if (selectedCollections.isNotEmpty()) {
                    Text(
                        text = "Selected:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        selectedCollections.forEach { collection ->
                            CollectionChip(
                                collection = collection,
                                onRemove = {
                                    selectedCollections = selectedCollections - collection
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Available collections list
                Text(
                    text = "Available collections:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(allCollections) { collection ->
                        val isSelected = selectedCollections.any { it.id == collection.id }
                        CollectionItem(
                            collection = collection,
                            isSelected = isSelected,
                            onClick = {
                                selectedCollections = if (isSelected) {
                                    selectedCollections.filter { it.id != collection.id }.toSet()
                                } else {
                                    selectedCollections + collection
                                }
                            }
                        )
                    }

                    if (allCollections.isEmpty()) {
                        item {
                            Text(
                                text = "No collections yet. Create one in the Collections screen!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        onCollectionsChanged(selectedCollections.toList())
                    }) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}

/**
 * Collection item in the selection list
 */
@Composable
private fun CollectionItem(
    collection: Collection,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = null
        )
        Spacer(modifier = Modifier.width(12.dp))
        Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = null,
            tint = collection.color?.let { parseColor(it) } ?: MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = collection.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            if (!collection.description.isNullOrBlank()) {
                Text(
                    text = collection.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = "${collection.linkCount}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Parse hex color string to Color
 */
private fun parseColor(colorString: String): Color? {
    return try {
        Color(android.graphics.Color.parseColor(colorString))
    } catch (e: Exception) {
        null
    }
}

/**
 * Get contrasting text color for the given background color
 */
private fun getContrastColor(backgroundColor: Color): Color {
    val red = backgroundColor.red
    val green = backgroundColor.green
    val blue = backgroundColor.blue
    val luminance = 0.299 * red + 0.587 * green + 0.114 * blue
    return if (luminance > 0.5) Color.Black else Color.White
}
