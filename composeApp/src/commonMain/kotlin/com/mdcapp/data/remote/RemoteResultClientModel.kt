package com.mdcapp.data.remote

import com.mdcapp.domain.entities.ClientModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteResultClientModel(
    @SerialName("Cliente Id") val clientId: String = "",
    @SerialName("Razón Social") val clientName: String = "",
    @SerialName("Activo") val isActive: Boolean = true,
    @SerialName("CUIT") val cuit: String = "",
    @SerialName("Direccion Comercio") val address: String = "",
    @SerialName("Direccion Fiscal") val taxAddress: String = "",
    @SerialName("Email") val email: String = "",
    @SerialName("Telefono") val phone: String = "",
    @SerialName("Localidad") val city: String = ""
)

fun RemoteResultClientModel.toClientDomain() =
    ClientModel(
        clientId = clientId,
        clientName = clientName,
        isActive = isActive,
        cuit = cuit,
        address = address,
        taxAddress = taxAddress,
        email = email,
        phone = phone,
        city = city
    )

fun ClientModel.toClientRemote() =
    RemoteResultClientModel(
        clientId = clientId,
        clientName = clientName,
        isActive = isActive,
        cuit = cuit,
        address = address,
        taxAddress = taxAddress,
        email = email,
        phone = phone,
        city = city
    )
