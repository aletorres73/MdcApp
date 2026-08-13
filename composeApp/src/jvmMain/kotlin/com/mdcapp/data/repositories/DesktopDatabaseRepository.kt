package com.mdcapp.data.repositories

import com.mdcapp.domain.repositories.IDatabaseRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.Base64

class DesktopDatabaseRepository(
    private val client: HttpClient,
    private val authRepository: DesktopAuthRepository
) : IDatabaseRepository {

    private val projectId = "database-rw-60033"
    private val apiKey = "AIzaSyC8RSmswZFBr4IhOgjtTyxH0GojOtu9F8k"
    private val baseUrl =
        "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents"
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private fun HttpRequestBuilder.applyAuth(token: String?) {
        // La API Key identifica el proyecto en la petición REST
        parameter("key", apiKey)

        if (!token.isNullOrBlank() && token != "null") {
            val cleanToken = token.replace("\n", "").replace("\r", "").trim()

            // Con un Token de Firebase "Real" (emisor securetoken), 
            // el patrón estándar (API Key + Authorization Bearer) debería funcionar.
            header("Authorization", "Bearer $cleanToken")

            // Diagnóstico de JWT (Mantenemos tus logs)
            try {
                val parts = cleanToken.split(".")
                if (parts.size > 1) {
                    val payload = String(Base64.getDecoder().decode(parts[1]))
                    println("📊 [JWT DEBUG] Payload: $payload")
                }
            } catch (e: Exception) {
                println("⚠️ [Firestore JVM] Error decoding JWT for debug: ${e.message}")
            }

            println("🔑 [Firestore JVM] Standard Auth: API Key + Bearer Token applied.")

        } else {
            println("⚠️ [Firestore JVM] Auth Token is null or blank. Using API Key only.")
        }
    }

    override suspend fun <T : Any> getDocument(path: String, serializer: KSerializer<T>): T? {
        val token = authRepository.idToken
        println("🔍 [Firestore JVM] GET Document: $path")

        return try {
            val response = client.get("$baseUrl/$path") {
                applyAuth(token)
            }

            if (response.status == HttpStatusCode.OK) {
                val firestoreDoc = response.body<JsonObject>()
                val flattenedJson = flattenFirestoreDocument(firestoreDoc)
                println("✅ [Firestore JVM] Document Fetched: $path")
                json.decodeFromJsonElement(serializer, flattenedJson)
            } else {
                val errorBody = response.bodyAsText()
                println("❌ [Firestore JVM] Error Fetching Document: $path - Status: ${response.status}")
                println("📄 [Firestore JVM] Error Body: $errorBody")
                null
            }
        } catch (e: Exception) {
            println("💥 [Firestore JVM] Exception Fetching Document: $path - ${e.message}")
            null
        }
    }

    override suspend fun <T : Any> getCollection(
        path: String,
        serializer: KSerializer<T>
    ): List<T> {
        val token = authRepository.idToken
        println("🔍 [Firestore JVM] GET Collection: $path")

        return try {
            val response = client.get("$baseUrl/$path") {
                applyAuth(token)
            }

            if (response.status == HttpStatusCode.OK) {
                val root = response.body<JsonObject>()
                val documents = root["documents"]?.jsonArray ?: return emptyList()
                println("✅ [Firestore JVM] Collection Fetched: $path (${documents.size} docs)")
                documents.map { doc ->
                    val flattened = flattenFirestoreDocument(doc.jsonObject)
                    json.decodeFromJsonElement(serializer, flattened)
                }
            } else {
                val errorBody = response.bodyAsText()
                println("❌ [Firestore JVM] Error Fetching Collection: $path - Status: ${response.status}")
                println("📄 [Firestore JVM] Error Body: $errorBody")
                emptyList()
            }
        } catch (e: Exception) {
            println("💥 [Firestore JVM] Exception Fetching Collection: $path - ${e.message}")
            emptyList()
        }
    }

    private fun flattenFirestoreDocument(doc: JsonObject): JsonElement {
        val fields = doc["fields"]?.jsonObject ?: return JsonObject(emptyMap())
        return JsonObject(fields.mapValues { (_, value) -> flattenValue(value.jsonObject) })
    }

    private fun flattenValue(valueObj: JsonObject): JsonElement {
        return when {
            "stringValue" in valueObj -> valueObj["stringValue"]!!
            "doubleValue" in valueObj -> valueObj["doubleValue"]!!
            "integerValue" in valueObj -> valueObj["integerValue"]!!
            "booleanValue" in valueObj -> valueObj["booleanValue"]!!
            "timestampValue" in valueObj -> valueObj["timestampValue"]!!
            "mapValue" in valueObj -> {
                val nestedFields =
                    valueObj["mapValue"]?.jsonObject?.get("fields")?.jsonObject ?: return JsonNull
                JsonObject(nestedFields.mapValues { (_, v) -> flattenValue(v.jsonObject) })
            }

            "arrayValue" in valueObj -> {
                val values = valueObj["arrayValue"]?.jsonObject?.get("values")?.jsonArray
                    ?: return JsonArray(emptyList())
                JsonArray(values.map { flattenValue(it.jsonObject) })
            }

            else -> JsonNull
        }
    }

    private fun wrapValue(element: JsonElement): JsonObject {
        return when (element) {
            is JsonPrimitive -> {
                when {
                    element.isString -> JsonObject(mapOf("stringValue" to element))
                    element.content == "true" || element.content == "false" -> JsonObject(
                        mapOf(
                            "booleanValue" to JsonPrimitive(
                                element.content.toBoolean()
                            )
                        )
                    )

                    element.content.contains(".") -> JsonObject(
                        mapOf(
                            "doubleValue" to JsonPrimitive(
                                element.content.toDouble()
                            )
                        )
                    )

                    else -> JsonObject(mapOf("integerValue" to JsonPrimitive(element.content)))
                }
            }

            is JsonObject -> {
                val fields = JsonObject(element.mapValues { wrapValue(it.value) })
                JsonObject(mapOf("mapValue" to JsonObject(mapOf("fields" to fields))))
            }

            is JsonArray -> {
                val values = JsonArray(element.map { wrapValue(it) })
                JsonObject(mapOf("arrayValue" to JsonObject(mapOf("values" to values))))
            }

            else -> JsonObject(mapOf("nullValue" to JsonNull))
        }
    }

    override suspend fun <T : Any> setDocument(path: String, data: T, serializer: KSerializer<T>) {
        val token = authRepository.idToken
        println("📤 [Firestore JVM] SET Document: $path")
        try {
            val jsonElement = json.encodeToJsonElement(serializer, data)
            val firestoreDoc =
                JsonObject(mapOf("fields" to JsonObject(jsonElement.jsonObject.mapValues {
                    wrapValue(it.value)
                })))

            val response = client.patch("$baseUrl/$path") {
                applyAuth(token)
                contentType(ContentType.Application.Json)
                setBody(firestoreDoc)
            }
            if (response.status == HttpStatusCode.OK) {
                println("✅ [Firestore JVM] Document Set: $path")
            } else {
                println("❌ [Firestore JVM] Error Setting Document: $path - Status: ${response.status}")
                println("📄 [Firestore JVM] Error Body: ${response.bodyAsText()}")
            }
        } catch (e: Exception) {
            println("💥 [Firestore JVM] Exception Setting Document: $path - ${e.message}")
        }
    }

    override suspend fun <T : Any> updateDocument(
        path: String,
        data: Any,
        serializer: KSerializer<T>?
    ) {
        val token = authRepository.idToken
        println("📤 [Firestore JVM] UPDATING Document: $path")
        try {
            val jsonElement = if (serializer != null) {
                @Suppress("UNCHECKED_CAST")
                json.encodeToJsonElement(serializer as KSerializer<Any>, data)
            } else if (data is Map<*, *>) {
                @Suppress("UNCHECKED_CAST")
                json.encodeToJsonElement(data as Map<String, Any?>)
            } else {
                throw Exception("Serializer or Map required for update")
            }

            val fields = JsonObject(jsonElement.jsonObject.mapValues { wrapValue(it.value) })
            val firestoreDoc = JsonObject(mapOf("fields" to fields))

            val updateMaskParams =
                jsonElement.jsonObject.keys.joinToString("&") { "updateMask.fieldPaths=$it" }

            val response = client.patch("$baseUrl/$path?$updateMaskParams") {
                applyAuth(token)
                contentType(ContentType.Application.Json)
                setBody(firestoreDoc)
            }
            if (response.status == HttpStatusCode.OK) {
                println("✅ [Firestore JVM] Document Updated: $path")
            } else {
                println("❌ [Firestore JVM] Error Updating Document: $path - Status: ${response.status}")
                println("📄 [Firestore JVM] Error Body: ${response.bodyAsText()}")
            }
        } catch (e: Exception) {
            println("💥 [Firestore JVM] Exception Updating Document: $path - ${e.message}")
        }
    }

    override suspend fun deleteDocument(path: String) {
        val token = authRepository.idToken
        println("🗑️ [Firestore JVM] DELETING Document: $path")
        try {
            val response = client.delete("$baseUrl/$path") {
                applyAuth(token)
            }
            if (response.status == HttpStatusCode.OK) {
                println("✅ [Firestore JVM] Document Deleted: $path")
            } else {
                println("❌ [Firestore JVM] Error Deleting Document: $path - Status: ${response.status}")
            }
        } catch (e: Exception) {
            println("💥 [Firestore JVM] Exception Deleting Document: $path - ${e.message}")
        }
    }

    override suspend fun <T : Any> addDocument(
        path: String,
        data: T,
        serializer: KSerializer<T>
    ): String {
        val token = authRepository.idToken
        println("📤 [Firestore JVM] ADDING Document to: $path")
        return try {
            val jsonElement = json.encodeToJsonElement(serializer, data)
            val firestoreDoc =
                JsonObject(mapOf("fields" to JsonObject(jsonElement.jsonObject.mapValues {
                    wrapValue(it.value)
                })))

            val response = client.post("$baseUrl/$path") {
                applyAuth(token)
                contentType(ContentType.Application.Json)
                setBody(firestoreDoc)
            }
            if (response.status == HttpStatusCode.OK) {
                val newDoc = response.body<JsonObject>()
                val name = newDoc["name"]?.jsonPrimitive?.content ?: ""
                val id = name.substringAfterLast("/")
                println("✅ [Firestore JVM] Document Added: $id")
                id
            } else {
                println("❌ [Firestore JVM] Error Adding Document: $path - Status: ${response.status}")
                ""
            }
        } catch (e: Exception) {
            println("💥 [Firestore JVM] Exception Adding Document: $path - ${e.message}")
            ""
        }
    }

    override fun <T : Any> observeDocument(path: String, serializer: KSerializer<T>): Flow<T?> =
        flow {
            while (true) {
                try {
                    emit(getDocument(path, serializer))
                } catch (e: Exception) {
                    println("⚠️ [Firestore JVM] Error in observeDocument ($path): ${e.message}")
                }
                delay(30000) // Polling cada 30 segundos
            }
        }

    override fun <T : Any> observeCollection(
        path: String,
        serializer: KSerializer<T>
    ): Flow<List<T>> = flow {
        while (true) {
            try {
                emit(getCollection(path, serializer))
            } catch (e: Exception) {
                println("⚠️ [Firestore JVM] Error in observeCollection ($path): ${e.message}")
            }
            delay(30000) // Polling cada 30 segundos
        }
    }
}
