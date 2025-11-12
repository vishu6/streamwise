# streamwise
🎬 Streamwise AI: Subscription Optimizer and Content Advisor

Streamwise AI is a modern Android application built with Jetpack Compose that helps users manage their sprawling digital content consumption. It provides a centralized, real-time watchlist and leverages external APIs and AI-driven logic to help users consolidate subscriptions and save money.

✨ Core Features

Streamwise is designed to be the single source of truth for your entertainment decisions:

Real-Time Watchlist: Add movies and shows to your personal watchlist, which is synced instantly across devices using Firebase Firestore.

Secure Authentication: Seamless user onboarding using Google Sign-In and Firebase Authentication.

Live Content Search: Search for movies using a real external API (like The Movie Database - TMDb) to get accurate, up-to-date information.

Subscription Optimization (Logic Pending): The foundational structure is in place to analyze your current watchlist against service availability and costs, providing smart recommendations on which streaming services you actually need to keep.

AI Content Advisor: (Future Feature) Structure ready for integrating an LLM to ask highly specific recommendation questions (e.g., "Recommend a lighthearted sci-fi show similar to X").

🛠️ Technology Stack

This project is built following modern Android and Google best practices, utilizing the MVVM architectural pattern.

Primary Language: Kotlin

UI Toolkit: Jetpack Compose (Modern, declarative UI)

Architecture: MVVM (Model-View-ViewModel)

Data & Backend:

Firebase Firestore: Real-time database for Watchlist data.

Firebase Authentication: Handles Google Sign-In and user identity.

Networking:

Retrofit: Type-safe HTTP client for API calls.

Kotlinx Serialization: Efficient conversion of API JSON to Kotlin data classes.

Concurrency: Kotlin Coroutines and Flow for reactive data streams.

🚀 Getting Started

To run this project locally, you will need to set up Firebase and provide an external API key.

Clone the Repository:

git clone [YOUR_REPO_URL]


Firebase Setup:

Create a new project in the Firebase Console.

Enable the Firestore Database and Google Sign-In provider in Firebase Authentication.

Download your google-services.json file and place it in the app/ directory.

API Key Configuration:

Obtain an API key from a movie database (e.g., TMDb).

Replace the placeholder YOUR_TMDB_API_KEY in app/src/main/java/com/example/streamwise/data/StreamwiseRepository.kt with your actual key.

Run the Application:
Open the project in Android Studio and run on an emulator or physical device.
