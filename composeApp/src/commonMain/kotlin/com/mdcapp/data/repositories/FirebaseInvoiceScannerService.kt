package com.mdcapp.data.repositories

import com.mdcapp.domain.repositories.IDatabaseRepository
import com.mdcapp.domain.repositories.IInvoiceScannerService
import com.mdcapp.domain.repositories.IStorageRepository
import com.mdcapp.domain.repositories.ScanResult
import io.github.aakira.napier.Napier
import kotlinx.coroutines.delay
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
    val error: String? = null
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
            val cleanFileName =
                if (fileName.isBlank()) "doc_$timestamp" else fileName.replace(" ", "_")

            val extension = cleanFileName.substringAfterLast('.', "")
            val extPath = if (extension.isNotEmpty()) ".$extension" else ""
            val nameWithoutExt =
                if (cleanFileName.contains('.')) cleanFileName.substringBeforeLast('.') else cleanFileName

            val storageFileName = "${nameWithoutExt}_$timestamp$extPath"
            val storagePath = "invoices/$cleanClient/$storageFileName"

            val documentId =
                storageFileName.substringBeforeLast('.').replace(Regex("[^a-zA-Z0-9_-]"), "_")

            Napier.i("Uploading file to Storage: $storagePath (documentId: $documentId)")
            val documentUrl = storageRepository.uploadFile(storagePath, fileBytes)

            Napier.i("Waiting for Cloud Function processing at scan_results/$documentId")
            val scanPath = "scan_results/$documentId"

            val resultDto = withTimeoutOrNull(35_000L.milliseconds) {
                var dto: ScanResultDto?
                while (true) {
                    dto = databaseRepository.getDocument(scanPath, ScanResultDto.serializer())
                    if (dto?.status == "completed" || dto?.status == "error") {
                        break
                    }
                    delay(1000L.milliseconds)
                }
                dto
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
