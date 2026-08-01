package com.mdcapp.data.remote

import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Serializador resiliente para fechas de Firestore.
 * Maneja casos donde el dato puede venir como Long o como Timestamp (u otros tipos numéricos).
 */
object FirestoreDateSerializer : KSerializer<Long> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FirestoreDate", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: Long) {
        encoder.encodeLong(value)
    }

    override fun deserialize(decoder: Decoder): Long {
        return try {
            decoder.decodeLong()
        } catch (e: Exception) {
            try {
                // Intentamos decodificar como Timestamp si está disponible
                val timestamp = decoder.decodeSerializableValue(Timestamp.serializer())
                (timestamp.seconds * 1000) + (timestamp.nanoseconds / 1000000)
            } catch (e2: Exception) {
                try {
                    decoder.decodeDouble().toLong()
                } catch (e3: Exception) {
                    0L
                }
            }
        }
    }
}
