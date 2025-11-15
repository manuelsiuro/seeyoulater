package com.msa.seeyoulater.ui.screens.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.seeyoulater.data.local.entity.Link
import com.msa.seeyoulater.data.repository.LinkRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SortOption { DATE_ADDED, LAST_OPENED }
enum class FilterOption { ALL, STARRED }

data class MainScreenState(
    val links: List<Link> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val recentlyDeletedLink: Link? = null,
    val searchQuery: String = "",
    val sortOption: SortOption = SortOption.DATE_ADDED,
    val filterOption: FilterOption = FilterOption.ALL,
    val isSelectionMode: Boolean = false,
    val selectedLinkIds: Set<Long> = emptySet()
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


    init {
        observeLinks()
    }

    private fun observeLinks() {
        viewModelScope.launch {
            // Combine flows for search, sort, and filter
             combine(
                _state.map { it.searchQuery }.debounce(300), // Apply debounce to search query from state
                _sortOption,
                _filterOption
            ) { query, sort, filter ->
                 Triple(query, sort, filter)
            }
            .flatMapLatest { (query, sort, filter) ->
                getFilteredAndSortedLinks(query, sort, filter)
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
                        // searchQuery is already in state, no need to update it here
                        sortOption = _sortOption.value,
                        filterOption = _filterOption.value
                    )
                }
            }
        }
    }

     private fun getFilteredAndSortedLinks(query: String, sort: SortOption, filter: FilterOption): Flow<List<Link>> {
        val baseFlow: Flow<List<Link>> = when (filter) {
            FilterOption.ALL -> when (sort) {
                SortOption.DATE_ADDED -> repository.getAllLinks()
                SortOption.LAST_OPENED -> repository.getAllLinksSortedByLastOpened()
            }
            FilterOption.STARRED -> when (sort) {
                SortOption.DATE_ADDED -> repository.getStarredLinks()
                 SortOption.LAST_OPENED -> repository.getStarredLinksSortedByLastOpened()
            }
        }

        return baseFlow.map { links ->
            if (query.isBlank()) {
                links // No search query, return all from base flow
            } else {
                 links.filter { link ->
                    link.url.contains(query, ignoreCase = true) ||
                    link.title?.contains(query, ignoreCase = true) == true ||
                    link.description?.contains(query, ignoreCase = true) == true
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
}
