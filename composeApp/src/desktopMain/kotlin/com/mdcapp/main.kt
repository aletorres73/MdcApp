package com.mdcapp

import android.app.Application
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.google.firebase.FirebasePlatform
import com.mdcapp.di.initKoin
import com.mdcapp.ui.PlatformNavigation
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize

fun main() = application {
    initKoin()
    try {
        FirebasePlatform.initializeFirebasePlatform(object : FirebasePlatform() {
            val storage = mutableMapOf<String, String>()
            override fun clear(key: String) {
                storage.remove(key)
            }

            override fun log(msg: String) = println(msg)

            override fun retrieve(key: String) = storage[key]

            override fun store(key: String, value: String) = storage.set(key, value)
        })
        val firebaseOptions = FirebaseOptions(
            projectId = "database-rw-60033",
            apiKey = "AIzaSyC9N_Nnpz0w4YK0VEsHTr7lUok4Xja9X1M",
            applicationId = "1:905643704793:web:db86558483e42ac480d9c8"
        )
        Firebase.initialize(Application(), firebaseOptions)
    } catch (e: Exception) {
        println(e)
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "MDCapp Desktop",
        state = WindowState(
            size = DpSize(1200.dp, 780.dp),
            position = WindowPosition(Alignment.CenterStart)
        )
    ) {
        PlatformNavigation()
    }
}