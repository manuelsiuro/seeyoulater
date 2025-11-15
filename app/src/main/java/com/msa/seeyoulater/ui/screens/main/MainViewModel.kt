package com.msa.seeyoulater.ui.screens.main

import android.util.Log
import android.webkit.URLUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.seeyoulater.data.local.entity.Link
import com.msa.seeyoulater.data.local.entity.Tag
import com.msa.seeyoulater.data.local.entity.Collection
import com.msa.seeyoulater.data.repository.LinkRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SortOption { DATE_ADDED, LAST_OPENED }
enum class FilterOption { ALL, STARRED, ARCHIVED }

data class MainScreenState(
    val links: List<Link> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val recentlyDeletedLink: Link? = null,
    val searchQuery: String = "",
    val sortOption: SortOption = SortOption.DATE_ADDED,
    val filterOption: FilterOption = FilterOption.ALL,
    val isSelectionMode: Boolean = false,
    val selectedLinkIds: Set<Long> = emptySet(),
    val selectedTagFilter: Tag? = null,
    val selectedCollectionFilter: Collection? = null,
    val allTags: List<Tag> = emptyList(),
    val allCollections: List<Collection> = emptyList()
)

@FlowPreview
@ExperimentalCoroutinesApi
class MainViewModel(private val repository: LinkRepository) : ViewModel() {

    private val _state = MutableStateFlow(MainScreenState())
    val state: StateFlow<MainScreenState> = _state.asStateFlow()

    // StateFlows for sorting and filtering
    private val _sortOption = MutableStateFlow(SortOption.DATE_ADDED)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _filterOption = MutableStateFlow(FilterOption.ALL)
    val filterOption: StateFlow<FilterOption> = _filterOption.asStateFlow()

    private val _selectedTagFilter = MutableStateFlow<Tag?>(null)
    private val _selectedCollectionFilter = MutableStateFlow<Collection?>(null)

    init {
        observeLinks()
        observeAllTags()
        observeAllCollections()
    }

    private fun observeAllTags() {
        viewModelScope.launch {
            repository.getAllTags().collect { tags ->
                _state.update { it.copy(allTags = tags) }
            }
        }
    }

    private fun observeAllCollections() {
        viewModelScope.launch {
            repository.getAllCollections().collect { collections ->
                _state.update { it.copy(allCollections = collections) }
            }
        }
    }

    private fun observeLinks() {
        viewModelScope.launch {
            // Combine flows for search, sort, filter, tag filter, and collection filter
             combine(
                _state.map { it.searchQuery }.debounce(300), // Apply debounce to search query from state
                _sortOption,
                _filterOption,
                _selectedTagFilter,
                _selectedCollectionFilter
            ) { query, sort, filter, tagFilter, collectionFilter ->
                 CombinedFilters(query, sort, filter, tagFilter, collectionFilter)
            }
            .flatMapLatest { filters ->
                getFilteredAndSortedLinks(filters)
            }
            .onStart { _state.update { it.copy(isLoading = true) } }
            .catch { e ->
                Log.e("MainViewModel", "Error observing links", e)
                _state.update { it.copy(isLoading = false, error = "Failed to load links: ${e.localizedMessage}") }
             }
            .collect { links ->
                _state.update {
                    it.copy(
                        links = links,
                        isLoading = false,
                        error = null,
                        sortOption = _sortOption.value,
                        filterOption = _filterOption.value,
                        selectedTagFilter = _selectedTagFilter.value,
                        selectedCollectionFilter = _selectedCollectionFilter.value
                    )
                }
            }
        }
    }

    private data class CombinedFilters(
        val query: String,
        val sort: SortOption,
        val filter: FilterOption,
        val tagFilter: Tag?,
        val collectionFilter: Collection?
    )

