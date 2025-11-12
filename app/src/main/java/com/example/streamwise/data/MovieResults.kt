package com.example.streamwise.data

/**
 * Represents a search result from a movie API.
 */
data class MovieResult(
    val id1: Int, // Unique ID from TMDB or similar API
    val id: String,
    val title: String,
    val overview: String,
    val releaseDate: String?
)

/**
 * Represents an item stored in the user's Firestore watchlist.
 */
data class UserWatchItem(
    val firestoreId: String = "", // Document ID in Firestore
    val movieId: String = "", // ID linking to MovieResult data
    val title: String = "",
    val status: WatchStatus = WatchStatus.TO_WATCH
)

enum class WatchStatus {
    TO_WATCH,
    WATCHING,
    WATCHED
}
