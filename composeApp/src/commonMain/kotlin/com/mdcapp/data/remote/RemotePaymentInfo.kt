package com.mdcapp.data.remote

import com.mdcapp.domain.entities.PaymentInfo
import kotlinx.serialization.Serializable

@Serializable
data class RemotePaymentInfo(
    val id: String = "",
    val alias: String = "",
    val cbu: String = "",
    val titular: String = "",
    val amount: Double = 0.0
)

fun RemotePaymentInfo.toDomain() = PaymentInfo(
    id = id,
    alias = alias,
    cbu = cbu,
    titular = titular,
    amount = amount
)

fun PaymentInfo.toRemote() = RemotePaymentInfo(
    id = id,
    alias = alias,
    cbu = cbu,
    titular = titular,
    amount = amount
)
