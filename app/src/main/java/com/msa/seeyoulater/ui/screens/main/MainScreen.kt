package com.msa.seeyoulater.ui.screens.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.msa.seeyoulater.LinkManagerApp
import com.msa.seeyoulater.R
import com.msa.seeyoulater.data.local.entity.Link
import com.msa.seeyoulater.ui.screens.main.components.LinkActionBottomSheet
import com.msa.seeyoulater.ui.screens.main.components.LinkItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class, ExperimentalCoroutinesApi::class)
@Composable
fun MainScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToDetail: (Long) -> Unit = {}
) {
    val context = LocalContext.current
    val application = context.applicationContext as LinkManagerApp
    val viewModel: MainViewModel = viewModel(
        factory = application.viewModelFactory
    )

    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedLinkForAction by remember { mutableStateOf<Link?>(null) }
    var showSearchBar by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Show Snackbar for undo delete
    LaunchedEffect(state.recentlyDeletedLink) {
        state.recentlyDeletedLink?.let { deletedLink ->
            val result = snackbarHostState.showSnackbar(
                message = "Link deleted",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            when (result) {
                SnackbarResult.ActionPerformed -> {
                    viewModel.undoDelete()
                }
                SnackbarResult.Dismissed -> {
                    viewModel.clearUndoState()
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (state.isSelectionMode) {
                // Selection Mode TopAppBar
                TopAppBar(
                    title = { Text("${state.selectedLinkIds.size} selected") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.exitSelectionMode() }) {
                            Icon(Icons.Default.Close, "Exit selection mode")
                        }
                    },
                    actions = {
                        // Select All
                        IconButton(onClick = { viewModel.selectAll() }) {
                            Icon(Icons.Default.SelectAll, "Select all")
                        }
                        // Star/Unstar
                        IconButton(onClick = { viewModel.bulkToggleStarSelected() }) {
                            Icon(Icons.Default.Star, "Toggle star for selected")
                        }
                        // Archive
                        IconButton(onClick = { viewModel.bulkArchiveSelected() }) {
                            Icon(Icons.Default.Archive, "Archive selected")
                        }
                        // Delete
                        IconButton(onClick = { viewModel.bulkDeleteSelected() }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete selected",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            } else if (showSearchBar) {
                // Search Bar
                TopAppBar(
                    title = {
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search links...") },
                            singleLine = true,
                            trailingIcon = {
                                if (state.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                        Icon(Icons.Default.Clear, "Clear search")
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            showSearchBar = false
                            viewModel.onSearchQueryChanged("")
                        }) {
                            Icon(Icons.Default.ArrowBack, "Close search")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            } else {
                // Normal TopAppBar
                TopAppBar(
                    title = { Text("See You Later") },
                    actions = {
                        // Search button
                        IconButton(onClick = { showSearchBar = true }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        }
                        // Sort button
                        Box {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.Sort,
                                    contentDescription = "Sort"
                                )
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Date Added") },
                                    onClick = {
                                        viewModel.onSortChanged(SortOption.DATE_ADDED)
                                        showSortMenu = false
                                    },
                                    leadingIcon = {
                                        if (state.sortOption == SortOption.DATE_ADDED) {
                                            Icon(Icons.Default.Check, null)
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Last Opened") },
                                    onClick = {
                                        viewModel.onSortChanged(SortOption.LAST_OPENED)
                                        showSortMenu = false
                                    },
                                    leadingIcon = {
                                        if (state.sortOption == SortOption.LAST_OPENED) {
                                            Icon(Icons.Default.Check, null)
                                        }
                                    }
                                )
                            }
                        }
                        // Settings button
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter Chips
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // Standard filters (All/Starred)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.filterOption == FilterOption.ALL,
                        onClick = { viewModel.onFilterChanged(FilterOption.ALL) },
                        label = { Text("All Links") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    )
                    FilterChip(
                        selected = state.filterOption == FilterOption.STARRED,
                        onClick = { viewModel.onFilterChanged(FilterOption.STARRED) },
                        label = { Text("Starred") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    )
                    FilterChip(
                        selected = state.filterOption == FilterOption.ARCHIVED,
                        onClick = { viewModel.onFilterChanged(FilterOption.ARCHIVED) },
                        label = { Text("Archived") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Archive,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    )
                }

                // Tag and Collection filters (scrollable)
                if (state.allTags.isNotEmpty() || state.allCollections.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Tag filters
                        state.allTags.forEach { tag ->
                            FilterChip(
                                selected = state.selectedTagFilter?.id == tag.id,
                                onClick = {
                                    if (state.selectedTagFilter?.id == tag.id) {
                                        viewModel.onTagFilterSelected(null)
                                    } else {
                                        viewModel.onTagFilterSelected(tag)
                                    }
                                },
                                label = { Text(tag.name) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.LocalOffer,
                                        contentDescription = null,
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                }
                            )
                        }

                        // Collection filters
                        state.allCollections.forEach { collection ->
                            FilterChip(
                                selected = state.selectedCollectionFilter?.id == collection.id,
                                onClick = {
                                    if (state.selectedCollectionFilter?.id == collection.id) {
                                        viewModel.onCollectionFilterSelected(null)
                                    } else {
                                        viewModel.onCollectionFilterSelected(collection)
                                    }
                                },
                                label = { Text(collection.name) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                }
                            )
                        }
                    }
                }

                // Active filter indicator
                if (state.selectedTagFilter != null || state.selectedCollectionFilter != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = state.selectedTagFilter?.let { "Filtered by tag: ${it.name}" }
                                ?: state.selectedCollectionFilter?.let { "Filtered by collection: ${it.name}" }
                                ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.clearFilters() }) {
                            Text("Clear")
                        }
                    }
                }
            }

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    scope.launch {
                        isRefreshing = true
                        viewModel.refreshLinks()
                        kotlinx.coroutines.delay(1000) // Give time for refresh
                        isRefreshing = false
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                // Loading State
                state.isLoading && state.links.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Error State
                state.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.error!!,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.refreshLinks() }) {
                            Text("Retry")
                        }
                    }
                }

                // Empty State
                state.links.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            modifier = Modifier.size(120.dp),
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "No links saved yet",
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Share links from other apps to save them here",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Content State - List of Links
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(
                            items = state.links,
                            key = { link -> link.id }
                        ) { link ->
                            LinkItem(
                                link = link,
                                onOpenLink = { linkToOpen ->
                                    // Show bottom sheet instead of opening directly
                                    selectedLinkForAction = linkToOpen
                                },
                                onToggleStar = { linkId ->
                                    viewModel.toggleStar(linkId)
                                },
                                onDeleteLink = { linkToDelete ->
                                    viewModel.deleteLink(linkToDelete)
                                },
                                isSelectionMode = state.isSelectionMode,
                                isSelected = state.selectedLinkIds.contains(link.id),
                                onLongClick = { clickedLink ->
                                    viewModel.enterSelectionMode(clickedLink.id)
                                },
                                onSelectionToggle = { linkId ->
                                    viewModel.toggleLinkSelection(linkId)
                                }
                            )
                        }
                    }
                }
            }
            }  // Closes PullToRefreshBox
        }
    }  // Closes Scaffold content lambda

    // Bottom Sheet for Link Actions
    selectedLinkForAction?.let { link ->
        LinkActionBottomSheet(
            link = link,
            onDismiss = { selectedLinkForAction = null },
            onOpenLink = {
                openLinkInBrowser(context, link.url)
                viewModel.markAsOpened(link.id)
            },
            onEditLink = {
                // Navigate to detail screen for editing
                onNavigateToDetail(link.id)
            },
            onShareLink = {
                shareLink(context, link.url, link.title, link.description)
            },
            onToggleRead = {
                if (link.isOpened) {
                    // Mark as unread - we need to add this to the ViewModel
                    // For now, we can just toggle (this is a simplification)
                    viewModel.markAsOpened(link.id)
                } else {
                    viewModel.markAsOpened(link.id)
                }
            },
            onToggleStar = {
                viewModel.toggleStar(link.id)
            },
            onToggleArchive = {
                viewModel.toggleArchive(link.id)
            },
            onDeleteLink = {
                viewModel.deleteLink(link)
            }
        )
    }
}

}

/**
 * Opens a URL in the default browser
 */
fun openLinkInBrowser(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // Handle case where no browser is available
        e.printStackTrace()
    }
}

/**
 * Shares a URL via the Android share sheet with title and description
 */
fun shareLink(context: Context, url: String, title: String? = null, description: String? = null) {
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
