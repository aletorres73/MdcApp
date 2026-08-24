package com.mdcapp.data.repositories

import com.mdcapp.domain.repositories.DatabaseQuery
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
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.Duration.Companion.milliseconds

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
        parameter("key", apiKey)
        if (!token.isNullOrBlank() && token != "null") {
            val cleanToken = token.replace("\n", "").replace("\r", "").trim()
            header("Authorization", "Bearer $cleanToken")
        }
    }

    override suspend fun <T : Any> getDocument(path: String, serializer: KSerializer<T>): T? {
        val token = authRepository.idToken
        return try {
            val response = client.get("$baseUrl/$path") {
                applyAuth(token)
            }
            if (response.status == HttpStatusCode.OK) {
                val firestoreDoc = response.body<JsonObject>()
                val flattenedJson = flattenFirestoreDocument(firestoreDoc)
                json.decodeFromJsonElement(serializer, flattenedJson)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun <T : Any> getCollection(
        path: String,
        serializer: KSerializer<T>,
        query: DatabaseQuery?
    ): List<T> {
        val token = authRepository.idToken
        val startTime = System.currentTimeMillis()

        return try {
            if (query == null) {
                println("🔍 [Firestore JVM] GET Collection (Full): $path")
                val response = client.get("$baseUrl/$path") {
                    applyAuth(token)
                }

                if (response.status == HttpStatusCode.OK) {
                    val root = response.body<JsonObject>()
                    val documents = root["documents"]?.jsonArray ?: emptyList()
                    println("✅ [Firestore JVM] Response: OK (${documents.size} docs) in ${System.currentTimeMillis() - startTime}ms")
                    documents.map { doc ->
                        val flattened = flattenFirestoreDocument(doc.jsonObject)
                        json.decodeFromJsonElement(serializer, flattened)
                    }
                } else {
                    val errorBody = response.bodyAsText()
                    println("❌ [Firestore JVM] Error: ${response.status} - $errorBody")
                    emptyList()
                }
            } else {
                // Endpoint runQuery
                val parentPath = if (path.contains("/")) path.substringBeforeLast("/") else ""
                val collectionId = if (path.contains("/")) path.substringAfterLast("/") else path

                // Firestore REST runQuery endpoint logic:
                // If path is "users/UID/allBillings"
                // parentPath = "users/UID"
                // collectionId = "allBillings"
                // URL should be: baseUrl/users/UID:runQuery
                val runQueryUrl =
                    if (parentPath.isEmpty()) "$baseUrl:runQuery" else "$baseUrl/$parentPath:runQuery"

                val structuredQuery = buildStructuredQuery(collectionId, query)

                println("🔍 [Firestore JVM] POST :runQuery on $path")
                println(
                    "📦 [Payload] ${
                        json.encodeToString(
                            JsonObject.serializer(),
                            structuredQuery
                        )
                    }"
                )

                val response = client.post(runQueryUrl) {
                    applyAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody(structuredQuery)
                }

                if (response.status == HttpStatusCode.OK) {
                    val results = response.body<JsonArray>()
                    println("✅ [Firestore JVM] Response: OK (${results.size} docs) in ${System.currentTimeMillis() - startTime}ms")

                    results.mapNotNull { result ->
                        val doc =
                            result.jsonObject["document"]?.jsonObject ?: return@mapNotNull null
                        val flattened = flattenFirestoreDocument(doc)
                        json.decodeFromJsonElement(serializer, flattened)
                    }
                } else {
                    val errorBody = response.bodyAsText()
                    println("❌ [Firestore JVM] Query Error: ${response.status} - $errorBody")
                    emptyList()
                }
            }
        } catch (e: Exception) {
            println("💥 [Firestore JVM] Exception: ${e.message}")
            emptyList()
        }
    }

    private fun buildStructuredQuery(collectionId: String, query: DatabaseQuery): JsonObject {
        return buildJsonObject {
            put("structuredQuery", buildJsonObject {
                put("from", buildJsonArray {
                    add(buildJsonObject {
                        put("collectionId", JsonPrimitive(collectionId))
                    })
                })

                if (query.filterBy != null && query.equalTo != null) {
                    put("where", buildJsonObject {
                        put("fieldFilter", buildJsonObject {
                            put(
                                "field",
                                buildJsonObject {
                                    put(
                                        "fieldPath",
                                        JsonPrimitive(quoteFieldPath(query.filterBy))
                                    )
                                })
                            put("op", JsonPrimitive("EQUAL"))
                            put("value", wrapValue(JsonPrimitive(query.equalTo)))
                        })
                    })
                }

                if (query.orderBy != null) {
                    put("orderBy", buildJsonArray {
                        add(buildJsonObject {
                            put(
                                "field",
                                buildJsonObject {
                                    put(
                                        "fieldPath",
                                        JsonPrimitive(quoteFieldPath(query.orderBy))
                                    )
                                })
                            put(
                                "direction",
                                JsonPrimitive(if (query.descending) "DESCENDING" else "ASCENDING")
                            )
                        })
                    })
                }

                if (query.limit != null) {
                    put("limit", JsonPrimitive(query.limit))
                }
            })
        }
    }

    private fun quoteFieldPath(path: String): String {
        val segments = path.split('.')
        return segments.joinToString(".") { segment ->
            if (segment.matches(Regex("[a-zA-Z_][a-zA-Z_0-9]*"))) {
                segment
            } else {
                "`$segment`"
            }
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
                    element.content == "true" || element.content == "false" ->
                        JsonObject(mapOf("booleanValue" to JsonPrimitive(element.content.toBoolean())))

                    element.content.contains(".") ->
                        JsonObject(mapOf("doubleValue" to JsonPrimitive(element.content.toDouble())))

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
        try {
            val jsonElement = json.encodeToJsonElement(serializer, data)
            val firestoreDoc =
                JsonObject(mapOf("fields" to JsonObject(jsonElement.jsonObject.mapValues {
                    wrapValue(it.value)
                })))

            client.patch("$baseUrl/$path") {
                applyAuth(token)
                contentType(ContentType.Application.Json)
                setBody(firestoreDoc)
            }
        } catch (e: Exception) {
        }
    }

    override suspend fun <T : Any> updateDocument(
        path: String,
        data: Any,
        serializer: KSerializer<T>?
    ) {
        val token = authRepository.idToken
        try {
            val jsonElement = if (serializer != null) {
                @Suppress("UNCHECKED_CAST")
                json.encodeToJsonElement(serializer as KSerializer<Any>, data)
            } else if (data is Map<*, *>) {
                @Suppress("UNCHECKED_CAST")
                json.encodeToJsonElement(data as Map<String, Any?>)
            } else {
                return
            }

            val fields = JsonObject(jsonElement.jsonObject.mapValues { wrapValue(it.value) })
            val firestoreDoc = JsonObject(mapOf("fields" to fields))
            val updateMaskParams =
                jsonElement.jsonObject.keys.joinToString("&") {
                    "updateMask.fieldPaths=${
                        quoteFieldPath(
                            it
                        )
                    }"
                }

            client.patch("$baseUrl/$path?$updateMaskParams") {
                applyAuth(token)
                contentType(ContentType.Application.Json)
                setBody(firestoreDoc)
            }
        } catch (e: Exception) {
        }
    }

    override suspend fun deleteDocument(path: String) {
        val token = authRepository.idToken
        try {
            client.delete("$baseUrl/$path") {
                applyAuth(token)
            }
        } catch (e: Exception) {
        }
    }

    override suspend fun <T : Any> addDocument(
        path: String,
        data: T,
        serializer: KSerializer<T>
    ): String {
        val token = authRepository.idToken
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
                name.substringAfterLast("/")
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    override fun <T : Any> observeDocument(path: String, serializer: KSerializer<T>): Flow<T?> =
        flow {
            while (true) {
                try {
                    emit(getDocument(path, serializer))
                } catch (e: Exception) {
                }
                delay(30000.milliseconds)
            }
        }

    override fun <T : Any> observeCollection(
        path: String,
        serializer: KSerializer<T>,
        query: DatabaseQuery?
    ): Flow<List<T>> = flow {
        while (true) {
            try {
                emit(getCollection(path, serializer, query))
            } catch (e: Exception) {
            }
            delay(30000.milliseconds)
        }
    }
}
