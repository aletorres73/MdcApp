package com.mdcapp.data.remote

import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.serialization.encoding.Decoder

actual fun decodeTimestampToMillis(decoder: Decoder): Long {
    val timestamp = decoder.decodeSerializableValue(Timestamp.serializer())
    return (timestamp.seconds * 1000) + (timestamp.nanoseconds / 1000000)
}
