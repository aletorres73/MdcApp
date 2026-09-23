package com.mdcapp.data.repositories

import com.mdcapp.domain.repositories.IDatabaseRepository
import com.mdcapp.domain.repositories.IInvoiceScannerService
import com.mdcapp.domain.repositories.IStorageRepository
import com.mdcapp.domain.repositories.ScanResult
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.milliseconds

@Serializable
data class ScanResultDto(
    val status: String = "",
    val number: String = "",
    val total: String = "",
    val type: String = "",
    val date: String = "",
    val clientName: String = "",
    val filePath: String = "",
    val error: String? = null,
    val createdAt: Long = 0L
)

class FirebaseInvoiceScannerService(
    private val storageRepository: IStorageRepository,
    private val databaseRepository: IDatabaseRepository
) : IInvoiceScannerService {

    override suspend fun scanInvoice(
        fileBytes: ByteArray,
        mimeType: String,
        fileName: String,
        clientName: String
    ): ScanResult? {
        return try {
            val cleanClient = if (clientName.isBlank()) "general" else clientName.replace("/", "_")
            val timestamp = System.currentTimeMillis()

            val rawFileName =
                if (fileName.isBlank()) "doc_$timestamp" else fileName.replace(" ", "_")
            val extension =
                if (rawFileName.contains('.')) rawFileName.substringAfterLast('.') else ""
            val extPath = if (extension.isNotEmpty()) ".$extension" else ""
            val nameWithoutExt =
                if (rawFileName.contains('.')) rawFileName.substringBeforeLast('.') else rawFileName
            val sanitizedBaseName = nameWithoutExt.replace(Regex("[^a-zA-Z0-9_-]"), "_")

            val documentId = "${sanitizedBaseName}_$timestamp"
            val storageFileName = "$documentId$extPath"
            val storagePath = "invoices/$cleanClient/$storageFileName"

            Napier.i("Uploading file to Storage: $storagePath (documentId: $documentId)")
            val documentUrl = storageRepository.uploadFile(storagePath, fileBytes)

            Napier.i("Observing real-time Cloud Function processing at scan_results/$documentId")
            val scanPath = "scan_results/$documentId"

            val resultDto = withTimeoutOrNull(35_000L.milliseconds) {
                databaseRepository.observeDocument(scanPath, ScanResultDto.serializer())
                    .filterNotNull()
                    .firstOrNull {
                        it.status == "completed" || it.status == "error"
                    }
            }

            if (resultDto == null) {
                Napier.e("Timeout waiting for Cloud Function scan result at $scanPath")
                return ScanResult(
                    metadata = emptyMap(),
                    documentUrl = documentUrl
                )
            }

            if (resultDto.status == "error") {
                Napier.e("Cloud Function reported error scanning document: ${resultDto.error}")
                return ScanResult(
                    metadata = emptyMap(),
                    documentUrl = documentUrl
                )
            }

            val metadataMap = mutableMapOf<String, String>()
            if (resultDto.number.isNotBlank()) metadataMap["number"] = resultDto.number
            if (resultDto.total.isNotBlank()) metadataMap["total"] = resultDto.total
            if (resultDto.type.isNotBlank()) metadataMap["type"] = resultDto.type
            if (resultDto.date.isNotBlank()) metadataMap["date"] = resultDto.date

            Napier.i("Cloud Function successfully scanned invoice: $metadataMap")
            ScanResult(
                metadata = metadataMap,
                documentUrl = documentUrl
            )
        } catch (e: Exception) {
            Napier.e("Error in FirebaseInvoiceScannerService", e)
            null
        }
    }
}
