package com.mdcapp.domain.repositories

data class ScanResult(
    val metadata: Map<String, String>,
    val documentUrl: String
)

interface IInvoiceScannerService {
    /**
     * Uploads the file to Firebase Storage and listens for the Cloud Function (Vertex AI)
     * scan result in Firestore.
     * Returns a [ScanResult] containing the extracted metadata and the uploaded document URL.
     */
    suspend fun scanInvoice(
        fileBytes: ByteArray,
        mimeType: String,
        fileName: String,
        clientName: String = "general"
    ): ScanResult?
}
