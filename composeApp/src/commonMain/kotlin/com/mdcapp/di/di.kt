package com.mdcapp.di

import com.mdcapp.domain.remote.HomeRepository
import com.mdcapp.domain.remote.OrderRepository
import com.mdcapp.domain.service.HomeService
import com.mdcapp.domain.service.OrderService
import com.mdcapp.domain.usescases.homeusescases.HomeUseCase
import com.mdcapp.domain.usescases.ordersusescases.BuyOrderUseCase
import com.mdcapp.domain.usescases.ordersusescases.OrdersUseCase
import com.mdcapp.ui.viewmodels.HomeViewModel
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


    single<HomeUseCase.GetAllFactories> { get<HomeUseCase>().GetAllFactories() }
    single<OrdersUseCase.GetAllOrders> { get<OrdersUseCase>().GetAllOrders() }
    single<BuyOrderUseCase.GetBuyOrderById> { get<BuyOrderUseCase>().GetBuyOrderById() }
    single<BuyOrderUseCase.GetBillings> { get<BuyOrderUseCase>().GetBillings() }
}

val dataModule = module {
    factoryOf(::OrderService)
    factoryOf(::HomeService)

    factoryOf(::OrderRepository)
    factoryOf(::HomeRepository)

    factoryOf(::OrdersUseCase)
    factoryOf(::BuyOrderUseCase)
    factoryOf(::HomeUseCase)
}

val viewModelModule = module {
    viewModelOf(::OrdersViewModel)
    viewModelOf(::BuyOrdersViewModel)
    viewModelOf(::HomeViewModel)
}

//expect val nativeModule: Module

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(appModule, dataModule, viewModelModule)
    }
}
