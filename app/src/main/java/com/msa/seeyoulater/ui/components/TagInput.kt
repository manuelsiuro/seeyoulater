package com.msa.seeyoulater.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.msa.seeyoulater.data.local.entity.Tag
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce

/**
 * Tag input component with autocomplete functionality
 *
 * @param currentTags The list of tags currently applied
 * @param allTags The list of all available tags for autocomplete
 * @param onTagsChanged Callback when tags are modified
 * @param modifier Optional modifier
 */
@OptIn(FlowPreview::class)
@Composable
fun TagInput(
    currentTags: List<Tag>,
    allTags: List<Tag>,
    onTagsChanged: (List<Tag>) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    // Display current tags as chips
    Column(modifier = modifier) {
        // Current tags display
        if (currentTags.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                currentTags.forEach { tag ->
                    TagChip(
                        tag = tag,
                        onRemove = {
                            onTagsChanged(currentTags.filter { it.id != tag.id })
                        }
                    )
                }
            }
        }

        // Add tag button
        OutlinedButton(
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add tag",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Tag")
        }
    }

    // Tag selection dialog
    if (showDialog) {
        TagSelectionDialog(
            currentTags = currentTags,
            allTags = allTags,
            onTagsChanged = { newTags ->
                onTagsChanged(newTags)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

/**
 * Dialog for selecting and creating tags
 */
@Composable
private fun TagSelectionDialog(
    currentTags: List<Tag>,
    allTags: List<Tag>,
    onTagsChanged: (List<Tag>) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTags by remember { mutableStateOf(currentTags.toSet()) }

    val filteredTags = remember(searchQuery, allTags) {
        if (searchQuery.isBlank()) {
            allTags
        } else {
            allTags.filter { tag ->
                tag.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

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
                    text = "Manage Tags",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Search/Create input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search or create tag") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocalOffer,
                            contentDescription = null
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            // Create new tag if query doesn't match existing tags
                            if (searchQuery.isNotBlank() &&
                                allTags.none { it.name.equals(searchQuery, ignoreCase = true) }
                            ) {
                                val newTag = Tag(name = searchQuery.trim())
                                selectedTags = selectedTags + newTag
                                searchQuery = ""
                            }
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Create new tag button (if query doesn't match existing)
                if (searchQuery.isNotBlank() &&
                    allTags.none { it.name.equals(searchQuery, ignoreCase = true) }
                ) {
                    TextButton(
                        onClick = {
                            val newTag = Tag(name = searchQuery.trim())
                            selectedTags = selectedTags + newTag
                            searchQuery = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create tag: \"${searchQuery.trim()}\"")
                    }
                }

                // Selected tags
                if (selectedTags.isNotEmpty()) {
                    Text(
                        text = "Selected:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        selectedTags.forEach { tag ->
                            TagChip(
                                tag = tag,
                                onRemove = {
                                    selectedTags = selectedTags - tag
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Available tags list
                Text(
                    text = "Available tags:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredTags) { tag ->
                        val isSelected = selectedTags.any { it.id == tag.id }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedTags = if (isSelected) {
                                        selectedTags.filter { it.id != tag.id }.toSet()
                                    } else {
                                        selectedTags + tag
                                    }
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TagChip(tag = tag)
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "${tag.usageCount} links",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (filteredTags.isEmpty() && searchQuery.isBlank()) {
                        item {
                            Text(
                                text = "No tags yet. Create your first tag!",
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
                        onTagsChanged(selectedTags.toList())
                    }) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}
