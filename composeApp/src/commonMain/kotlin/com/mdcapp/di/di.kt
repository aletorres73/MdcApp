package com.mdcapp.di

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.mdcapp.data.service.AuthService
import com.mdcapp.data.service.BillingPaginationService
import com.mdcapp.data.service.ClientService
import com.mdcapp.data.service.FirebaseAnalyticsService
import com.mdcapp.data.service.HomeService
import com.mdcapp.data.service.InitService
import com.mdcapp.data.service.OrderService
import com.mdcapp.data.service.UserService
import com.mdcapp.domain.repositories.AuthRepository
import com.mdcapp.domain.repositories.HomeRepository
import com.mdcapp.domain.repositories.OrderRepository
import com.mdcapp.domain.service.AnalyticsService
import com.mdcapp.domain.usescases.InitConfigUseCase
import com.mdcapp.domain.usescases.clientsusecase.GetClientsUseCase
import com.mdcapp.domain.usescases.homeusescases.HomeUseCase
import com.mdcapp.domain.usescases.homeusescases.PaymentConditionsUseCase
import com.mdcapp.domain.usescases.invoiceusecase.InvoiceUseCase
import com.mdcapp.domain.usescases.ordersusescases.BuyOrderUseCase
import com.mdcapp.domain.usescases.ordersusescases.GetFactoriesListUseCase
import com.mdcapp.domain.usescases.ordersusescases.OrdersUseCase
import com.mdcapp.ui.viewmodels.AddClientViewModel
import com.mdcapp.ui.viewmodels.AgendaViewModel
import com.mdcapp.ui.viewmodels.ClientsViewModel
import com.mdcapp.ui.viewmodels.CommissionsViewModel
import com.mdcapp.ui.viewmodels.FactoryViewModel
import com.mdcapp.ui.viewmodels.HomeViewModel
import com.mdcapp.ui.viewmodels.LoginViewModel
import com.mdcapp.ui.viewmodels.ProfileViewModel
import com.mdcapp.ui.viewmodels.SignUpViewModel
import com.mdcapp.ui.viewmodels.SubscriptionViewModel
import com.mdcapp.ui.viewmodels.buyorders.BuyOrdersViewModel
import com.mdcapp.ui.viewmodels.invoices.AddInvoiceViewModel
import com.mdcapp.ui.viewmodels.invoices.DetailInvoiceViewModel
import com.mdcapp.ui.viewmodels.invoices.InvoicesPagedViewModel
import com.mdcapp.ui.viewmodels.invoices.InvoicesViewModel
import com.mdcapp.ui.viewmodels.orders.ClientOrdersViewModel
import com.mdcapp.ui.viewmodels.orders.CreateOrderViewModel
import com.mdcapp.ui.viewmodels.orders.OrdersViewModel
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.storage.storage
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule = module {
    single<FirebaseFirestore> { Firebase.firestore }
    single<dev.gitlive.firebase.firestore.FirebaseFirestore> { dev.gitlive.firebase.Firebase.firestore }
    single<dev.gitlive.firebase.storage.FirebaseStorage> { dev.gitlive.firebase.Firebase.storage }
    single<FirebaseAuth> { dev.gitlive.firebase.Firebase.auth }

    single<HomeUseCase.GetAllFactories> { get<HomeUseCase>().GetAllFactories() }

    single<OrdersUseCase.GetAllOrders> { get<OrdersUseCase>().GetAllOrders() }
    single<OrdersUseCase.ObserveAllOrders> { get<OrdersUseCase>().ObserveAllOrders() }
    single<OrdersUseCase.GetOrdersByFactory> { get<OrdersUseCase>().GetOrdersByFactory() }
    single<OrdersUseCase.ObserveOrdersByFactory> { get<OrdersUseCase>().ObserveOrdersByFactory() }
    single<OrdersUseCase.GetOrderBranch> { get<OrdersUseCase>().GetOrderBranch() }

    single<BuyOrderUseCase.GetBuyOrderById> { get<BuyOrderUseCase>().GetBuyOrderById() }
    single<BuyOrderUseCase.GetBuyOrdersByClient> { get<BuyOrderUseCase>().GetBuyOrdersByClient() }
    single<BuyOrderUseCase.ObserveBuyOrdersByClient> { get<BuyOrderUseCase>().ObserveBuyOrdersByClient() }
    single<BuyOrderUseCase.GetBillings> { get<BuyOrderUseCase>().GetBillings() }
    single<BuyOrderUseCase.AddPaymentToRegister> { get<BuyOrderUseCase>().AddPaymentToRegister() }
    single<BuyOrderUseCase.GetLastIdPaymentFromRegister> { get<BuyOrderUseCase>().GetLastIdPaymentFromRegister() }
    single<BuyOrderUseCase.UpdateBilling> { get<BuyOrderUseCase>().UpdateBilling() }
    single<BuyOrderUseCase.GetPaymentsRegister> { get<BuyOrderUseCase>().GetPaymentsRegister() }
    single<BuyOrderUseCase.SaveOrder> { get<BuyOrderUseCase>().SaveOrder() }
    single<GetFactoriesListUseCase> { get<GetFactoriesListUseCase>() }

    single<PaymentConditionsUseCase.GetPaymentsConditions> { get<PaymentConditionsUseCase>().GetPaymentsConditions() }
    single<PaymentConditionsUseCase.SetPaymentsConditionsFactory> { get<PaymentConditionsUseCase>().SetPaymentsConditionsFactory() }

    single<GetClientsUseCase> { get<GetClientsUseCase>() }

    single<InvoiceUseCase.GetBillingsByClient> { get<InvoiceUseCase>().GetBillingsByClient() }
    single<InvoiceUseCase.GetClientName> { get<InvoiceUseCase>().GetClientName() }
    single<InvoiceUseCase.FilterByBrand> { get<InvoiceUseCase>().FilterByBrand() }
    single<InvoiceUseCase.GetInvoiceByNumber> { get<InvoiceUseCase>().GetInvoiceByNumber() }
    single<InvoiceUseCase.ObserveInvoice> { get<InvoiceUseCase>().ObserveInvoice() }
    single<InvoiceUseCase.ObserveBillingsByClient> { get<InvoiceUseCase>().ObserveBillingsByClient() }
    single<InvoiceUseCase.ObservePaymentsByInvoice> { get<InvoiceUseCase>().ObservePaymentsByInvoice() }
    single<InvoiceUseCase.ObservePaymentsByClient> { get<InvoiceUseCase>().ObservePaymentsByClient() }
    single<InvoiceUseCase.ObserveAllBillings> { get<InvoiceUseCase>().ObserveAllBillings() }
    single<InvoiceUseCase.ObserveAllPayments> { get<InvoiceUseCase>().ObserveAllPayments() }
    single<InvoiceUseCase.GetPaymentCondition> { get<InvoiceUseCase>().GetPaymentCondition() }
    single<InvoiceUseCase.GetInvoicePaged> { get<InvoiceUseCase>().GetInvoicePaged() }
    single<InvoiceUseCase.GetAllClients> { get<InvoiceUseCase>().GetAllClients() }
    single<InitConfigUseCase> { get<InitConfigUseCase>() }
    single<InvoiceUseCase.UpdateInvoice> { get<InvoiceUseCase>().UpdateInvoice() }
    single<InvoiceUseCase.CreateInvoice> { get<InvoiceUseCase>().CreateInvoice() }
    single<InvoiceUseCase.DeleteInvoice> { get<InvoiceUseCase>().DeleteInvoice() }
}


