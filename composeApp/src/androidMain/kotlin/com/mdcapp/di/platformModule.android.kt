package com.mdcapp.di

import com.mdcapp.data.repositories.AndroidAuthRepository
import com.mdcapp.data.repositories.AndroidDatabaseRepository
import com.mdcapp.data.repositories.AndroidStorageRepository
import com.mdcapp.data.repositories.FirebaseInvoiceScannerService
import com.mdcapp.data.service.AndroidSessionManager
import com.mdcapp.domain.repositories.IAuthRepository
import com.mdcapp.domain.repositories.IDatabaseRepository
import com.mdcapp.domain.repositories.IInvoiceScannerService
import com.mdcapp.domain.repositories.IStorageRepository
import com.mdcapp.domain.service.ISessionManager
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.storage.storage
import org.koin.dsl.module

actual val platformModule = module {
    single { Firebase.auth }
    single { Firebase.firestore }
    single { Firebase.storage }
    single<ISessionManager> { AndroidSessionManager(get()) }
    single<IAuthRepository> { AndroidAuthRepository(get(), get()) }
    single<IDatabaseRepository> { AndroidDatabaseRepository(get()) }
    single<IStorageRepository> { AndroidStorageRepository(get()) }
    single<IInvoiceScannerService> { FirebaseInvoiceScannerService(get(), get()) }
}
