package com.mdcapp.data.remote

import kotlinx.serialization.encoding.Decoder

actual fun decodeTimestampToMillis(decoder: Decoder): Long {
    return try {
        // En Desktop (REST), las fechas suelen venir como String ISO 8601
        // Por ahora, devolvemos 0 o intentamos decodificar el String si es necesario.
        // Como DesktopDatabaseRepository es placeholder, esto se ampliará al implementar el REST real.
        0L
    } catch (e: Exception) {
        0L
    }
}
