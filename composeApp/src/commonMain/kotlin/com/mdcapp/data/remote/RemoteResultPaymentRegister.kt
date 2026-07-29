package com.mdcapp.data.remote

import com.mdcapp.domain.entities.MovementMethod
import com.mdcapp.domain.entities.MovementStatus
import com.mdcapp.domain.entities.PaymentRegisterModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemotePaymentRegisterResult(
    @SerialName("Pago Id") val id: Int = 0,
    @SerialName("Cliente ID") val clientId: String = "",
    @SerialName("Marca") val branch: String = "",
    @SerialName("Fecha") val date: Long = 0L,
    @SerialName("Razón Social") val clientName: String = "",
    @SerialName("Remito") val documentNumber: String = "",
    @SerialName("Tipo") val type: String = "",
    @SerialName("Monto pagado") val total: Double = 0.0,
    @SerialName("Notas") val notes: String = "",
    @SerialName("Metodo") val method: String = MovementMethod.PAGO.name,
    @SerialName("Estado") val status: String = MovementStatus.PENDIENTE.name,
    @SerialName("Fecha Conciliacion") val reconciliationDate: Long = 0L,
    @SerialName("Es Virtual") val isVirtual: Boolean = false
)

fun RemotePaymentRegisterResult.toPaymentDomain() = PaymentRegisterModel(
    id,
    clientId,
    branch,
    date,
    clientName,
    documentNumber,
    type,
    total,
    notes,
    method,
    status,
    reconciliationDate,
    isVirtual
)

fun PaymentRegisterModel.toPaymentRemote() = RemotePaymentRegisterResult(
    id,
    clientId,
    branch,
    date,
    clientName,
    documentNumber,
    type,
    total,
    notes,
    method,
    status,
    reconciliationDate,
    isVirtual
)

