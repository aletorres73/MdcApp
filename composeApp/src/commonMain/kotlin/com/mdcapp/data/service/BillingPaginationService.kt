package com.mdcapp.data.service

import com.mdcapp.data.remote.RemoteResultBillingModel
import com.mdcapp.domain.entities.InvoicePage
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.Query

class BillingPaginationService(
    private val db: FirebaseFirestore,
    private val authService: AuthService
) {
    companion object {
        const val BILLINGS = "billings"
    }

    private val userId: String
        get() = authService.currentUser?.uid ?: "unknown"

    private val billingsCollection
        get() = db.collection("users").document(userId).collection(BILLINGS)

    suspend fun fetchBillingsPaged(
        state: String?,
        client: String?,
        number: String?,
        limit: Long,
        startAfterId: String?,
        direction: String = "desc"
    ): InvoicePage {
        return try {
            var query: Query = billingsCollection

            if (!state.isNullOrBlank()) {
                query = query.where { "Estado" equalTo state }
            }

            if (!client.isNullOrBlank()) {
                query = query.where { "Razon Social" equalTo client }
            }

            if (!number.isNullOrBlank()) {
                query = query.where { "Numero" equalTo number }
            }

            query = query
                .orderBy(
                    "Timestamp",
                    direction = if (direction == "desc") Direction.DESCENDING else Direction.ASCENDING
                )
                .limit(limit)

            if (!startAfterId.isNullOrBlank()) {
                val lastDoc = billingsCollection.document(startAfterId).get()
                if (lastDoc.exists) {
                    query = query.startAfter(lastDoc)
                }
            }
// ...

            val snapshot = query.get()

            val items = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.data<RemoteResultBillingModel>()
                } catch (_: Exception) {
                    null
                }
            }

            InvoicePage(
                items = items,
                nextCursor = snapshot.documents.lastOrNull()?.id,
                quantity = snapshot.documents.size
            )
        } catch (e: Exception) {
            println("Error en fetchBillingsPaged: ${e.message}")
            InvoicePage(emptyList(), null, 0)
        }
    }
}