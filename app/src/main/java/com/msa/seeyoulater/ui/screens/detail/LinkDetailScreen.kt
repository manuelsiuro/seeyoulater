package com.msa.seeyoulater.ui.screens.detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.msa.seeyoulater.data.local.entity.Link
import com.msa.seeyoulater.data.local.entity.Tag
import com.msa.seeyoulater.data.local.entity.Collection
import com.msa.seeyoulater.ui.components.TagInput
import com.msa.seeyoulater.ui.components.CollectionPicker
import com.msa.seeyoulater.ui.theme.StarredColor
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkDetailScreen(
    viewModel: LinkDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToReader: ((Long) -> Unit)? = null
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Show error as snackbar
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Link") },
            text = { Text("Are you sure you want to delete this link? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteLink(onDeleteComplete = onNavigateBack)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Link Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Navigate back")
                    }
                },
                actions = {
                    // Star/Unstar
                    IconButton(onClick = { viewModel.toggleStar() }) {
                        Icon(
                            imageVector = if (state.link?.isStarred == true)
                                Icons.Filled.Star
                            else
                                Icons.Outlined.StarBorder,
                            contentDescription = if (state.link?.isStarred == true)
                                "Remove from favorites"
                            else
                                "Add to favorites",
                            tint = if (state.link?.isStarred == true)
                                StarredColor
                            else
                                LocalContentColor.current
                        )
                    }
                    // Share
                    IconButton(
                        onClick = {
                            state.link?.let { link ->
                                shareLink(context, link.url, link.title, link.description)
                            }
                        }
                    ) {
                        Icon(Icons.Default.Share, "Share link")
                    }
                    // Delete
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete link",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            // Read, Open, and Save buttons
            state.link?.let { link ->
                Surface(
                    tonalElevation = 3.dp,
                    shadowElevation = 3.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Reader Mode button (full width)
                        if (onNavigateToReader != null) {
                            Button(
                                onClick = { onNavigateToReader(link.id) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Icon(Icons.Default.Article, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Read Article")
                            }
                        }
                        // Open and Save buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { openLinkInBrowser(context, link.url) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.OpenInBrowser, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Open")
                            }
                            Button(
                                onClick = {
                                    viewModel.saveChanges(onSaveComplete = onNavigateBack)
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !state.isSaving
                            ) {
                                if (state.isSaving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                                }
                                Spacer(Modifier.width(8.dp))
                                Text("Save")
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            state.link == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = state.error ?: "Link not found",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onNavigateBack) {
                            Text("Go Back")
                        }
                    }
                }
            }
            else -> {
                LinkDetailContent(
                    link = state.link!!,
                    tags = state.tags,
                    collections = state.collections,
                    allTags = state.allTags,
                    allCollections = state.allCollections,
                    onTitleChanged = { viewModel.updateTitle(it) },
                    onDescriptionChanged = { viewModel.updateDescription(it) },
                    onNotesChanged = { viewModel.updateNotes(it) },
                    onTagsChanged = { viewModel.updateTags(it) },
                    onCollectionsChanged = { viewModel.updateCollections(it) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun LinkDetailContent(
    link: Link,
    tags: List<Tag>,
    collections: List<Collection>,
    allTags: List<Tag>,
    allCollections: List<Collection>,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onTagsChanged: (List<Tag>) -> Unit,
    onCollectionsChanged: (List<Collection>) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Preview Image
        if (!link.previewImageUrl.isNullOrBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(link.previewImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Link preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Favicon and URL
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(link.faviconUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Favicon",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "URL",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = link.url,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        HorizontalDivider()

        // Editable Title
        OutlinedTextField(
            value = link.title ?: "",
            onValueChange = onTitleChanged,
            label = { Text("Title") },
            placeholder = { Text("Enter a title for this link") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            maxLines = 3
        )

        // Editable Description
        OutlinedTextField(
            value = link.description ?: "",
            onValueChange = onDescriptionChanged,
            label = { Text("Description") },
            placeholder = { Text("Add a description (optional)") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            singleLine = false,
            maxLines = 5
        )

        // Personal Notes
        OutlinedTextField(
            value = link.notes ?: "",
            onValueChange = onNotesChanged,
            label = { Text("Personal Notes") },
            placeholder = { Text("Add your thoughts, ideas, or reminders (optional)") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            singleLine = false,
            maxLines = 6,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Note,
                    contentDescription = "Notes"
                )
            }
        )

        // Show last modified timestamp for notes if available
        if (link.notesLastModified != null) {
            Text(
                text = "Notes last edited: ${formatTimestamp(link.notesLastModified!!)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        HorizontalDivider()

        // Tags Section
        Text(
            text = "Tags",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        TagInput(
            currentTags = tags,
            allTags = allTags,
            onTagsChanged = onTagsChanged
        )

        HorizontalDivider()

        // Collections Section
        Text(
            text = "Collections",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        CollectionPicker(
            currentCollections = collections,
            allCollections = allCollections,
            onCollectionsChanged = onCollectionsChanged
        )

        HorizontalDivider()

        // Metadata
        Text(
            text = "Metadata",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        MetadataRow(
            label = "Added",
            value = formatTimestamp(link.addedTimestamp)
        )

        if (link.isOpened && link.lastOpenedTimestamp != null) {
            MetadataRow(
                label = "Last Opened",
                value = formatTimestamp(link.lastOpenedTimestamp!!)
            )
        }

        MetadataRow(
            label = "Status",
            value = buildString {
                if (link.isStarred) append("Starred")
                if (link.isStarred && link.isOpened) append(" • ")
                if (link.isOpened) append("Opened")
                if (!link.isStarred && !link.isOpened) append("New")
            }
        )

        // Spacer at bottom for better scrolling
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun MetadataRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun openLinkInBrowser(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun shareLink(context: Context, url: String, title: String? = null, description: String? = null) {
    val shareText = buildString {
        if (!title.isNullOrBlank()) {
            append(title)
            append("\n\n")
        }
        if (!description.isNullOrBlank()) {
            append(description)
            append("\n\n")
        }
        append(url)
    }

    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        if (!title.isNullOrBlank()) {
            putExtra(Intent.EXTRA_SUBJECT, title)
        }
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, null)
    context.startActivity(shareIntent)
}
