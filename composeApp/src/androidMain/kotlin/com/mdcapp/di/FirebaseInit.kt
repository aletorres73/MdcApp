package com.mdcapp.di

import android.content.Context
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.mdcapp.BuildConfig
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

        // Initialize Firebase App Check for Vertex AI and other Firebase services
        try {
            val firebaseAppCheck = FirebaseAppCheck.getInstance()
            if (BuildConfig.DEBUG) {
                firebaseAppCheck.installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance()
                )
                Napier.i("Firebase App Check initialized with DebugAppCheckProviderFactory")

                // Request token at startup so DebugAppCheckProvider logs the debug secret to Logcat immediately
                firebaseAppCheck.getAppCheckToken(false)
                    .addOnSuccessListener { _ ->
                        Napier.i("App Check token obtained successfully")
                    }
                    .addOnFailureListener { e ->
                        Napier.w(
                            "App Check token request failed (register the debug token from Logcat in Firebase Console)",
                            e
                        )
                    }
            } else {
                firebaseAppCheck.installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance()
                )
                Napier.i("Firebase App Check initialized with PlayIntegrityAppCheckProviderFactory")
            }
        } catch (e: Exception) {
            Napier.e("Failed to initialize Firebase App Check", e)
        }

        Napier.i("Firebase initialized successfully on Android")
    } catch (e: Exception) {
        Napier.e("Failed to initialize Firebase on Android", e)
    }
}
