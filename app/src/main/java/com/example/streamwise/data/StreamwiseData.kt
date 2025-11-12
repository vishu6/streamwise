package com.example.streamwise.data

import androidx.compose.ui.graphics.Color

// --- Data Structures (Mocked) ---

data class Service(val id: String, val name: String, val color: Color)
data class ContentItem(val title: String, val genre: String, val services: List<String>, val year: Int)

val STREAMING_SERVICES = listOf(
    Service("netflix", "Netflix", Color(0xFFDC2626)), // Red-600
    Service("max", "Max", Color(0xFF4338CA)),       // Indigo-700
    Service("disney+", "Disney+", Color(0xFF2563EB)), // Blue-600
    Service("hulu", "Hulu", Color(0xFF16A34A)),      // Green-600
    Service("prime", "Prime Video", Color(0xFF0EA5E9)), // Sky-500
    Service("apple+", "Apple TV+", Color(0xFF1F2937)),
    Service("peacock", "Peacock", Color(0xFFFBBF24)),
)

val CONTENT_DATA = listOf(
    ContentItem("The Expanse", "Sci-Fi/Action", listOf("prime"), 2015),
    ContentItem("Foundation", "Sci-Fi/Drama", listOf("max"), 2021),
    ContentItem("Parks and Recreation", "Comedy/Humor", listOf("peacock", "hulu"), 2009),
    ContentItem("Severance", "Sci-Fi/Thriller", listOf("apple+"), 2022),
    ContentItem("Our Planet", "Documentary", listOf("netflix"), 2019),
    ContentItem("Succession", "Drama/Comedy", listOf("max"), 2018),
    ContentItem("Arcane", "Animation/Action", listOf("netflix"), 2021),
    ContentItem("Only Murders in the Building", "Comedy/Mystery", listOf("hulu"), 2021),
)

// Extension function to handle set toggling (used for subscriptions)
fun Set<String>.toggle(id: String): Set<String> {
    return if (this.contains(id)) {
        this.minus(id)
    } else {
        this.plus(id)
    }
}
