package com.mdcapp.di

import io.github.aakira.napier.Napier

actual fun initFirebaseApp(context: Any?) {
    // En Desktop no inicializamos el SDK de Firebase ya que usamos Ktor/REST.
    // Esta función queda como un no-op para evitar crashes y errores de compilación.
    Napier.i("Firebase initialization skipped on Desktop (REST mode active)")
}
