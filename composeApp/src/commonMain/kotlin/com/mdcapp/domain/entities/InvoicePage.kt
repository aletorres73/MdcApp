package com.mdcapp.domain.entities

import com.mdcapp.data.remote.RemoteResultBillingModel

data class InvoicePage(
    val items: List<RemoteResultBillingModel>,
    val nextCursor: String?,
    val quantity: Int
)
