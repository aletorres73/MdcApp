package com.mdcapp.data.model

data class ClientModel(
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