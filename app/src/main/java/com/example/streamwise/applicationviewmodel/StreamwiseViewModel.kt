package com.example.streamwise.applicationviewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streamwise.data.StreamwiseRepository
import com.example.streamwise.data.UserWatchItem
import com.example.streamwise.data.WatchStatus
import com.example.streamwise.data.WatchmodeTitle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Represents the entire UI state for the application.
 */
data class StreamwiseUiState(
    // Watchlist data from Firestore (Reactive)
    val watchlist: List<UserWatchItem> = emptyList(),
    val isWatchlistLoading: Boolean = true,
    val watchlistError: String? = null,

    // Search results from Watchmode API
    val searchResults: List<WatchmodeTitle> = emptyList(),
    val searchTerm: String = "",
    val isSearching: Boolean = false,
    val searchError: String? = null,

    // Subscriptions data from Firestore
    val currentSubscriptions: Set<String> = emptySet()
)

class StreamwiseViewModel(
    private val repository: StreamwiseRepository
) : ViewModel() {

    // --- State Management ---
    private val _uiState = MutableStateFlow(StreamwiseUiState())
    val uiState: StateFlow<StreamwiseUiState> = _uiState.asStateFlow()

    init {
        getWatchListUpdates()
    }

    // --- Firestore Operations (Watchlist) ---

    private fun getWatchListUpdates() {
        viewModelScope.launch {
            repository.getWatchList().collect { watchlist ->
                _uiState.value = _uiState.value.copy(
                    watchlist = watchlist,
                    isWatchlistLoading = false,
                    watchlistError = null
                )
            }
        }
    }

    fun addWatchItem(title: WatchmodeTitle) {
        viewModelScope.launch {
            val newItem = UserWatchItem(
                movieId = title.id.toString(), // Use the API's ID
                title = title.name, // Use the API's name
                status = WatchStatus.TO_WATCH
            )
            try {
                repository.addWatchItem(newItem)
                _uiState.value = _uiState.value.copy(searchResults = emptyList(), searchTerm = "")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(watchlistError = "Failed to add item: ${e.message}")
            }
        }
    }

    fun updateWatchItemStatus(firestoreId: String, newStatus: WatchStatus) {
        viewModelScope.launch {
            try {
                repository.updateWatchItemStatus(firestoreId, newStatus)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(watchlistError = "Failed to update status: ${e.message}")
            }
        }
    }

    fun deleteWatchItem(firestoreId: String) {
        viewModelScope.launch {
            try {
                repository.deleteWatchItem(firestoreId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(watchlistError = "Failed to delete item: ${e.message}")
            }
        }
    }

    // --- API Search Operations ---

    fun updateSearchTerm(newTerm: String) {
        _uiState.value = _uiState.value.copy(searchTerm = newTerm)
    }

    fun searchMovies() {
        if (_uiState.value.searchTerm.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, searchError = null)
            try {
                val results = repository.searchMovies(_uiState.value.searchTerm)
                _uiState.value = _uiState.value.copy(searchResults = results)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(searchError = "Search failed: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isSearching = false)
            }
        }
    }
}
