package com.mdcapp.domain.repositories

interface IStorageRepository {
    suspend fun uploadFile(path: String, data: ByteArray): String
    suspend fun getDownloadUrl(path: String): String
}
