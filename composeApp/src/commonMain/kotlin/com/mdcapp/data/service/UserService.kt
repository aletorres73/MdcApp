package com.mdcapp.data.service

import com.mdcapp.data.remote.RemotePaymentInfo
import com.mdcapp.data.remote.RemoteResultUserModel
import com.mdcapp.data.remote.toDomain
import com.mdcapp.data.remote.toRemote
import com.mdcapp.domain.entities.PaymentEntry
import com.mdcapp.domain.entities.PaymentInfo
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
    private val userId: String?
        get() = authService.currentUser?.uid

    private val userDocument
        get() = userId?.let { db.collection("users").document(it) }

    suspend fun getPaymentInfo(): PaymentInfo {
        return try {
            db.collection("appConfig/android/payment_info")
                .get()
                .documents
                .firstOrNull()?.data<RemotePaymentInfo>()?.toDomain()
                ?: PaymentInfo()
        } catch (e: Exception) {
            println("Error fetching payment info: ${e.message}")
            PaymentInfo()
        }
    }

    fun observeUserProfile(): Flow<UserModel?> {
        val doc = userDocument ?: return kotlinx.coroutines.flow.flowOf(null)
        return doc.snapshots.map { it.data<RemoteResultUserModel>().toDomain() }
    }

    suspend fun getUserProfile(): UserModel? {
        val doc = userDocument ?: return null
        return try {
            val snapshot = doc.get()
            if (snapshot.exists) {
                snapshot.data<RemoteResultUserModel>().toDomain()
            } else {
                UserModel(uid = userId ?: "", email = authService.currentUser?.email ?: "")
            }
        } catch (e: Exception) {
            println("Error fetching user profile: ${e.message}")
            null
        }
    }

    suspend fun saveUserProfile(user: UserModel) {
        val id = userId ?: return
        userDocument?.set(user.copy(uid = id).toRemote())
    }

    suspend fun uploadReceipt(imageBytes: ByteArray): String {
        val id = userId ?: return ""
        val timestamp = System.currentTimeMillis()
        val fileName = "receipts/$id/$timestamp.jpg"
        val ref = storage.reference(fileName)
        ref.putData(wrapImageData(imageBytes))

        val currentUser = getUserProfile() ?: return ""
        val newPayment = PaymentEntry(
            date = timestamp,
            status = "PENDIENTE",
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
