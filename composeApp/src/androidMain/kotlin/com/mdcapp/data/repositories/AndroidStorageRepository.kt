package com.mdcapp.data.repositories

import com.mdcapp.domain.repositories.IStorageRepository
import dev.gitlive.firebase.storage.Data
import dev.gitlive.firebase.storage.FirebaseStorage

class AndroidStorageRepository(private val storage: FirebaseStorage) : IStorageRepository {
    override suspend fun uploadFile(path: String, data: ByteArray): String {
        val ref = storage.reference(path)
        ref.putData(Data(data))
        return path
    }

    override suspend fun getDownloadUrl(path: String): String {
        return storage.reference(path).getDownloadUrl()
    }
}
