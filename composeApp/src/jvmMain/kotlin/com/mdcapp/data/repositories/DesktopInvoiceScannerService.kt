package com.mdcapp.data.repositories

import com.mdcapp.domain.repositories.IInvoiceScannerService

class DesktopInvoiceScannerService : IInvoiceScannerService {
    override suspend fun scanInvoice(fileBytes: ByteArray, mimeType: String): Map<String, String>? {
        // Desktop doesn't have Vertex AI Firebase SDK out of the box,
        // and we are not implementing the direct REST call for now.
        return null
    }
}