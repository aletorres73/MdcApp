package com.mdcapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class InfoClientModel(
    val clientId: Int,
    var clientName: String,
    var fantasyName: String,
    var cuit: String,
    var address: String,
    var taxAddress: String,
    var city: String,
    var taxCity: String,
    var deliveryTime: String,
    var email: String,
    var phone: String,
    var contactName: String
)

data class ClientModel(
    val clientId: String,
    val clientName: String,
)

@Serializable
data class RemoteResultClientModel(
    @SerialName("Cliente Id") val clientId: String = "",
    @SerialName("Razón Social") val clientName: String = "",
)

fun RemoteResultClientModel.toDomain() =
    ClientModel(
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