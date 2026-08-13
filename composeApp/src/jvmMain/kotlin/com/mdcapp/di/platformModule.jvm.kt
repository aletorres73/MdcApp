package com.mdcapp.di

import com.mdcapp.data.repositories.DesktopAuthRepository
import com.mdcapp.data.repositories.DesktopDatabaseRepository
import com.mdcapp.domain.repositories.IAuthRepository
import com.mdcapp.domain.repositories.IDatabaseRepository
import com.mdcapp.domain.repositories.IStorageRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

actual val platformModule = module {
    single {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            install(Logging) {
                level = LogLevel.INFO
            }
        }
    }
    single { DesktopAuthRepository(get()) }
    single<IAuthRepository> { get<DesktopAuthRepository>() }
    single<IDatabaseRepository> { DesktopDatabaseRepository(get(), get()) }
    single<IStorageRepository> {
        object : IStorageRepository {
            override suspend fun uploadFile(path: String, data: ByteArray): String = path
            override suspend fun getDownloadUrl(path: String): String = ""
        }
    }
}
