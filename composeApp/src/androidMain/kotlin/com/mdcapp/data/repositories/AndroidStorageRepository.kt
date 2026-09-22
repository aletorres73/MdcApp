package com.mdcapp.data.repositories

import com.mdcapp.domain.repositories.IStorageRepository
import dev.gitlive.firebase.storage.Data
import dev.gitlive.firebase.storage.FirebaseStorage

class AndroidStorageRepository(private val storage: FirebaseStorage) : IStorageRepository {
    override suspend fun uploadFile(path: String, data: ByteArray): String {
        val ref = storage.reference(path)
        ref.putData(Data(data))
        return ref.getDownloadUrl()
    }

    override suspend fun getDownloadUrl(path: String): String {
        return storage.reference(path).getDownloadUrl()
    }

    override suspend fun deleteFile(pathOrUrl: String): Boolean {
        return try {
            val path = extractStoragePath(pathOrUrl)
            if (path.isNotEmpty()) {
                storage.reference(path).delete()
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun extractStoragePath(pathOrUrl: String): String {
        if (pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://")) {
            val pathPart = pathOrUrl.substringAfter("/o/").substringBefore("?")
            return pathPart.replace("%2F", "/").replace("%20", " ")
        }
        return pathOrUrl
    }
}
