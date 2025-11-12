package com.example.streamwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.streamwise.ui.StreamwiseApp
import com.example.streamwise.ui.theme.StreamwiseTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

// Main Activity
class MainActivity : ComponentActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var signInLauncher: ActivityResultLauncher<android.content.Intent>

    private val userId = mutableStateOf<String?>(null)
    private val isLoading = mutableStateOf(false)
    private val authError = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Set up the ActivityResultLauncher for Google Sign-In
        signInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    authError.value = "Google Sign-In failed. (code: ${e.statusCode})"
                    isLoading.value = false
                }
            } else {
                isLoading.value = false
            }
        }

        // Check for existing signed-in user
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            userId.value = currentUser.uid
        }

        setContent {
            StreamwiseTheme {
                // Define the sign-out function
                val onSignOut: () -> Unit = {
                    Firebase.auth.signOut()
                    googleSignInClient.signOut().addOnCompleteListener { 
                        userId.value = null
                    }
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    if (userId.value != null) {
                        StreamwiseApp(onSignOut = onSignOut)
                    } else {
                        GoogleSignInScreen(
                            isLoading = isLoading.value,
                            error = authError.value,
                            onSignInClick = {
                                isLoading.value = true
                                authError.value = null
                                val signInIntent = googleSignInClient.signInIntent
                                signInLauncher.launch(signInIntent)
                            }
                        )
                    }
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        Firebase.auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    userId.value = Firebase.auth.currentUser?.uid
                } else {
                    authError.value = "Firebase authentication failed."
                }
                isLoading.value = false
            }
    }
}

// Google Sign-In Screen Composable
@Composable
fun GoogleSignInScreen(
    isLoading: Boolean,
    error: String?,
    onSignInClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Streamwise Logo",
                modifier = Modifier.size(150.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Find and compare movies across all your streaming services.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(48.dp))

            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Button(
                    onClick = onSignInClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google_logo),
                        contentDescription = "Google Logo",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Unspecified // Use original colors of the vector
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Sign in with Google", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            error?.let {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// Previews
@Preview(showBackground = true)
@Composable
fun SignInScreenPreview() {
    StreamwiseTheme {
        GoogleSignInScreen(isLoading = false, error = null, onSignInClick = {})
    }
}

@Preview(showBackground = true, name = "Sign-In Loading")
@Composable
fun SignInScreenLoadingPreview() {
    StreamwiseTheme {
        GoogleSignInScreen(isLoading = true, error = null, onSignInClick = {})
    }
}

@Preview(showBackground = true, name = "Sign-In Error")
@Composable
fun SignInScreenErrorPreview() {
    StreamwiseTheme {
        GoogleSignInScreen(isLoading = false, error = "Authentication failed. Please try again.", onSignInClick = {})
    }
}
