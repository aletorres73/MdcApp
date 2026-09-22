package com.mdcapp.data.repositories

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.core.graphics.createBitmap
import com.google.firebase.Firebase
import com.google.firebase.vertexai.type.content
import com.google.firebase.vertexai.vertexAI
import com.mdcapp.domain.repositories.IInvoiceScannerService
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class AndroidInvoiceScannerService : IInvoiceScannerService {
    override suspend fun scanInvoice(fileBytes: ByteArray, mimeType: String): Map<String, String>? {
        return withContext(Dispatchers.IO) {
            try {
                Napier.i("Starting scanInvoice with file size: ${fileBytes.size} bytes, mimeType: $mimeType")

                // Initialize the generative model
                val generativeModel = Firebase.vertexAI.generativeModel(
                    modelName = "gemini-2.5-flash",
                    systemInstruction = content { text("Eres un asistente contable de Argentina. Devuelve la salida SIEMPRE en formato JSON puro (sin markdown).") }
                )

                var finalBitmap: Bitmap? = null

                // Detect PDF by MIME type or magic bytes (%PDF)
                val isPdf = mimeType.contains("pdf", ignoreCase = true) ||
                        (fileBytes.size >= 4 &&
                                fileBytes[0] == 0x25.toByte() &&
                                fileBytes[1] == 0x50.toByte() &&
                                fileBytes[2] == 0x44.toByte() &&
                                fileBytes[3] == 0x46.toByte())

                if (isPdf) {
                    Napier.i("Document detected as PDF. Rendering page 0 to Bitmap...")
                    val tempFile = File.createTempFile("temp_pdf", ".pdf")
                    FileOutputStream(tempFile).use { it.write(fileBytes) }

                    val fileDescriptor =
                        ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
                    val pdfRenderer = PdfRenderer(fileDescriptor)
                    if (pdfRenderer.pageCount > 0) {
                        val page = pdfRenderer.openPage(0)
                        val bitmap = createBitmap(page.width * 2, page.height * 2)
                        bitmap.eraseColor(android.graphics.Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        finalBitmap = bitmap
                        page.close()
                    }
                    pdfRenderer.close()
                    fileDescriptor.close()
                    tempFile.delete()
                } else {
                    Napier.i("Document detected as Image. Decoding ByteArray to Bitmap...")
                    finalBitmap = BitmapFactory.decodeByteArray(fileBytes, 0, fileBytes.size)
                }

                if (finalBitmap == null) {
                    Napier.e("Failed to decode bitmap from file bytes.")
                    return@withContext null
                }

                val prompt = """
                    Analiza esta imagen y extrae la siguiente información de la factura o remito en formato JSON con estas claves exactas:
                    - "number": El número del documento (ej. 0001-00001234). String vacio si no se encuentra.
                    - "total": El monto final a pagar. Solo el numero sin signo de dolar. String vacio si no se encuentra.
                    - "type": Debe ser "Factura" o "Remito". Si no dice, asume "Remito".
                    - "date": La fecha de emision o entrega en formato "yyyy-MM-dd".
                """.trimIndent()

                Napier.i("Sending request to Vertex AI Gemini model...")
                val response = generativeModel.generateContent(
                    content {
                        image(finalBitmap)
                        text(prompt)
                    }
                )

                val responseText =
                    response.text?.trim()?.removePrefix("```json")?.removeSuffix("```")?.trim()
                Napier.i("Vertex AI Response: $responseText")

                if (responseText != null) {
                    val json = JSONObject(responseText)
                    val map = mutableMapOf<String, String>()
                    if (json.has("number")) map["number"] = json.getString("number")
                    if (json.has("total")) map["total"] = json.getString("total")
                    if (json.has("type")) map["type"] = json.getString("type")
                    if (json.has("date")) map["date"] = json.getString("date")
                    return@withContext map
                }
                null
            } catch (e: com.google.firebase.vertexai.type.ServiceDisabledException) {
                Napier.e("Vertex AI API is disabled in Firebase Console", e)
                throw Exception("La API de Vertex AI está deshabilitada en la consola de Firebase para el proyecto. Habilítala desde Firebase Console > Vertex AI (Build con IA).")
            } catch (e: com.google.firebase.vertexai.type.ServerException) {
                Napier.e("Server error in Vertex AI API (App Check or backend)", e)
                if (e.message?.contains("App Check", ignoreCase = true) == true) {
                    throw Exception("Error de App Check en Firebase: Token no válido. Si estás en desarrollo, busca en Logcat 'DebugAppCheckProvider' y registra la clave en Firebase Console > App Check.")
                }
                throw e
            } catch (e: Exception) {
                Napier.e("Error in scanInvoice with Vertex AI", e)
                throw e
            }
        }
    }
}