package com.mdcapp.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteBranchOrder(
    @SerialName("Marca")
    val branch: String = "",
)