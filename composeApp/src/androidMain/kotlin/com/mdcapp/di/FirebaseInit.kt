package com.mdcapp.di

import android.content.Context
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize
import io.github.aakira.napier.Napier

actual fun initFirebaseApp(context: Any?) {
    try {
        val appContext = context as? Context ?: return

        val firebaseOptions = FirebaseOptions(
            projectId = "database-rw-60033",
            apiKey = "AIzaSyC9N_Nnpz0w4YK0VEsHTr7lUok4Xja9X1M",
            applicationId = "1:905643704793:web:db86558483e42ac480d9c8",
        )

        // Using GitLive initialization with explicit options to ensure it works without google-services plugin
        Firebase.initialize(appContext, options = firebaseOptions)
        Napier.i("Firebase initialized successfully on Android")
    } catch (e: Exception) {
        Napier.e("Failed to initialize Firebase on Android", e)
    }
}
