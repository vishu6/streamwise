package com.example.streamwise.applicationviewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streamwise.data.MovieResult
import com.example.streamwise.data.StreamwiseRepository
import com.example.streamwise.data.UserWatchItem
import com.example.streamwise.data.WatchStatus
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

    // Search results (Transient)
    val searchResults: List<MovieResult> = emptyList(),
    val searchTerm: String = "",
    val isSearching: Boolean = false,
    val searchError: String? = null,

    // Subscriptions data from Firestore (Placeholder, will be integrated later)
    val currentSubscriptions: Set<String> = emptySet()
)

class StreamwiseViewModel(
    private val repository: StreamwiseRepository
) : ViewModel() {

    // --- State Management ---
    // MutableStateFlow holds the current state, and StateFlow exposes it to the UI safely.
    private val _uiState = MutableStateFlow(StreamwiseUiState())
    val uiState: StateFlow<StreamwiseUiState> = _uiState.asStateFlow()

    init {
        // Start listening to the Firestore Watchlist immediately upon initialization
        getWatchListUpdates()
    }

    // --- Firestore Operations (Watchlist) ---

    private fun getWatchListUpdates() {
        viewModelScope.launch {
            // Collects items emitted by the repository's real-time listener
            repository.getWatchList().collect { watchlist ->
                _uiState.value = _uiState.value.copy(
                    watchlist = watchlist,
                    isWatchlistLoading = false,
                    watchlistError = null
                )
            }
        }
    }

    fun addWatchItem(movie: MovieResult) {
        viewModelScope.launch {
            val newItem = UserWatchItem(
                movieId = movie.id,
                title = movie.title,
                status = WatchStatus.TO_WATCH
            )
            try {
                // The repository adds to Firestore, and the Flow listener automatically updates the UI state
                repository.addWatchItem(newItem)
                // Clear search results after adding an item to encourage a new search
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

    // --- API Search Operations (External Data) ---

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
