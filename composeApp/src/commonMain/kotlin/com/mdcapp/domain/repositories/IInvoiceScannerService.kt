package com.mdcapp.domain.repositories

interface IInvoiceScannerService {
    /**
     * Scans an image/PDF (provided as a ByteArray) and extracts invoice information.
     * Returns a map with keys like "number", "date" (epoch millis), "total", "type" (Factura/Remito)
     */
    suspend fun scanInvoice(fileBytes: ByteArray, mimeType: String): Map<String, String>?
}