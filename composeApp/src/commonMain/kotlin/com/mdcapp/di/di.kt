package com.mdcapp.di

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.mdcapp.data.service.BillingPaginationService
import com.mdcapp.data.service.ClientService
import com.mdcapp.data.service.HomeService
import com.mdcapp.data.service.InitService
import com.mdcapp.data.service.OrderService
import com.mdcapp.domain.repositories.HomeRepository
import com.mdcapp.domain.repositories.OrderRepository
import com.mdcapp.domain.usescases.InitConfigUseCase
import com.mdcapp.domain.usescases.clientsusecase.GetClientsUseCase
import com.mdcapp.domain.usescases.homeusescases.HomeUseCase
import com.mdcapp.domain.usescases.homeusescases.PaymentConditionsUseCase
import com.mdcapp.domain.usescases.invoiceusecase.InvoiceUseCase
import com.mdcapp.domain.usescases.ordersusescases.BuyOrderUseCase
import com.mdcapp.domain.usescases.ordersusescases.GetFactoriesListUseCase
import com.mdcapp.domain.usescases.ordersusescases.OrdersUseCase
import com.mdcapp.ui.viewmodels.ClientsViewModel
import com.mdcapp.ui.viewmodels.HomeViewModel
import com.mdcapp.ui.viewmodels.buyorders.BuyOrdersViewModel
import com.mdcapp.ui.viewmodels.invoices.DetailInvoiceViewModel
import com.mdcapp.ui.viewmodels.invoices.InvoicesPagedViewModel
import com.mdcapp.ui.viewmodels.invoices.InvoicesViewModel
import com.mdcapp.ui.viewmodels.orders.OrdersViewModel
import dev.gitlive.firebase.firestore.firestore
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule = module {
    single<FirebaseFirestore> { Firebase.firestore }
    single<dev.gitlive.firebase.firestore.FirebaseFirestore> { dev.gitlive.firebase.Firebase.firestore }

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

    single<InvoiceUseCase.GetBillingsByClient> { get<InvoiceUseCase>().GetBillingsByClient() }
    single<InvoiceUseCase.GetClientName> { get<InvoiceUseCase>().GetClientName() }
    single<InvoiceUseCase.FilterByBrand> { get<InvoiceUseCase>().FilterByBrand() }
    single<InvoiceUseCase.GetInvoiceByNumber> { get<InvoiceUseCase>().GetInvoiceByNumber() }
    single<InvoiceUseCase.GetPaymentCondition> { get<InvoiceUseCase>().GetPaymentCondition() }
    single<InvoiceUseCase.GetInvoicePaged> { get<InvoiceUseCase>().GetInvoicePaged() }
    single<InvoiceUseCase.GetAllClients> { get<InvoiceUseCase>().GetAllClients() }
    single<InitConfigUseCase> { get<InitConfigUseCase>() }
    single<InvoiceUseCase.UpdateInvoice> { get<InvoiceUseCase>().UpdateInvoice() }
}

val dataModule = module {
//services
    factoryOf(::InitService)
    factoryOf(::OrderService)
    factoryOf(::HomeService)
    factoryOf(::ClientService)
    factoryOf(::BillingPaginationService)
//repositories
    factoryOf(::OrderRepository)
    factoryOf(::HomeRepository)
// use cases
    factoryOf(::InitConfigUseCase)
    factoryOf(::OrdersUseCase)
    factoryOf(::BuyOrderUseCase)
    factoryOf(::HomeUseCase)
    factoryOf(::PaymentConditionsUseCase)
    factoryOf(::GetFactoriesListUseCase)
    factoryOf(::GetClientsUseCase)
    factoryOf(::InvoiceUseCase)
}

val viewModelModule = module {
    viewModelOf(::OrdersViewModel)
    viewModelOf(::BuyOrdersViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::ClientsViewModel)
//    viewModelOf(::InvoicesViewModel)
    viewModel { (clientId: String) -> InvoicesViewModel(clientId, get(), get(), get()) }
    viewModel { (invoiceNumber: String) ->
        DetailInvoiceViewModel(
            invoiceNumber,
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModelOf(::InvoicesPagedViewModel)
}

//expect val nativeModule: Module

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(appModule, dataModule, viewModelModule)
    }
}
