package com.mdcapp.di

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.mdcapp.data.service.ClientService
import com.mdcapp.data.service.HomeService
import com.mdcapp.data.service.OrderService
import com.mdcapp.domain.repositories.HomeRepository
import com.mdcapp.domain.repositories.OrderRepository
import com.mdcapp.domain.usescases.clientsusecase.GetClientsUseCase
import com.mdcapp.domain.usescases.homeusescases.HomeUseCase
import com.mdcapp.domain.usescases.homeusescases.PaymentConditionsUseCase
import com.mdcapp.domain.usescases.ordersusescases.BuyOrderUseCase
import com.mdcapp.domain.usescases.ordersusescases.GetFactoriesListUseCase
import com.mdcapp.domain.usescases.ordersusescases.OrdersUseCase
import com.mdcapp.ui.viewmodels.ClientsViewModel
import com.mdcapp.ui.viewmodels.HomeViewModel
import com.mdcapp.ui.viewmodels.buyorders.BuyOrdersViewModel
import com.mdcapp.ui.viewmodels.orders.OrdersViewModel
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule = module {
    single<FirebaseFirestore> { Firebase.firestore }

    single<HomeUseCase.GetAllFactories> { get<HomeUseCase>().GetAllFactories() }

    single<OrdersUseCase.GetAllOrders> { get<OrdersUseCase>().GetAllOrders() }
    single<OrdersUseCase.GetOrdersByFactory> { get<OrdersUseCase>().GetOrdersByFactory() }
    single<OrdersUseCase.GetOrderBranch> { get<OrdersUseCase>().GetOrderBranch() }

    single<BuyOrderUseCase.GetBuyOrderById> { get<BuyOrderUseCase>().GetBuyOrderById() }
    single<BuyOrderUseCase.GetBillings> { get<BuyOrderUseCase>().GetBillings() }
    single<BuyOrderUseCase.AddPaymentToRegister> { get<BuyOrderUseCase>().AddPaymentToRegister() }
    single<BuyOrderUseCase.GetLastIdPaymentFromRegister> { get<BuyOrderUseCase>().GetLastIdPaymentFromRegister() }
    single<BuyOrderUseCase.UpdateBilling> { get<BuyOrderUseCase>().UpdateBilling() }
    single<BuyOrderUseCase.GetPaymentsRegister> { get<BuyOrderUseCase>().GetPaymentsRegister() }
    single<GetFactoriesListUseCase> { get<GetFactoriesListUseCase>() }

    single<PaymentConditionsUseCase.GetPaymentsConditions> { get<PaymentConditionsUseCase>().GetPaymentsConditions() }
    single<PaymentConditionsUseCase.SetPaymentsConditionsFactory> { get<PaymentConditionsUseCase>().SetPaymentsConditionsFactory() }

    single<GetClientsUseCase> { get<GetClientsUseCase>() }
}

val dataModule = module {
//services
    factoryOf(::OrderService)
    factoryOf(::HomeService)
    factoryOf(::ClientService)
//repositories
    factoryOf(::OrderRepository)
    factoryOf(::HomeRepository)
// use cases
    factoryOf(::OrdersUseCase)
    factoryOf(::BuyOrderUseCase)
    factoryOf(::HomeUseCase)
    factoryOf(::PaymentConditionsUseCase)
    factoryOf(::GetFactoriesListUseCase)
    factoryOf(::GetClientsUseCase)
}

val viewModelModule = module {
    viewModelOf(::OrdersViewModel)
    viewModelOf(::BuyOrdersViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::ClientsViewModel)
}

//expect val nativeModule: Module

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(appModule, dataModule, viewModelModule)
    }
}
