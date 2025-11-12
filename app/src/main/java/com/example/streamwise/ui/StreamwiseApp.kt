package com.example.streamwise.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.example.streamwise.data.CONTENT_DATA
import com.example.streamwise.data.ContentItem
import com.example.streamwise.data.StreamwiseRepository
import com.example.streamwise.data.UsageEvent
import com.example.streamwise.data.toggle
import com.example.streamwise.ui.components.ProfileScreen
import com.example.streamwise.ui.components.RecommendationsTab
import com.example.streamwise.ui.components.SearchTab
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamwiseApp(onSignOut: () -> Unit) {
    val context = LocalContext.current
    val repository = remember {
        StreamwiseRepository(
            firestore = Firebase.firestore,
            appId = "streamwise",
            userId = Firebase.auth.currentUser?.uid ?: ""
        )
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Search", "Recs", "Profile")

    var searchTerm by remember { mutableStateOf("") }
    val searchResults = remember { mutableStateListOf<ContentItem>() }
    var recommendationsQuery by remember { mutableStateOf("") }
    var recommendationsResult by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val subscriptions by repository.getUserSubscriptions().collectAsState(initial = emptySet())
    val recentUsage by repository.getRecentUsage().collectAsState(initial = emptyList())

    val coroutineScope = rememberCoroutineScope()

    // Reverted to original search logic
    LaunchedEffect(searchTerm) {
        if (searchTerm.isBlank()) {
            searchResults.clear()
            searchResults.addAll(CONTENT_DATA)
        } else {
            searchResults.clear()
            searchResults.addAll(CONTENT_DATA.filter {
                it.title.contains(searchTerm, ignoreCase = true) ||
                        it.genre.contains(searchTerm, ignoreCase = true)
            })
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Streamwise AI", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = {
                            when (title) {
                                "Search" -> Icon(Icons.Filled.Search, contentDescription = title)
                                "Recs" -> Icon(Icons.Filled.Star, contentDescription = title)
                                "Profile" -> Icon(Icons.Filled.Person, contentDescription = title)
                            }
                        },
                        label = { Text(title) },
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index }
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (selectedTabIndex) {
                0 -> SearchTab(
                    searchTerm = searchTerm,
                    onSearchTermChange = { searchTerm = it },
                    searchResults = searchResults,
                    isLoading = isLoading,
                    repository = repository,
                    coroutineScope = coroutineScope
                )
                1 -> RecommendationsTab(
                    query = recommendationsQuery,
                    onQueryChange = { recommendationsQuery = it },
                    result = recommendationsResult,
                    isLoading = isLoading,
                    error = error,
                    onFetch = { /* ... */ }
                )
                2 -> ProfileScreen(
                    subscriptions = subscriptions,
                    recentUsage = recentUsage,
                    onToggleSubscription = { serviceId ->
                        val newSubscriptions = subscriptions.toggle(serviceId)
                        coroutineScope.launch { repository.saveUserSubscriptions(newSubscriptions) }
                    },
                    onSignOut = onSignOut
                )
            }
        }
    }
}
