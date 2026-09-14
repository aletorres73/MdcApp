package com.mdcapp.data.service

import com.mdcapp.data.remote.RemoteResultBillingModel
import com.mdcapp.domain.entities.InvoicePage
import com.mdcapp.domain.repositories.DatabaseQuery
import com.mdcapp.domain.repositories.Filter
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
        startAfterId: String?
    ): InvoicePage {
        return try {
            val filters = mutableListOf<Filter>()

            if (!state.isNullOrBlank() && state != "Todas") {
                filters.add(Filter("Estado", "EQUAL", state))
            }

            // Búsqueda escalable usando searchTerms
            val searchValue = client ?: number
            if (!searchValue.isNullOrBlank()) {
                filters.add(Filter("searchTerms", "ARRAY_CONTAINS", searchValue.lowercase()))
            }

            val query = DatabaseQuery(
                filters = filters,
                orderBy = "Timestamp",
                descending = true,
                limit = limit.toInt(),
                startAfter = startAfterId
            )

            println("🔍 [BillingPagination] Fetching from server. Query: $query")

            val pagedItems = db.getCollection(
                "users/$userId/allBillings",
                RemoteResultBillingModel.serializer(),
                query
            )

            InvoicePage(
                items = pagedItems,
                nextCursor = pagedItems.lastOrNull()?.timeStamp?.toString(),
                quantity = pagedItems.size,
                endReached = pagedItems.size < limit.toInt()
            )
        } catch (e: Exception) {
            println("Error en fetchBillingsPaged: ${e.message}")
            InvoicePage(emptyList(), null, 0)
        }
    }
}
