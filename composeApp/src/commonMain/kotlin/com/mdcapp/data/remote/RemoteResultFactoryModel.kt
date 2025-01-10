package com.mdcapp.data.remote

import com.mdcapp.data.model.FactoryModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteResultFactoryModel(
    @SerialName("Fabrica") val name: String = "",
    @SerialName("Marcas") val branchList: List<String> = emptyList()
)

fun RemoteResultFactoryModel.toDomain() = FactoryModel(
    name = name,
    branchList = branchList
)