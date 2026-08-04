package com.mdcapp.data.service

import com.mdcapp.data.remote.RemoteResultUserModel
import com.mdcapp.data.remote.toDomain
import com.mdcapp.data.remote.toRemote
import com.mdcapp.domain.entities.PaymentEntry
import com.mdcapp.domain.entities.UserModel
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserService(
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val authService: AuthService
) {
    private val userId: String
        get() = authService.currentUser?.uid ?: throw Exception("User not logged in")

    private val userDocument
        get() = db.collection("users").document(userId)

    fun observeUserProfile(): Flow<UserModel?> {
        return userDocument.snapshots.map { it.data<RemoteResultUserModel>().toDomain() }
    }

    suspend fun getUserProfile(): UserModel? {
        return try {
            val snapshot = userDocument.get()
            if (snapshot.exists) {
                snapshot.data<RemoteResultUserModel>().toDomain()
            } else {
                UserModel(uid = userId, email = authService.currentUser?.email ?: "")
            }
        } catch (e: Exception) {
            println("Error fetching user profile: ${e.message}")
            null
        }
    }

    suspend fun saveUserProfile(user: UserModel) {
        userDocument.set(user.copy(uid = userId).toRemote())
    }

    suspend fun uploadReceipt(imageBytes: ByteArray): String {
        val timestamp = System.currentTimeMillis()
        val fileName = "receipts/$userId/$timestamp.jpg"
        val ref = storage.reference(fileName)
        ref.putData(wrapImageData(imageBytes))

        val currentUser = getUserProfile() ?: return ""
        val newPayment = PaymentEntry(
            date = timestamp,
            status = "PENDIENTE_IA",
            receiptRef = fileName
        )

        val updatedHistory = currentUser.paymentHistory + newPayment

        // La extensión de la suscripción ahora la maneja la Cloud Function tras validar con IA.
        // Solo guardamos el historial actualizado con el estado PENDIENTE_IA.
        saveUserProfile(
            currentUser.copy(
                paymentHistory = updatedHistory
            )
        )

        return fileName
    }
}
