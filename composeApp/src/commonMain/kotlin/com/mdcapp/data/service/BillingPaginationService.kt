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
        startAfterId: String?,
        direction: String = "desc"
    ): InvoicePage {
        return try {
            val filters = mutableListOf<Filter>()

            if (!state.isNullOrBlank() && state != "Todas") {
                filters.add(Filter("Estado", "EQUAL", state))
            }

            // Búsqueda por prefijo para cliente
            var activeOrderBy = "Timestamp"
            if (!client.isNullOrBlank()) {
                activeOrderBy = "Razon Social"
                filters.add(
                    Filter(
                        "Razon Social",
                        "GREATER_THAN_OR_EQUAL",
                        client
                    )
                )
                filters.add(
                    Filter(
                        "Razon Social",
                        "LESS_THAN",
                        client + "\uf8ff"
                    )
                )
            }

            // Búsqueda por prefijo para número
            if (!number.isNullOrBlank()) {
                activeOrderBy = "Numero"
                filters.add(
                    Filter(
                        "Numero",
                        "GREATER_THAN_OR_EQUAL",
                        number
                    )
                )
                filters.add(
                    Filter(
                        "Numero",
                        "LESS_THAN",
                        number + "\uf8ff"
                    )
                )
            }

            val query = DatabaseQuery(
                filters = filters,
                orderBy = activeOrderBy,
                descending = direction == "desc",
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
