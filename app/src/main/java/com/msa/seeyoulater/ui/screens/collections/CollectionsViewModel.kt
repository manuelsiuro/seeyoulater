package com.msa.seeyoulater.ui.screens.collections

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.seeyoulater.data.local.entity.Collection
import com.msa.seeyoulater.data.repository.LinkRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CollectionsScreenState(
    val collections: List<Collection> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val showCreateDialog: Boolean = false,
    val editingCollection: Collection? = null
)

class CollectionsViewModel(
    private val repository: LinkRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CollectionsScreenState())
    val state: StateFlow<CollectionsScreenState> = _state.asStateFlow()

    init {
        loadCollections()
    }

    private fun loadCollections() {
        viewModelScope.launch {
            repository.getAllCollections().collect { collections ->
                _state.update {
                    it.copy(
                        collections = collections,
                        isLoading = false,
                        error = null
                    )
                }
            }
        }
    }

    fun showCreateDialog() {
        _state.update { it.copy(showCreateDialog = true, editingCollection = null) }
    }

    fun showEditDialog(collection: Collection) {
        _state.update { it.copy(showCreateDialog = true, editingCollection = collection) }
    }

    fun hideDialog() {
        _state.update { it.copy(showCreateDialog = false, editingCollection = null) }
    }

    fun createCollection(
        name: String,
        description: String?,
        icon: String?,
        color: String?
    ) {
        viewModelScope.launch {
            try {
                repository.createCollection(
                    name = name,
                    description = description,
                    icon = icon,
                    color = color
                )
                hideDialog()
            } catch (e: Exception) {
                Log.e("CollectionsViewModel", "Error creating collection", e)
                _state.update {
                    it.copy(error = "Failed to create collection: ${e.localizedMessage}")
                }
            }
        }
    }

    fun updateCollection(collection: Collection) {
        viewModelScope.launch {
            try {
                repository.updateCollection(collection)
                hideDialog()
            } catch (e: Exception) {
                Log.e("CollectionsViewModel", "Error updating collection", e)
                _state.update {
                    it.copy(error = "Failed to update collection: ${e.localizedMessage}")
                }
            }
        }
    }

    fun deleteCollection(collectionId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteCollection(collectionId)
            } catch (e: Exception) {
                Log.e("CollectionsViewModel", "Error deleting collection", e)
                _state.update {
                    it.copy(error = "Failed to delete collection: ${e.localizedMessage}")
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