val dataModule = module {
//services
    single<AnalyticsService> { FirebaseAnalyticsService() }
    factoryOf(::InitService)
    factory { OrderService(get(), get()) }
    factory { HomeService(get(), get()) }
    factory { ClientService(get(), get()) }
    factory { BillingPaginationService(get(), get()) }
    factoryOf(::AuthService)
    factoryOf(::UserService)
//repositories
    factoryOf(::OrderRepository)
    factoryOf(::HomeRepository)
    factoryOf(::AuthRepository)
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
    viewModelOf(::CreateOrderViewModel)
    viewModel { (clientId: String) -> ClientOrdersViewModel(clientId, get(), get()) }
    viewModelOf(::BuyOrdersViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::ClientsViewModel)
    viewModelOf(::FactoryViewModel)
    viewModelOf(::AgendaViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::SignUpViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::AddClientViewModel)
    viewModelOf(::CommissionsViewModel)
    viewModelOf(::SubscriptionViewModel)
//    viewModelOf(::InvoicesViewModel)
    viewModel { (clientId: String) ->
        InvoicesViewModel(
            clientId,
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel { (orderId: String) ->
        AddInvoiceViewModel(
            orderId,
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel { (invoiceNumber: String) ->
        DetailInvoiceViewModel(
            invoiceNumber,
            get(),
            get(),
            get(),
            get(),
            get(),
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

