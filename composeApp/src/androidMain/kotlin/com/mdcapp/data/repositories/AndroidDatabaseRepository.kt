package com.mdcapp.data.repositories

import com.mdcapp.domain.repositories.IDatabaseRepository
import dev.gitlive.firebase.firestore.FirebaseFirestore
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

    override fun <T : Any> observeCollection(
        path: String,
        serializer: KSerializer<T>
    ): Flow<List<T>> {
        return db.collection(path).snapshots.map { snapshot ->
            snapshot.documents.map { it.data(serializer) }
        }
    }

    override suspend fun <T : Any> getCollection(
        path: String,
        serializer: KSerializer<T>
    ): List<T> {
        return db.collection(path).get().documents.map { it.data(serializer) }
    }
}
