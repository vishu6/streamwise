package com.example.streamwise.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

@Composable
fun RecommendationsTab(
    query: String,
    onQueryChange: (String) -> Unit,
    result: String?,
    isLoading: Boolean,
    error: String?,
    onFetch: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "StreamWise",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Just type whats on you mind and get the movie recommendations..",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("E.g., I loved The Avengers, but now I want something with more humor.") },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onFetch,
            enabled = !isLoading && query.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Thinking...", color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp)
            } else {
                Text("Get Smart Recs", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxSize(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Streamwise Suggests",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (error != null) {
                    Text(error, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
                } else if (result != null) {
                    Text(result, style = MaterialTheme.typography.bodyMedium, lineHeight = 1.5.em)
                } else if (!isLoading) {
                    Text("Your personalized recommendation will appear here!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
