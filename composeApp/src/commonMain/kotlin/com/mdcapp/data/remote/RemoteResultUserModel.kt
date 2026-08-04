package com.mdcapp.data.remote

import com.mdcapp.domain.entities.PaymentEntry
import com.mdcapp.domain.entities.UserModel
import kotlinx.serialization.Serializable

@Serializable
data class RemoteResultUserModel(
    val uid: String = "",
    val name: String = "",
    val lastName: String = "",
    val email: String = "",
    val subscriptionExpiresAt: Long = 0L,
    val isManuallyEnabled: Boolean = false,
    val paymentHistory: List<RemoteResultPaymentEntry> = emptyList()
)

@Serializable
data class RemoteResultPaymentEntry(
    val date: Long = 0L,
    val amount: Double = 0.0,
    val status: String = "PENDIENTE",
    val transactionRef: String = "",
    val receiptRef: String = ""
)

fun RemoteResultUserModel.toDomain() = UserModel(
    uid = uid,
    name = name,
    lastName = lastName,
    email = email,
    subscriptionExpiresAt = subscriptionExpiresAt,
    isManuallyEnabled = isManuallyEnabled,
    paymentHistory = paymentHistory.map { it.toDomain() }
)

fun UserModel.toRemote() = RemoteResultUserModel(
    uid = uid,
    name = name,
    lastName = lastName,
    email = email,
    subscriptionExpiresAt = subscriptionExpiresAt,
    isManuallyEnabled = isManuallyEnabled,
    paymentHistory = paymentHistory.map { it.toRemote() }
)

fun RemoteResultPaymentEntry.toDomain() = PaymentEntry(
    date = date,
    amount = amount,
    status = status,
    transactionRef = transactionRef,
    receiptRef = receiptRef
)

fun PaymentEntry.toRemote() = RemoteResultPaymentEntry(
    date = date,
    amount = amount,
    status = status,
    transactionRef = transactionRef,
    receiptRef = receiptRef
)
