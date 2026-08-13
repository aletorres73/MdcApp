package com.mdcapp.data.repositories

import com.mdcapp.domain.repositories.IStorageRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

class DesktopStorageRepository(
    private val client: HttpClient,
    private val authRepository: DesktopAuthRepository
) : IStorageRepository {

    private val bucket = "database-rw-60033.firebasestorage.app"
    private val baseUrl = "https://firebasestorage.googleapis.com/v0/b/$bucket/o"
    private val apiKey = "AIzaSyC8RSmswZFBr4IhOgjtTyxH0GojOtu9F8k"

    private fun HttpRequestBuilder.applyAuth(token: String?) {
        // La API Key identifica el proyecto en Firebase Storage REST
        parameter("key", apiKey)

        if (!token.isNullOrBlank() && token != "null") {
            val cleanToken = token.replace("\n", "").replace("\r", "").trim()
            header("Authorization", "Bearer $cleanToken")
        }
    }

    override suspend fun uploadFile(path: String, data: ByteArray): String {
        // Implementación básica de subida si se requiere en el futuro
        // La API REST de Storage es más compleja para multipart, por ahora devolvemos el path
        return path
    }

    override suspend fun getDownloadUrl(path: String): String {
        val encodedPath = path.replace("/", "%2F")
        val token = authRepository.idToken

        return try {
            println("🔍 [Storage JVM] Getting Metadata for: $path")
            val response = client.get("$baseUrl/$encodedPath") {
                applyAuth(token)
            }

            if (response.status == HttpStatusCode.OK) {
                val metadata = response.body<JsonObject>()
                val downloadTokens = metadata["downloadTokens"]?.jsonPrimitive?.content
                if (downloadTokens != null) {
                    val url = "$baseUrl/$encodedPath?alt=media&token=$downloadTokens"
                    println("✅ [Storage JVM] Download URL Fetched")
                    url
                } else {
                    println("⚠️ [Storage JVM] No download tokens found for: $path")
                    ""
                }
            } else {
                println("❌ [Storage JVM] Error fetching metadata: ${response.status}")
                ""
            }
        } catch (e: Exception) {
            println("💥 [Storage JVM] Exception fetching metadata: ${e.message}")
            ""
        }
    }
}
