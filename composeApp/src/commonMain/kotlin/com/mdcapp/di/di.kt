package com.mdcapp.di

import com.mdcapp.domain.remote.OrderRepository
import com.mdcapp.domain.service.OrderService
import com.mdcapp.domain.usescases.handlerusescases.HandlersUsesCases
import com.mdcapp.domain.usescases.ordersusescases.BuyOrderUseCase
import com.mdcapp.domain.usescases.ordersusescases.OrdersUseCase
import com.mdcapp.ui.viewmodels.buyorders.BuyOrdersViewModel
import com.mdcapp.ui.viewmodels.orders.OrdersViewModel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule = module {
    single<FirebaseFirestore> { Firebase.firestore }
    single<OrdersUseCase.GetAllOrders> { get<OrdersUseCase>().GetAllOrders() }
    single<BuyOrderUseCase.GetBuyOrderById> { get<BuyOrderUseCase>().GetBuyOrderById() }
}

val dataModule = module {
    factoryOf(::OrderService)
    factoryOf(::OrderRepository)
    factoryOf(::OrdersUseCase)
    factoryOf(::BuyOrderUseCase)
    factoryOf(::HandlersUsesCases)
}

val viewModelModule = module {
    viewModelOf(::OrdersViewModel)
    viewModelOf(::BuyOrdersViewModel)
}

//expect val nativeModule: Module

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(appModule, dataModule, viewModelModule)
    }
}
