package com.mdcapp.data.repositories

import com.mdcapp.domain.repositories.DatabaseQuery
import com.mdcapp.domain.repositories.IDatabaseRepository
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer

class AndroidDatabaseRepository(private val db: FirebaseFirestore) : IDatabaseRepository {
    override suspend fun <T : Any> getDocument(path: String, serializer: KSerializer<T>): T? {
        val doc = db.document(path).get()
        return if (doc.exists) doc.data(serializer) else null
    }

    override suspend fun <T : Any> setDocument(path: String, data: T, serializer: KSerializer<T>) {
        db.document(path).set(serializer, data)
    }

    override suspend fun <T : Any> updateDocument(
        path: String,
        data: Any,
        serializer: KSerializer<T>?
    ) {
        if (serializer != null) {
            @Suppress("UNCHECKED_CAST")
            db.document(path).update(serializer as KSerializer<Any>, data)
        } else if (data is Map<*, *>) {
            @Suppress("UNCHECKED_CAST")
            db.document(path).update(data as Map<String, Any?>)
        }
    }

    override suspend fun deleteDocument(path: String) {
        db.document(path).delete()
    }

    override suspend fun <T : Any> addDocument(
        path: String,
        data: T,
        serializer: KSerializer<T>
    ): String {
        return db.collection(path).add(serializer, data).id
    }

    override fun <T : Any> observeDocument(path: String, serializer: KSerializer<T>): Flow<T?> {
        return db.document(path).snapshots.map { if (it.exists) it.data(serializer) else null }
    }

    private fun applyQuery(base: Query, query: DatabaseQuery?): Query {
        var q = base
        query?.let {
            // Compatibilidad con filtros antiguos
            if (it.filterBy != null && it.equalTo != null) {
                val field = it.filterBy
                val value = it.equalTo
                q = q.where { field.equalTo(value) }
            }

            // Nuevos filtros
            it.filters.forEach { filter ->
                val field = filter.field
                val value = filter.value
                q = when (filter.operator) {
                    "EQUAL" -> q.where { field.equalTo(value) }
                    "GREATER_THAN_OR_EQUAL" -> q.where { field.greaterThanOrEqualTo(value) }
                    "LESS_THAN" -> q.where { field.lessThan(value) }
                    "LESS_THAN_OR_EQUAL" -> q.where { field.lessThanOrEqualTo(value) }
                    "GREATER_THAN" -> q.where { field.greaterThan(value) }
                    else -> q
                }
            }

            if (it.orderBy != null) {
                q = q.orderBy(
                    it.orderBy,
                    if (it.descending) Direction.DESCENDING else Direction.ASCENDING
                )
            }

            if (it.limit != null) {
                q = q.limit(it.limit.toLong())
            }

            if (it.startAfter != null) {
                // Intentamos convertir a número si es posible para coincidir con el tipo en Firestore
                val numericValue = it.startAfter.toLongOrNull() ?: it.startAfter.toDoubleOrNull()
                q = q.startAfter(numericValue ?: it.startAfter)
            }
        }
        return q
    }

    override fun <T : Any> observeCollection(
        path: String,
        serializer: KSerializer<T>,
        query: DatabaseQuery?
    ): Flow<List<T>> {
        val q = applyQuery(db.collection(path), query)
        return q.snapshots.map { snapshot ->
            snapshot.documents.map { it.data(serializer) }
        }
    }

    override suspend fun <T : Any> getCollection(
        path: String,
        serializer: KSerializer<T>,
        query: DatabaseQuery?
    ): List<T> {
        val q = applyQuery(db.collection(path), query)
        return q.get().documents.map { it.data(serializer) }
    }

    override suspend fun getCollectionIds(path: String): List<String> {
        return db.collection(path).get().documents.map { it.id }
    }
}
