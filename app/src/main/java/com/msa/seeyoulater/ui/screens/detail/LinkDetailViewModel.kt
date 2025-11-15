package com.msa.seeyoulater.ui.screens.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.seeyoulater.data.local.entity.Link
import com.msa.seeyoulater.data.local.entity.Tag
import com.msa.seeyoulater.data.local.entity.Collection
import com.msa.seeyoulater.data.repository.LinkRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LinkDetailState(
    val link: Link? = null,
    val tags: List<Tag> = emptyList(),
    val collections: List<Collection> = emptyList(),
    val allTags: List<Tag> = emptyList(),
    val allCollections: List<Collection> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
)

class LinkDetailViewModel(
    private val repository: LinkRepository,
    private val linkId: Long
) : ViewModel() {

    private val _state = MutableStateFlow(LinkDetailState())
    val state: StateFlow<LinkDetailState> = _state.asStateFlow()

    init {
        loadLink()
        loadTags()
        loadCollections()
        loadAllTags()
        loadAllCollections()
    }

    private fun loadLink() {
        viewModelScope.launch {
            try {
                val link = repository.getLinkById(linkId)
                if (link != null) {
                    _state.update {
                        it.copy(
                            link = link,
                            isLoading = false,
                            error = null
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Link not found"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("LinkDetailViewModel", "Error loading link $linkId", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load link: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    private fun loadTags() {
        viewModelScope.launch {
            repository.getTagsForLink(linkId).collect { tags ->
                _state.update { it.copy(tags = tags) }
            }
        }
    }

    private fun loadCollections() {
        viewModelScope.launch {
            repository.getCollectionsForLink(linkId).collect { collections ->
                _state.update { it.copy(collections = collections) }
            }
        }
    }

    private fun loadAllTags() {
        viewModelScope.launch {
            repository.getAllTags().collect { allTags ->
                _state.update { it.copy(allTags = allTags) }
            }
        }
    }

    private fun loadAllCollections() {
        viewModelScope.launch {
            repository.getAllCollections().collect { allCollections ->
                _state.update { it.copy(allCollections = allCollections) }
            }
        }
    }

    fun updateTitle(newTitle: String) {
        _state.update { currentState ->
            currentState.link?.let { link ->
                currentState.copy(link = link.copy(title = newTitle.ifBlank { null }))
            } ?: currentState
        }
    }

    fun updateDescription(newDescription: String) {
        _state.update { currentState ->
            currentState.link?.let { link ->
                currentState.copy(link = link.copy(description = newDescription.ifBlank { null }))
            } ?: currentState
        }
    }

    fun updateNotes(newNotes: String) {
        _state.update { currentState ->
            currentState.link?.let { link ->
                currentState.copy(
                    link = link.copy(
                        notes = newNotes.ifBlank { null },
                        notesLastModified = if (newNotes.isNotBlank()) System.currentTimeMillis() else null
                    )
                )
            } ?: currentState
        }
    }

    fun saveChanges(onSaveComplete: () -> Unit) {
        val currentLink = _state.value.link ?: return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            try {
                repository.updateLink(currentLink)
                _state.update {
                    it.copy(
                        isSaving = false,
                        saveSuccess = true
                    )
                }
                onSaveComplete()
            } catch (e: Exception) {
                Log.e("LinkDetailViewModel", "Error saving link", e)
                _state.update {
                    it.copy(
                        isSaving = false,
                        error = "Failed to save changes: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun toggleStar() {
        viewModelScope.launch {
            try {
                repository.toggleStarStatus(linkId)
            } catch (e: Exception) {
                Log.e("LinkDetailViewModel", "Error toggling star", e)
                _state.update {
                    it.copy(error = "Failed to toggle star")
                }
            }
        }
    }

    fun refreshPreview() {
        viewModelScope.launch {
            try {
                repository.fetchAndUpdateLinkPreview(linkId)
            } catch (e: Exception) {
                Log.e("LinkDetailViewModel", "Error refreshing preview", e)
                _state.update {
                    it.copy(error = "Failed to refresh preview")
                }
            }
        }
    }

    fun deleteLink(onDeleteComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteLink(linkId)
                onDeleteComplete()
            } catch (e: Exception) {
                Log.e("LinkDetailViewModel", "Error deleting link", e)
                _state.update {
                    it.copy(error = "Failed to delete link: ${e.localizedMessage}")
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    // ==================== Tag Operations ====================

    fun updateTags(newTags: List<Tag>) {
        viewModelScope.launch {
            try {
                // Get current tag IDs
                val currentTagIds = _state.value.tags.map { it.id }.toSet()
                val newTagIds = newTags.map { it.id }.toSet()

                // Tags to add (new tags without ID need to be created first)
                newTags.forEach { tag ->
                    if (tag.id == 0L) {
                        // New tag - needs to be created
                        repository.addTagToLink(linkId, tag.name, tag.color)
                    } else if (!currentTagIds.contains(tag.id)) {
                        // Existing tag - just add the relationship
                        repository.addTagToLink(linkId, tag.name, tag.color)
                    }
                }

                // Tags to remove
                _state.value.tags.forEach { tag ->
                    if (!newTagIds.contains(tag.id)) {
                        repository.removeTagFromLink(linkId, tag.id)
                    }
                }

                // State will be updated automatically through loadTags() flow
            } catch (e: Exception) {
                Log.e("LinkDetailViewModel", "Error updating tags", e)
                _state.update {
                    it.copy(error = "Failed to update tags: ${e.localizedMessage}")
                }
            }
        }
    }

    // ==================== Collection Operations ====================

    fun updateCollections(newCollections: List<Collection>) {
        viewModelScope.launch {
            try {
                // Get current collection IDs
                val currentCollectionIds = _state.value.collections.map { it.id }.toSet()
                val newCollectionIds = newCollections.map { it.id }.toSet()

                // Collections to add
                newCollections.forEach { collection ->
                    if (!currentCollectionIds.contains(collection.id)) {
                        repository.addLinkToCollection(linkId, collection.id)
                    }
                }

                // Collections to remove
                _state.value.collections.forEach { collection ->
                    if (!newCollectionIds.contains(collection.id)) {
                        repository.removeLinkFromCollection(linkId, collection.id)
                    }
                }

                // State will be updated automatically through loadCollections() flow
            } catch (e: Exception) {
                Log.e("LinkDetailViewModel", "Error updating collections", e)
                _state.update {
                    it.copy(error = "Failed to update collections: ${e.localizedMessage}")
                }
            }
        }
    }
}
