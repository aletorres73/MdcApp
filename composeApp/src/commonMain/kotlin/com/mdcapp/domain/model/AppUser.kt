package com.mdcapp.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AppUser(
    val uid: String,
    val email: String?,
    val displayName: String? = null
)
