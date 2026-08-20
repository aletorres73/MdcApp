package com.mdcapp.domain.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.KSerializer

@kotlinx.serialization.Serializable
data class DatabaseQuery(
    val filterBy: String? = null,
    val equalTo: String? = null,
    val orderBy: String? = null,
    val descending: Boolean = false,
    val limit: Int? = null
)

interface IDatabaseRepository {
    suspend fun <T : Any> getDocument(path: String, serializer: KSerializer<T>): T?
    suspend fun <T : Any> setDocument(path: String, data: T, serializer: KSerializer<T>)
    suspend fun <T : Any> updateDocument(
        path: String,
        data: Any,
        serializer: KSerializer<T>? = null
    )

    suspend fun deleteDocument(path: String)
    suspend fun <T : Any> addDocument(path: String, data: T, serializer: KSerializer<T>): String
    fun <T : Any> observeDocument(path: String, serializer: KSerializer<T>): Flow<T?>
    fun <T : Any> observeCollection(
        path: String,
        serializer: KSerializer<T>,
        query: DatabaseQuery? = null
    ): Flow<List<T>>

    suspend fun <T : Any> getCollection(
        path: String,
        serializer: KSerializer<T>,
        query: DatabaseQuery? = null
    ): List<T>
}
