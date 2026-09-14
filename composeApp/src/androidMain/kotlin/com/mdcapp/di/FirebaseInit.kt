package com.mdcapp.di

import android.content.Context
import com.mdcapp.BuildKonfig
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize
import io.github.aakira.napier.Napier

actual fun initFirebaseApp(context: Any?) {
    try {
        val appContext = context as? Context ?: return

        val firebaseOptions = FirebaseOptions(
            projectId = BuildKonfig.FIREBASE_PROJECT_ID,
            apiKey = BuildKonfig.FIREBASE_API_KEY,
            applicationId = BuildKonfig.FIREBASE_APP_ID_ANDROID,
        )

        // Using GitLive initialization with explicit options to ensure it works without google-services plugin
        Firebase.initialize(appContext, options = firebaseOptions)
        Napier.i("Firebase initialized successfully on Android")
    } catch (e: Exception) {
        Napier.e("Failed to initialize Firebase on Android", e)
    }
}
