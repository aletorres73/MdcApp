package com.mdcapp.data.repositories

import com.mdcapp.domain.repositories.IDatabaseRepository
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.KSerializer

class DesktopDatabaseRepository(
    private val client: HttpClient,
    private val authRepository: DesktopAuthRepository
) : IDatabaseRepository {
    override suspend fun <T : Any> getDocument(path: String, serializer: KSerializer<T>): T? = null
    override suspend fun <T : Any> setDocument(path: String, data: T, serializer: KSerializer<T>) {}
    override suspend fun <T : Any> updateDocument(
        path: String,
        data: Any,
        serializer: KSerializer<T>?
    ) {
    }

    override suspend fun deleteDocument(path: String) {}
    override suspend fun <T : Any> addDocument(
        path: String,
        data: T,
        serializer: KSerializer<T>
    ): String = ""

    override fun <T : Any> observeDocument(path: String, serializer: KSerializer<T>): Flow<T?> =
        flowOf(null)

    override fun <T : Any> observeCollection(
        path: String,
        serializer: KSerializer<T>
    ): Flow<List<T>> = flowOf(emptyList())

    override suspend fun <T : Any> getCollection(
        path: String,
        serializer: KSerializer<T>
    ): List<T> = emptyList()
}
