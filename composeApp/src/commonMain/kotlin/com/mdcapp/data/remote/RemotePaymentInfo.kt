package com.mdcapp.data.remote

import com.mdcapp.domain.entities.PaymentInfo
import kotlinx.serialization.Serializable

@Serializable
data class RemotePaymentInfo(
    val alias: String = "",
    val cbu: String = "",
    val titular: String = "",
    val amount: Double = 0.0
)

fun RemotePaymentInfo.toDomain() = PaymentInfo(
    alias = alias,
    cbu = cbu,
    titular = titular,
    amount = amount
)

fun PaymentInfo.toRemote() = RemotePaymentInfo(
    alias = alias,
    cbu = cbu,
    titular = titular,
    amount = amount
)
