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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeUserProfile(): Flow<UserModel?> {
        return authService.observeAuthState().flatMapLatest { user ->
            val uid = user?.uid ?: userId
            val path = uid?.let { "users/$it" }
            println("🔑 [MDC_SESSION] UserService.observeUserProfile() -> userId: $uid, path: $path")
            if (path == null) {
                println("⚠️ [MDC_SESSION] UserService.observeUserProfile() path is null, emitting null")
                return@flatMapLatest flowOf(null)
            }
            db.observeDocument(path, RemoteResultUserModel.serializer())
                .map { doc ->
                    val remoteUser: RemoteResultUserModel? = doc
                    val profile: UserModel? = remoteUser?.toDomain()
                    println("🔑 [MDC_SESSION] UserService.observeUserProfile emission -> profile: ${profile?.email}, expiresAt: ${profile?.subscriptionExpiresAt}, isManuallyEnabled: ${profile?.isManuallyEnabled}")
                    profile
                }
        }
    }

    suspend fun getUserProfile(): UserModel? {
        val uid = userId
        val path = userPath
        println("🔑 [MDC_SESSION] UserService.getUserProfile() called -> userId: $uid, path: $path")
        if (path == null) return null
        return try {
            val data = db.getDocument(path, RemoteResultUserModel.serializer())
            val profile = data?.toDomain() ?: UserModel(
                uid = userId ?: "",
                email = authService.currentUser?.email ?: ""
            )
            println("🔑 [MDC_SESSION] UserService.getUserProfile() success -> profile: ${profile.email}")
            profile
        } catch (e: Exception) {
            println("❌ [MDC_SESSION] Error fetching user profile: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    suspend fun saveUserProfile(user: UserModel) {
        val id = user.uid.ifEmpty { userId } ?: return
        val path = "users/$id"
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
