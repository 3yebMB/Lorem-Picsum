package dev.m13d.lorempicsum.features.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.m13d.lorempicsum.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.m13d.lorempicsum.data.PicsumPhoto
import dev.m13d.lorempicsum.data.PicsumRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class PicsumGalleryViewModel @Inject constructor(
    private val repository: PicsumRepository
) : ViewModel() {

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    private val refreshTriggerChannel = Channel<Refresh>()
    private val refreshTrigger = refreshTriggerChannel.receiveAsFlow()

    var pendingScrollToTopAfterRefresh = false

    val picsumPhotos = refreshTrigger.flatMapLatest { refresh ->
        repository.getPicsumPhotos(
            refresh == Refresh.FORCE,
            onFetchSuccess = {
                pendingScrollToTopAfterRefresh = true
            },
            onFetchFailed = { t ->
                viewModelScope.launch { eventChannel.send(Event.ShowErrorMessage(t)) }
            }
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    init {
        viewModelScope.launch {
            repository.deleteNonBookmarkedPhotosOlderThan(
                System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
            )
        }
    }

    fun onStart() {
        if (picsumPhotos.value !is Resource.Loading) {
            viewModelScope.launch {
                refreshTriggerChannel.send(Refresh.NORMAL)
            }
        }
    }

    fun onManualRefresh() {
        if (picsumPhotos.value !is Resource.Loading) {
            viewModelScope.launch {
                refreshTriggerChannel.send(Refresh.FORCE)
            }
        }
    }

    fun onBookmarkClick(picsumPhoto: PicsumPhoto) {
        val currentlyBookmarked = picsumPhoto.isBookmarked
        val updatedPhoto = picsumPhoto.copy(isBookmarked = !currentlyBookmarked)
        viewModelScope.launch {
            repository.updatePhoto(updatedPhoto)
        }
    }

    enum class Refresh {
        FORCE, NORMAL
    }

    sealed class Event {
        data class ShowErrorMessage(val error: Throwable) : Event()
    }
}