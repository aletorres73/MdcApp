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
            val doc = db.collection("appConfig/android/payment_info")
                .get()
                .documents
                .firstOrNull()
            val data = doc?.data<RemotePaymentInfo>()
            data?.let {
                it.toDomain().copy(id = it.id.ifEmpty { doc.id })
            } ?: PaymentInfo()
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

    suspend fun uploadReceipt(imageBytes: ByteArray, paymentInfo: PaymentInfo): String {
        val id = userId ?: return ""
        val timestamp = System.currentTimeMillis()
        val fileName = "receipts/$id/$timestamp.jpg"
        val ref = storage.reference(fileName)
        ref.putData(wrapImageData(imageBytes))

        val currentUser = getUserProfile() ?: return ""

        // El paymentId ahora lo asigna la Cloud Function tras validar con IA.
        // Se envía en 0 y la función lo actualiza de forma atómica.
        val newPayment = PaymentEntry(
            date = timestamp,
            amount = paymentInfo.amount,
            status = "PENDIENTE",
            receiptRef = fileName,
            paymentInfoId = paymentInfo.id,
            paymentId = 0L
        )

        val updatedHistory = currentUser.paymentHistory + newPayment

        saveUserProfile(
            currentUser.copy(
                paymentHistory = updatedHistory
            )
        )

        return fileName
    }
}
