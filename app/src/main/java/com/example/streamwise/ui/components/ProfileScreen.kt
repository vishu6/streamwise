package com.example.streamwise.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.streamwise.data.STREAMING_SERVICES
import com.example.streamwise.data.Service
import com.example.streamwise.data.UsageEvent
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun ProfileScreen(
    subscriptions: Set<String>,
    recentUsage: List<UsageEvent>,
    onToggleSubscription: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val user = Firebase.auth.currentUser
    val suggestion = remember(subscriptions, recentUsage) {
        getOptimizationSuggestion(subscriptions, recentUsage)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // User Profile Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(user?.photoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "User Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(64.dp).clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(user?.displayName ?: "Guest User", style = MaterialTheme.typography.headlineSmall)
                Text(user?.email ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Subscription Optimizer Section
        Text("Subscription Optimizer", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Toggle the services you pay for. We'll suggest which ones to cancel based on your recent activity.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Smart Tip", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(suggestion, style = MaterialTheme.typography.bodyMedium, lineHeight = 1.5.em)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(STREAMING_SERVICES) { service ->
                val isSubscribed = subscriptions.contains(service.id)
                SubscriptionToggleRow(service = service, isSubscribed = isSubscribed, onToggle = onToggleSubscription)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sign Out Button
        Button(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Text("Sign Out", color = MaterialTheme.colorScheme.onErrorContainer)
        }
    }
}

@Composable
private fun SubscriptionToggleRow(service: Service, isSubscribed: Boolean, onToggle: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onToggle(service.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSubscribed) service.color.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isSubscribed) service.color else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween // This is the fix
        ) {
            Text(
                text = service.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Switch(
                checked = isSubscribed,
                onCheckedChange = { onToggle(service.id) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = service.color,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    checkedTrackColor = service.color.copy(alpha = 0.5f),
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

private fun getOptimizationSuggestion(subscriptions: Set<String>, usage: List<UsageEvent>): String {
    if (subscriptions.isEmpty()) {
        return "You haven't selected any subscriptions. Toggle the services you pay for to get started!"
    }

    val usedServiceIds = usage.map { it.serviceId }.toSet()
    val unusedSubscriptions = subscriptions.filterNot { it in usedServiceIds }

    if (unusedSubscriptions.isEmpty()) {
        return "You're using all your subscriptions. Keep up the smart streaming!"
    }

    val unusedServiceNames = unusedSubscriptions.mapNotNull { serviceId ->
        STREAMING_SERVICES.find { it.id == serviceId }?.name
    }

    return if (unusedServiceNames.size == 1) {
        "You subscribe to ${unusedServiceNames.first()} but haven't used it in the last 90 days. Consider pausing it to save money!"
    } else {
        "You subscribe to ${unusedServiceNames.joinToString()} but haven't used them in the last 90 days. Consider pausing them to save money!"
    }
}
