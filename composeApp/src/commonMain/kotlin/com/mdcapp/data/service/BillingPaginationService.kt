package com.mdcapp.data.service

import android.util.Log
import com.google.firebase.firestore.Query.Direction.ASCENDING
import com.google.firebase.firestore.Query.Direction.DESCENDING
import com.mdcapp.data.remote.RemoteResultBillingModel
import com.mdcapp.domain.entities.InvoicePage
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.Query

class BillingPaginationService(
    private val db: FirebaseFirestore
) {
    companion object {
        const val BILLINGS = "billings"
        const val CLIENTS = "clients"

        val BILLING_OBJECT = RemoteResultBillingModel::class.java
    }

    suspend fun fetchBillingsPaged(
        state: String?,
        client: String?,
        number: String?,
        limit: Long,
        startAfterId: String?,
        direction: String = "desc"
    ): InvoicePage {

        return try {

            var query: Query = db.collection(BILLINGS)

            // Filtros dinámicos
            if (!state.isNullOrBlank()) {
                query = query.where {
                    "Estado" equalTo state
                }
            }

            if (!client.isNullOrBlank()) {
                query = query.where {
                    "Razon Social" equalTo client
                }
            }

            if (!number.isNullOrBlank()) {
                query = query.where {
                    "Numero" equalTo number
                }
            }

            // Orden + límite
            query = query
                .orderBy(
                    "Timestamp",
                    direction = if (direction == "desc") DESCENDING else ASCENDING
                )
                .limit(limit)

            // Cursor
            if (!startAfterId.isNullOrBlank()) {
                val lastDoc = db
                    .collection(BILLINGS)
                    .document(startAfterId)
                    .get()

                if (lastDoc.exists) {
                    query = query.startAfter(lastDoc)
                }
            }

            val snapshot = query.get()

            val items = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.data<RemoteResultBillingModel>()
                } catch (_: Exception) {
                    null
                }
            }
            Log.i("firestore", "on fetchBillingsPaged: $items")

            InvoicePage(
                items = items,
                nextCursor = snapshot.documents.lastOrNull()?.id,
                quantity = snapshot.documents.size
            )

        } catch (e: Exception) {

            Log.e("firestore", "Error fetchBillingsPaged", e)

            InvoicePage(
                emptyList(),
                null,
                0
            )
        }
    }


}