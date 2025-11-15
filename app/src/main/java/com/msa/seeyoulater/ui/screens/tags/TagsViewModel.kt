package com.msa.seeyoulater.ui.screens.tags

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.seeyoulater.data.local.entity.Tag
import com.msa.seeyoulater.data.repository.LinkRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TagsScreenState(
    val tags: List<Tag> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val showCreateDialog: Boolean = false,
    val editingTag: Tag? = null
)

class TagsViewModel(
    private val repository: LinkRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TagsScreenState())
    val state: StateFlow<TagsScreenState> = _state.asStateFlow()

    init {
        loadTags()
    }

    private fun loadTags() {
        viewModelScope.launch {
            repository.getTagsByUsage().collect { tags ->
                _state.update {
                    it.copy(
                        tags = tags,
                        isLoading = false,
                        error = null
                    )
                }
            }
        }
    }

    fun showCreateDialog() {
        _state.update { it.copy(showCreateDialog = true, editingTag = null) }
    }

    fun showEditDialog(tag: Tag) {
        _state.update { it.copy(showCreateDialog = true, editingTag = tag) }
    }

    fun hideDialog() {
        _state.update { it.copy(showCreateDialog = false, editingTag = null) }
    }

    fun createTag(name: String, color: String?) {
        viewModelScope.launch {
            try {
                repository.createTag(name = name, color = color)
                hideDialog()
            } catch (e: Exception) {
                Log.e("TagsViewModel", "Error creating tag", e)
                _state.update {
                    it.copy(error = "Failed to create tag: ${e.localizedMessage}")
                }
            }
        }
    }

    fun updateTag(tag: Tag) {
        viewModelScope.launch {
            try {
                repository.updateTag(tag)
                hideDialog()
            } catch (e: Exception) {
                Log.e("TagsViewModel", "Error updating tag", e)
                _state.update {
                    it.copy(error = "Failed to update tag: ${e.localizedMessage}")
                }
            }
        }
    }

    fun deleteTag(tagId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteTag(tagId)
            } catch (e: Exception) {
                Log.e("TagsViewModel", "Error deleting tag", e)
                _state.update {
                    it.copy(error = "Failed to delete tag: ${e.localizedMessage}")
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
