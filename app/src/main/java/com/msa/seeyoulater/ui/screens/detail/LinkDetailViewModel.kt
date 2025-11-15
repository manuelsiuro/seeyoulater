package com.msa.seeyoulater.ui.screens.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.seeyoulater.data.local.entity.Link
import com.msa.seeyoulater.data.repository.LinkRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LinkDetailState(
    val link: Link? = null,
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
}