     private fun getFilteredAndSortedLinks(filters: CombinedFilters): Flow<List<Link>> {
        // First, get the base flow based on collection/tag filter or standard filter
        val baseFlow: Flow<List<Link>> = when {
            filters.collectionFilter != null -> {
                // Filter by collection
                repository.getLinksInCollection(filters.collectionFilter.id)
            }
            filters.tagFilter != null -> {
                // Filter by tag - need to get link IDs first, then fetch links
                repository.getLinkIdsForTag(filters.tagFilter.id).flatMapLatest { linkIds ->
                    repository.getAllLinks().map { allLinks ->
                        allLinks.filter { link -> linkIds.contains(link.id) }
                    }
                }
            }
            else -> {
                // Standard filter (ALL, STARRED, or ARCHIVED)
                when (filters.filter) {
                    FilterOption.ALL -> when (filters.sort) {
                        SortOption.DATE_ADDED -> repository.getAllLinks()
                        SortOption.LAST_OPENED -> repository.getAllLinksSortedByLastOpened()
                    }
                    FilterOption.STARRED -> when (filters.sort) {
                        SortOption.DATE_ADDED -> repository.getStarredLinks()
                        SortOption.LAST_OPENED -> repository.getStarredLinksSortedByLastOpened()
                    }
                    FilterOption.ARCHIVED -> repository.getArchivedLinks()
                }
            }
        }

        return baseFlow.map { links ->
            var filteredLinks = links

            // Apply starred filter if tag/collection filter is active
            if ((filters.tagFilter != null || filters.collectionFilter != null) && filters.filter == FilterOption.STARRED) {
                filteredLinks = filteredLinks.filter { it.isStarred }
            }

            // Apply sort if tag/collection filter is active
            if (filters.tagFilter != null || filters.collectionFilter != null) {
                filteredLinks = when (filters.sort) {
                    SortOption.DATE_ADDED -> filteredLinks.sortedByDescending { it.addedTimestamp }
                    SortOption.LAST_OPENED -> filteredLinks.sortedBy { it.lastOpenedTimestamp }
                }
            }

            // Apply search query
            if (filters.query.isBlank()) {
                filteredLinks
            } else {
                filteredLinks.filter { link ->
                    link.url.contains(filters.query, ignoreCase = true) ||
                    link.title?.contains(filters.query, ignoreCase = true) == true ||
                    link.description?.contains(filters.query, ignoreCase = true) == true
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun onSortChanged(option: SortOption) {
        _sortOption.value = option
    }

     fun onFilterChanged(option: FilterOption) {
        _filterOption.value = option
    }

    fun onTagFilterSelected(tag: Tag?) {
        _selectedTagFilter.value = tag
        // Clear collection filter when tag filter is selected
        if (tag != null) {
            _selectedCollectionFilter.value = null
        }
    }

    fun onCollectionFilterSelected(collection: Collection?) {
        _selectedCollectionFilter.value = collection
        // Clear tag filter when collection filter is selected
        if (collection != null) {
            _selectedTagFilter.value = null
        }
    }

    fun clearFilters() {
        _selectedTagFilter.value = null
        _selectedCollectionFilter.value = null
    }


    fun toggleStar(linkId: Long) {
        viewModelScope.launch {
            try {
                repository.toggleStarStatus(linkId)
            } catch (e: Exception) {
                 Log.e("MainViewModel", "Error toggling star for $linkId", e)
                // Handle error (e.g., show a Snackbar)
            }
        }
    }

    fun toggleArchive(linkId: Long) {
        viewModelScope.launch {
            try {
                repository.toggleArchiveStatus(linkId)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error toggling archive for $linkId", e)
                _state.update { it.copy(error = "Failed to archive/unarchive link.") }
            }
        }
    }

     fun markAsOpened(linkId: Long) {
        viewModelScope.launch {
             try {
                repository.markLinkAsOpened(linkId)
             } catch (e: Exception) {
                 Log.e("MainViewModel", "Error marking link $linkId as opened", e)
             }
        }
    }

    fun deleteLink(link: Link) {
        viewModelScope.launch {
            try {
                repository.deleteLink(link.id)
                // Set recently deleted for potential Undo action
                 _state.update { it.copy(recentlyDeletedLink = link) }
             } catch (e: Exception) {
                 Log.e("MainViewModel", "Error deleting link ${link.id}", e)
                // Handle error
                _state.update { it.copy(error = "Failed to delete link.") }
            }
        }
    }

     fun undoDelete() {
         val linkToRestore = _state.value.recentlyDeletedLink
         if (linkToRestore != null) {
             viewModelScope.launch {
                 try {
                     // Re-insert the link (or update if ID is preserved, depends on repo impl)
                     // Simple re-insert:
                     repository.updateLink(linkToRestore) // Assuming update works like insertOrReplace
                     // Clear the recently deleted state
                     _state.update { it.copy(recentlyDeletedLink = null) }
                 } catch (e: Exception) {
                     Log.e("MainViewModel", "Error undoing delete for ${linkToRestore.id}", e)
                     _state.update { it.copy(error = "Failed to undo deletion.", recentlyDeletedLink = null) }
                 }
             }
         }
    }

     fun clearUndoState() {
        // Call this after the Snackbar duration for Undo expires
        _state.update { it.copy(recentlyDeletedLink = null) }
    }

     fun refreshLinks() {
        // Force re-fetch or re-trigger the flow observation
        // One way is to just re-trigger the combineLatest by slightly changing a dependent state
         // _sortOption.value = _sortOption.value // This might not always re-trigger depending on distinctUntilChanged
         // A more reliable way might be needed if flows don't update automatically
         // For now, let's assume the flows handle updates correctly.
          _state.update { it.copy(isLoading = true) } // Show loading indicator during manual refresh
         // The existing combine flow should re-emit when the underlying data changes or dependencies update
         // If preview fetching is manual, trigger it here
         // viewModelScope.launch { _state.value.links.forEach { repository.fetchAndUpdateLinkPreview(it.id) } }
    }

     // Trigger background preview fetch for all links that might need it
    fun fetchPreviewsIfNeeded() {
        viewModelScope.launch {
            _state.value.links.forEach { link ->
                if (link.title.isNullOrBlank() || link.previewImageUrl.isNullOrBlank()) {
                    // Launch non-blocking background task via repository
                     // repository.fetchAndUpdateLinkPreview(link.id)
                     // Note: RepositoryImpl already launches this on save.
                     // This might be for a manual refresh or initial load scenario.
                }
            }
        }
    }

    // Selection mode functions
    fun enterSelectionMode(linkId: Long) {
        _state.update {
            it.copy(
                isSelectionMode = true,
                selectedLinkIds = setOf(linkId)
            )
        }
    }

    fun exitSelectionMode() {
        _state.update {
            it.copy(
                isSelectionMode = false,
                selectedLinkIds = emptySet()
            )
        }
    }

    fun toggleLinkSelection(linkId: Long) {
        _state.update { currentState ->
            val newSelection = if (currentState.selectedLinkIds.contains(linkId)) {
                currentState.selectedLinkIds - linkId
            } else {
                currentState.selectedLinkIds + linkId
            }

            // Exit selection mode if no items selected
            if (newSelection.isEmpty()) {
                currentState.copy(
                    isSelectionMode = false,
                    selectedLinkIds = emptySet()
                )
            } else {
                currentState.copy(selectedLinkIds = newSelection)
            }
        }
    }

    fun selectAll() {
        _state.update {
            it.copy(selectedLinkIds = it.links.map { link -> link.id }.toSet())
        }
    }

    fun clearSelection() {
        exitSelectionMode()
    }

    fun bulkDeleteSelected() {
        val selectedIds = _state.value.selectedLinkIds
        if (selectedIds.isEmpty()) return

        viewModelScope.launch {
            try {
                selectedIds.forEach { linkId ->
                    repository.deleteLink(linkId)
                }
                exitSelectionMode()
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error bulk deleting links", e)
                _state.update { it.copy(error = "Failed to delete selected links.") }
            }
        }
    }

    fun bulkToggleStarSelected() {
        val selectedIds = _state.value.selectedLinkIds
        if (selectedIds.isEmpty()) return

        viewModelScope.launch {
            try {
                selectedIds.forEach { linkId ->
                    repository.toggleStarStatus(linkId)
                }
                exitSelectionMode()
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error bulk starring links", e)
                _state.update { it.copy(error = "Failed to star selected links.") }
            }
        }
    }

    fun bulkArchiveSelected() {
        val selectedIds = _state.value.selectedLinkIds
        if (selectedIds.isEmpty()) return

        viewModelScope.launch {
            try {
                selectedIds.forEach { linkId ->
                    repository.archiveLink(linkId)
                }
                exitSelectionMode()
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error bulk archiving links", e)
                _state.update { it.copy(error = "Failed to archive selected links.") }
            }
        }
    }

    fun batchImportUrls(urlText: String) {
        viewModelScope.launch {
            try {
                val urls = urlText.lines()
                    .map { it.trim() }
                    .filter { it.isNotBlank() && URLUtil.isValidUrl(it) }
                    .distinct()

                var imported = 0
                var skipped = 0

                urls.forEach { url ->
                    // Check if URL already exists
                    val exists = repository.isUrlSaved(url)
                    if (!exists) {
                        repository.saveLink(url, null)
                        imported++
                    } else {
                        skipped++
                    }
                }

                val message = buildString {
                    append("Imported $imported link${if (imported != 1) "s" else ""}")
                    if (skipped > 0) {
                        append(", skipped $skipped duplicate${if (skipped != 1) "s" else ""}")
                    }
                }

                _state.update { it.copy(error = message) } // Using error field for success message
                Log.i("MainViewModel", message)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error batch importing URLs", e)
                _state.update { it.copy(error = "Failed to import URLs: ${e.localizedMessage}") }
            }
        }
    }
}
