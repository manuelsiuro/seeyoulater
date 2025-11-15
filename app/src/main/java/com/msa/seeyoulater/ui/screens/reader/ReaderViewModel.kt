package com.msa.seeyoulater.ui.screens.reader

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.seeyoulater.data.local.entity.Link
import com.msa.seeyoulater.data.repository.LinkRepository
import com.msa.seeyoulater.data.services.ContentExtractor
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ReaderState(
    val link: Link? = null,
    val isLoading: Boolean = true,
    val isFetchingContent: Boolean = false,
    val error: String? = null,
    val fontSize: Int = 16, // Default font size in SP
    val readingProgress: Float = 0f // 0.0 to 1.0
)

class ReaderViewModel(
    private val repository: LinkRepository,
    private val linkId: Long
) : ViewModel() {

    private val _state = MutableStateFlow(ReaderState())
    val state: StateFlow<ReaderState> = _state.asStateFlow()

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
                            readingProgress = link.readingProgress,
                            isLoading = false,
                            error = null
                        )
                    }

                    // If content is not saved, fetch it
                    if (link.savedContent.isNullOrBlank()) {
                        fetchAndSaveContent()
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
                Log.e("ReaderViewModel", "Error loading link $linkId", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load link: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun fetchAndSaveContent() {
        val currentLink = _state.value.link ?: return

        viewModelScope.launch {
            _state.update { it.copy(isFetchingContent = true) }

            try {
                val extractedContent = ContentExtractor.extractFromUrl(currentLink.url)

                if (extractedContent != null) {
                    // Save the content to the database
                    repository.saveArticleContent(
                        linkId = linkId,
                        content = extractedContent.content,
                        estimatedReadingTime = extractedContent.estimatedReadingTime
                    )

                    // Reload the link to get updated content
                    val updatedLink = repository.getLinkById(linkId)
                    _state.update {
                        it.copy(
                            link = updatedLink,
                            isFetchingContent = false,
                            error = null
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isFetchingContent = false,
                            error = "Failed to extract article content"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("ReaderViewModel", "Error fetching content", e)
                _state.update {
                    it.copy(
                        isFetchingContent = false,
                        error = "Failed to fetch content: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun updateReadingProgress(progress: Float) {
        _state.update { it.copy(readingProgress = progress) }

        viewModelScope.launch {
            try {
                repository.updateReadingProgress(linkId, progress)
            } catch (e: Exception) {
                Log.e("ReaderViewModel", "Error updating reading progress", e)
            }
        }
    }

    fun increaseFontSize() {
        val currentSize = _state.value.fontSize
        if (currentSize < 24) {
            _state.update { it.copy(fontSize = currentSize + 2) }
        }
    }

    fun decreaseFontSize() {
        val currentSize = _state.value.fontSize
        if (currentSize > 12) {
            _state.update { it.copy(fontSize = currentSize - 2) }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
