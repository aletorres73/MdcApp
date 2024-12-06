package com.mdcapp.di

import com.mdcapp.domain.remote.OrderRepository
import com.mdcapp.domain.service.OrderService
import com.mdcapp.domain.usescases.HandlersUsesCases
import com.mdcapp.domain.usescases.OrdersUseCase
import com.mdcapp.ui.viewmodels.orders.OrdersViewModel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule = module {
    single<FirebaseFirestore> { Firebase.firestore }
    single<OrdersUseCase.GetAllOrders> { get<OrdersUseCase>().GetAllOrders() }
//    single<HandlersUsesCases> {get<HandlersUsesCases>()}
}

val dataModule = module {
    factoryOf(::OrderService)
    factoryOf(::OrderRepository)
    factoryOf(::OrdersUseCase)
    singleOf(::HandlersUsesCases)
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
