package com.mdcapp.data.service

import android.util.Log
import com.google.firebase.firestore.Query.Direction.DESCENDING
import com.mdcapp.data.remote.RemoteResultBillingModel
import com.mdcapp.domain.entities.InvoicePage
import dev.gitlive.firebase.firestore.FirebaseFirestore

class BillingPaginationService(
    private val db: FirebaseFirestore
) {
    companion object {
        const val BILLINGS = "billings"
        const val CLIENTS = "clients"

        val BILLING_OBJECT = RemoteResultBillingModel::class.java
    }

    suspend fun fetchBillingsPaged(
        state: String,
        limit: Long,
        startAfterId: String?
    ): InvoicePage {

        return try {

            var query = db
                .collection(BILLINGS)
                .where { "Estado" equalTo state }
                .orderBy("Fecha", direction = DESCENDING)
                .limit(limit)

            if (startAfterId != null) {
                val lastDoc = db
                    .collection(BILLINGS)
                    .document(startAfterId)
                    .get()

                if (lastDoc.exists) {
                    query = query.startAfter(lastDoc)
                }
            }

            val snapshot = query.get()

            val data = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.data<RemoteResultBillingModel>()
                } catch (e: Exception) {
                    null
                }
            }

            val lastDocId = snapshot.documents.lastOrNull()?.id

            Log.i("firestore", "on fetchBillingsPaged: $data")
            Log.i("firestore", "on fetchBillingsPaged: $lastDocId")

            InvoicePage(
                items = data,
                nextCursor = lastDocId,
                quantity = snapshot.documents.size
            )

        } catch (e: Exception) {

            Log.e("firestore", "Error fetch invoices paged: $e")

            InvoicePage(
                emptyList(),
                null,
                0
            )
        }
    }
}