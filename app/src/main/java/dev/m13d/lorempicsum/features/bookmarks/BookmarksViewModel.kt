package dev.m13d.lorempicsum.features.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.m13d.lorempicsum.data.PicsumPhoto
import dev.m13d.lorempicsum.data.PicsumRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val repository: PicsumRepository
) : ViewModel() {

    val bookmarks = repository.getAllBookmarkedPhotos()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun onBookmarkClick(photo: PicsumPhoto) {
        val currentlyBookmarked = photo.isBookmarked
        val updatedPhoto = photo.copy(isBookmarked = !currentlyBookmarked)
        viewModelScope.launch {
            repository.updatePhoto(updatedPhoto)
        }
    }

    fun onDeleteAllBookmarks() {
        viewModelScope.launch {
            repository.resetAllBookmarks()
        }
    }
}