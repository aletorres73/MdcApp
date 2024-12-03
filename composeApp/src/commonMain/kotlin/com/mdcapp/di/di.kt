package com.mdcapp.di

import com.mdcapp.domain.remote.OrderRepository
import com.mdcapp.domain.service.OrderService
import com.mdcapp.domain.usescases.GetAllOrdersUseCase
import com.mdcapp.ui.viewmodels.orders.OrdersViewModel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.CollectionReference
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule = module {
    single<FirebaseFirestore> { Firebase.firestore }
}

val dataModule = module {
    factoryOf(::OrderService)
    factoryOf(::OrderRepository)
    factoryOf(::GetAllOrdersUseCase)
}

val viewModelModule = module {
    viewModelOf(::OrdersViewModel)
}

//expect val nativeModule: Module

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(appModule, dataModule, viewModelModule)
    }
}

