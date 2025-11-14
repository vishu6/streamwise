package com.example.streamwise.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.streamwise.data.StreamwiseRepository
import com.example.streamwise.data.WatchmodeSource
import com.example.streamwise.data.WatchmodeTitle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun SearchTab(
    searchTerm: String,
    onSearchTermChange: (String) -> Unit,
    searchResults: List<WatchmodeTitle>,
    isLoading: Boolean,
    repository: StreamwiseRepository,
    coroutineScope: CoroutineScope
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            TextField(
                value = searchTerm,
                onValueChange = onSearchTermChange,
                placeholder = { Text("Search for movies...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search Icon") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(searchResults) { title ->
                    WatchmodeResultCard(title = title) {
                        coroutineScope.launch {
                            repository.logUsageEvent(it.sourceId.toString())
                        }
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.webUrl))
                        context.startActivity(intent)
                    }
                }
            }
        }
    }
}

@Composable
fun WatchmodeResultCard(title: WatchmodeTitle, onSourceClick: (WatchmodeSource) -> Unit) {
    // Filter to only show subscription sources and remove duplicates by name
    val uniqueSubscriptionSources = title.sources
        .filter { it.type == "sub" }
        .distinctBy { it.name }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(title.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Poster for ${title.name}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(width = 80.dp, height = 120.dp).clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = title.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                title.releaseYear?.let {
                    Text(
                        text = it.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Horizontal list of sources
                if (uniqueSubscriptionSources.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(uniqueSubscriptionSources) { source ->
                            SourceChip(source = source, onClick = { onSourceClick(source) })
                        }
                    }
                } else {
                    Text("Not available on any subscription services.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun SourceChip(source: WatchmodeSource, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = source.name,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
