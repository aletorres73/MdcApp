package com.mdcapp.data.remote

import com.mdcapp.domain.entities.PaymentRegisterModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemotePaymentRegisterResult(
    @SerialName("Pago Id") val id: Int = 0,
    @SerialName("Cliente ID") val clientId: String = "",
    @SerialName("Marca") val branch: String = "",
    @SerialName("Fecha") val date: String = "",
    @SerialName("Razón Social") val clientName: String = "",
    @SerialName("Remito") val documentNumber: String = "",
    @SerialName("Tipo") val type: String = "",
    @SerialName("Monto pagado") val total: Double = 0.0,
    @SerialName("Notas") val notes: String = ""
)

fun RemotePaymentRegisterResult.toPaymentDomain() = PaymentRegisterModel(
    id, clientId, branch, date, clientName, documentNumber, type, total, notes
)

fun PaymentRegisterModel.toPaymentRemote() = RemotePaymentRegisterResult(
    id, clientId, branch, date, clientName, documentNumber, type, total, notes
)

