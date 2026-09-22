package com.mdcapp.domain.logic

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SharedDocument(
    val fileBytes: ByteArray,
    val mimeType: String,
    val fileName: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SharedDocument

        if (!fileBytes.contentEquals(other.fileBytes)) return false
        if (mimeType != other.mimeType) return false
        if (fileName != other.fileName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fileBytes.contentHashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + fileName.hashCode()
        return result
    }
}

object SharedDocumentHandler {
    private val _pendingDocument = MutableStateFlow<SharedDocument?>(null)
    val pendingDocument: StateFlow<SharedDocument?> = _pendingDocument.asStateFlow()

    fun setPendingDocument(document: SharedDocument) {
        _pendingDocument.value = document
    }

    fun clearPendingDocument() {
        _pendingDocument.value = null
    }
}