package com.mdcapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mdcapp.domain.logic.SharedDocument
import com.mdcapp.domain.logic.SharedDocumentHandler
import com.mdcapp.ui.PlatformNavigation
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)

        setContent {
            PlatformNavigation()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND) {
            val uri: Uri? =
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_STREAM)
                } ?: intent.clipData?.getItemAt(0)?.uri

            val rawMimeType = intent.type
            if (uri != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // Resolve actual MIME type from ContentResolver if needed
                        val resolvedMimeType = contentResolver.getType(uri) ?: rawMimeType ?: ""

                        // Resolve actual file display name from ContentResolver
                        var fileName = ""
                        try {
                            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                if (nameIndex != -1 && cursor.moveToFirst()) {
                                    fileName = cursor.getString(nameIndex) ?: ""
                                }
                            }
                        } catch (e: Exception) {
                            Napier.w("Could not resolve display name from ContentResolver", e)
                        }

                        if (fileName.isBlank()) {
                            fileName = "shared_document_${System.currentTimeMillis()}"
                        }

                        // Read bytes from ContentResolver
                        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }

                        if (bytes != null && bytes.isNotEmpty()) {
                            Napier.i("Successfully read ${bytes.size} bytes for shared file: $fileName ($resolvedMimeType)")
                            SharedDocumentHandler.setPendingDocument(
                                SharedDocument(
                                    fileBytes = bytes,
                                    mimeType = resolvedMimeType,
                                    fileName = fileName
                                )
                            )
                        } else {
                            Napier.e("Failed to read bytes from URI: $uri")
                        }
                    } catch (e: Exception) {
                        Napier.e("Error processing shared URI from Intent", e)
                    }
                }
            }
        }
    }
}
