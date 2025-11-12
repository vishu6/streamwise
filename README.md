Streamwise - README

=====================
PROJECT INFORMATION
=====================

Streamwise is a modern Android application designed to help users discover and track movies and TV shows across various streaming services. This project is built entirely with Kotlin and leverages the latest Android development tools and practices.

==============
🚀 FEATURES
==============

- Seamless Authentication: Secure sign-in with Google, powered by Firebase Authentication.
- Movie & Show Discovery: (Planned) Browse, search, and discover content from a comprehensive media database.
- Unified Watchlist: (Planned) Keep track of what to watch next in a single, unified list.
- Modern & Responsive UI: A clean and intuitive user interface built with Jetpack Compose and Material 3.

================================
🛠️ TECH STACK & ARCHITECTURE
================================

This project follows modern Android architecture and best practices, utilizing a stack of robust and scalable technologies.

- Core: 100% Kotlin
- UI: Jetpack Compose with a Material 3 design system.
- Architecture: MVVM (Model-View-ViewModel) to separate business logic from the UI.
- Asynchronous Programming: Kotlin Coroutines for managing background threads.
- Backend & Authentication: Firebase (Authentication, Firestore) for a secure and scalable backend.
- Image Loading: Coil for efficient and fast image loading.
- Dependency Management: Gradle Version Catalogs for clean and maintainable dependencies.

=============================
⚙️ SETUP AND CONFIGURATION
=============================

To build and run this project locally, you will need to configure your own Firebase project.

Prerequisites:
- Android Studio Iguana | 2023.2.1 or newer
- JDK 11 or newer

Steps:

1. Clone the Repository:
   git clone https://github.com/your-username/streamwise.git

2. Create a Firebase Project:
   - Go to the Firebase Console (https://console.firebase.google.com/) and create a new project.
   - Add a new Android app to your Firebase project with the package name `com.example.streamwise`.
   - Follow the setup instructions to download the `google-services.json` file.

3. Add `google-services.json`:
   - Place the downloaded `google-services.json` file into the `app/` directory of the project.

4. Enable Authentication:
   - In the Firebase Console, go to the Authentication section.
   - Click "Get Started" and enable Google as a sign-in provider.

5. Build and Run:
   - Open the project in Android Studio.
   - Let Gradle sync the dependencies.
   - Run the app on an emulator or a physical device.

==========
LICENSE
==========

This project is licensed under the MIT License.
