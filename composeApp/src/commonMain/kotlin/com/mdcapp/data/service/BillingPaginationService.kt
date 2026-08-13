package com.mdcapp.data.service

import com.mdcapp.data.remote.RemoteResultBillingModel
import com.mdcapp.domain.entities.InvoicePage
import com.mdcapp.domain.repositories.IDatabaseRepository

class BillingPaginationService(
    private val db: IDatabaseRepository,
    private val authService: AuthService
) {
    companion object {
        const val BILLINGS = "billings"
    }

    private val userId: String
        get() = authService.currentUser?.uid ?: "unknown"

    suspend fun fetchBillingsPaged(
        state: String?,
        client: String?,
        number: String?,
        limit: Long,
        startAfterId: String?,
        direction: String = "desc"
    ): InvoicePage {
        return try {
            val allBillings = db.getCollection(
                "users/$userId/allBillings",
                RemoteResultBillingModel.serializer()
            )

            var filtered = allBillings.asSequence()

            if (!state.isNullOrBlank()) {
                filtered = filtered.filter { it.stateBilling == state }
            }

            if (!client.isNullOrBlank()) {
                filtered = filtered.filter { it.clientName == client }
            }

            if (!number.isNullOrBlank()) {
                filtered = filtered.filter { it.billingNumber == number }
            }

            val sorted = if (direction == "desc") {
                filtered.sortedByDescending { it.timeStamp }
            } else {
                filtered.sortedBy { it.timeStamp }
            }

            val sortedList = sorted.toList()

            val startIndex = if (startAfterId.isNullOrBlank()) 0 else {
                val index = sortedList.indexOfFirst { it.billingNumber == startAfterId }
                if (index == -1) 0 else index + 1
            }

            val pagedItems = sortedList.drop(startIndex).take(limit.toInt())

            InvoicePage(
                items = pagedItems,
                nextCursor = pagedItems.lastOrNull()?.billingNumber,
                quantity = sortedList.size
            )
        } catch (e: Exception) {
            println("Error en fetchBillingsPaged: ${e.message}")
            InvoicePage(emptyList(), null, 0)
        }
    }
}
