package com.example.streamwise.data

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.UUID

data class UsageEvent(val serviceId: String = "", val timestamp: Timestamp? = null)

class StreamwiseRepository(
    private val firestore: FirebaseFirestore,
    private val appId: String,
    private val userId: String
) {

    private fun getWatchlistCollectionRef() = firestore.collection("artifacts").document(appId).collection("users").document(userId).collection("watchlist")
    private fun getUserSettingsDocRef() = firestore.collection("artifacts").document(appId).collection("users").document(userId).collection("settings").document("subscriptions")
    private fun getUsageEventsCollectionRef() = firestore.collection("artifacts").document(appId).collection("users").document(userId).collection("usageEvents")

    // --- Watchlist Operations ---
    fun getWatchList(): Flow<List<UserWatchItem>> = callbackFlow {
        val collectionRef = getWatchlistCollectionRef()
        val subscription = collectionRef
            .orderBy("title")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("StreamwiseRepo", "Watchlist listener failed.", error)
                    close(error)
                    return@addSnapshotListener
                }
                val watchlist = snapshot?.documents?.mapNotNull { it.toObject<UserWatchItem>()?.copy(firestoreId = it.id) } ?: emptyList()
                trySend(watchlist)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addWatchItem(item: UserWatchItem) {
        getWatchlistCollectionRef().add(item.copy(firestoreId = UUID.randomUUID().toString())).await()
    }

    suspend fun updateWatchItemStatus(firestoreId: String, newStatus: WatchStatus) {
        getWatchlistCollectionRef().document(firestoreId).update("status", newStatus.name).await()
    }

    suspend fun deleteWatchItem(firestoreId: String) {
        getWatchlistCollectionRef().document(firestoreId).delete().await()
    }

    // --- User Subscription Operations ---
    fun getUserSubscriptions(): Flow<Set<String>> = callbackFlow {
        val docRef = getUserSettingsDocRef()
        val subscription = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val serviceIds = snapshot?.get("serviceIds") as? List<String> ?: emptyList()
            trySend(serviceIds.toSet())
        }
        awaitClose { subscription.remove() }
    }

    suspend fun saveUserSubscriptions(subscriptions: Set<String>) {
        getUserSettingsDocRef().set(mapOf("serviceIds" to subscriptions.toList())).await()
    }

    // --- Usage Tracking Operations ---
    suspend fun logUsageEvent(serviceId: String) {
        val event = mapOf("serviceId" to serviceId, "timestamp" to FieldValue.serverTimestamp())
        getUsageEventsCollectionRef().add(event).await()
    }

    fun getRecentUsage(): Flow<List<UsageEvent>> = callbackFlow {
        val ninetyDaysAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -90) }.time
        val query = getUsageEventsCollectionRef().whereGreaterThanOrEqualTo("timestamp", ninetyDaysAgo)
        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val events = snapshot?.documents?.mapNotNull { it.toObject<UsageEvent>() } ?: emptyList()
            trySend(events)
        }
        awaitClose { subscription.remove() }
    }

    // --- External Movie API Operations ---
    suspend fun searchMovies(query: String): List<WatchmodeTitle> = coroutineScope {
        try {
            val searchResponse = WatchmodeApi.service.search(searchValue = query)
            val titles = searchResponse.titleResults

            val enrichedTitles = titles.map { title ->
                async {
                    try {
                        val sources = WatchmodeApi.service.getTitleSources(title.id)
                        // Normalize the data before returning it
                        val normalizedSources = sources.map { source ->
                            if (source.name == "Hotstar") {
                                source.copy(name = "Disney+")
                            } else {
                                source
                            }
                        }
                        title.copy(sources = normalizedSources)
                    } catch (e: Exception) {
                        Log.e("StreamwiseRepo", "Failed to fetch sources for title ${title.id}: ${e.message}")
                        title
                    }
                }
            }.awaitAll()

            return@coroutineScope enrichedTitles

        } catch (e: Exception) {
            Log.e("StreamwiseRepo", "Watchmode API search failed: ${e.message}", e)
            return@coroutineScope emptyList()
        }
    }
}
