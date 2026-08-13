package com.mdcapp.data.service

import com.mdcapp.data.remote.RemotePaymentInfo
import com.mdcapp.data.remote.RemoteResultUserModel
import com.mdcapp.data.remote.toDomain
import com.mdcapp.data.remote.toRemote
import com.mdcapp.domain.entities.PaymentEntry
import com.mdcapp.domain.entities.PaymentInfo
import com.mdcapp.domain.entities.UserModel
import com.mdcapp.domain.repositories.IDatabaseRepository
import com.mdcapp.domain.repositories.IStorageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserService(
    private val db: IDatabaseRepository,
    private val storage: IStorageRepository,
    private val authService: AuthService
) {
    private val userId: String?
        get() = authService.currentUser?.uid

    private val userPath
        get() = userId?.let { "users/$it" }

    suspend fun getPaymentInfo(): PaymentInfo {
        return try {
            val docs =
                db.getCollection("appConfig/android/payment_info", RemotePaymentInfo.serializer())
            val data = docs.firstOrNull()
            data?.let {
                it.toDomain()
                    .copy(id = it.id.ifEmpty { "payment_info" }) // Ajuste manual de ID si es necesario
            } ?: PaymentInfo()
        } catch (e: Exception) {
            println("Error fetching payment info: ${e.message}")
            PaymentInfo()
        }
    }

    fun observeUserProfile(): Flow<UserModel?> {
        val path = userPath ?: return kotlinx.coroutines.flow.flowOf(null)
        return db.observeDocument(path, RemoteResultUserModel.serializer()).map { it?.toDomain() }
    }

    suspend fun getUserProfile(): UserModel? {
        val path = userPath ?: return null
        return try {
            val data = db.getDocument(path, RemoteResultUserModel.serializer())
            data?.toDomain() ?: UserModel(
                uid = userId ?: "",
                email = authService.currentUser?.email ?: ""
            )
        } catch (e: Exception) {
            println("Error fetching user profile: ${e.message}")
            null
        }
    }

    suspend fun saveUserProfile(user: UserModel) {
        val id = userId ?: return
        val path = userPath ?: return
        db.setDocument(path, user.copy(uid = id).toRemote(), RemoteResultUserModel.serializer())
    }

    suspend fun uploadReceipt(imageBytes: ByteArray, paymentInfo: PaymentInfo): String {
        val id = userId ?: return ""
        val timestamp = System.currentTimeMillis()
        val fileName = "receipts/$id/$timestamp.jpg"
        storage.uploadFile(fileName, imageBytes)

        val currentUser = getUserProfile() ?: return ""

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
