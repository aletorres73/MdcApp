package com.mdcapp.data.remote

import com.mdcapp.domain.entities.ClientModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteResultClientModel(
    @SerialName("Cliente Id") val clientId: String = "",
    @SerialName("Razón Social") val clientName: String = "",
)

fun RemoteResultClientModel.toClientDomain() =
    ClientModel(
        clientId = clientId,
        clientName = clientName,
    )

fun ClientModel.toClientRemote() =
    RemoteResultClientModel(
        clientId = clientId,
        clientName = clientName,
    )

@Serializable
data class RemoteResultInfoClientModel(
    @SerialName("Cliente Id") val clientId: String = "",
    @SerialName("Razón Social") val clientName: String = "",
    @SerialName("Nombre fantasia") val fantasyName: String = "",
    @SerialName("CUIT") val cuit: String = "",
    @SerialName("Direccion Comercio") val address: String = "",
    @SerialName("Direccion Fiscal") val taxAddress: String = "",
    @SerialName("Localidad Comercio") val city: String = "",
    @SerialName("Localidad Fiscal") val taxCity: String = "",
    @SerialName("Horario de entrega") val deliveryTime: String = "",
    @SerialName("Email") val email: String = "",
    @SerialName("Telefono") val phone: String = "",
    @SerialName("Contacto") val contactName: String = ""
)

