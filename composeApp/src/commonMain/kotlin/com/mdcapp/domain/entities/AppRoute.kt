package com.mdcapp.domain.entities

sealed class AppRoute(val route: String) {

    data object Home : AppRoute("Home")
    data object Login : AppRoute("Login")
    data object SignUp : AppRoute("SignUp")
    data class AddClient(val id: String? = null, val name: String? = null) : AppRoute("AddClient") {
        companion object {
            const val BASE_ROUTE = "AddClient?id={id}&name={name}"
            fun createRoute(id: String? = null, name: String? = null): String {
                return if (id != null && name != null) "AddClient?id=$id&name=$name" else "AddClient"
            }
        }
    }
    data class CreateOrder(val clientId: String? = null, val orderId: String? = null) :
        AppRoute("CreateOrder") {
        companion object {
            const val BASE_ROUTE = "CreateOrder?clientId={clientId}&orderId={orderId}"
            fun createRoute(clientId: String? = null, orderId: String? = null): String {
                return when {
                    clientId != null && orderId != null -> "CreateOrder?clientId=$clientId&orderId=$orderId"
                    clientId != null -> "CreateOrder?clientId=$clientId"
                    orderId != null -> "CreateOrder?orderId=$orderId"
                    else -> "CreateOrder"
                }
            }
        }
    }
    data object Factories : AppRoute("Factories")

    data class AddInvoice(val clientId: String, val orderId: String) : AppRoute("AddInvoice") {
        companion object {
            const val BASE_ROUTE = "AddInvoice/{clientId}/{orderId}"
            fun createRoute(clientId: String, orderId: String) = "AddInvoice/$clientId/$orderId"
        }
    }

    data object Clients : AppRoute("Clients")

    data class ClientOrders(val clientId: String) : AppRoute("ClientOrders") {
        companion object {
            const val BASE_ROUTE = "ClientOrders/{clientId}"
            fun createRoute(clientId: String) = "ClientOrders/$clientId"
        }
    }

    data object InvoicesPaged : AppRoute("InvoicesPaged")
    data object Commissions : AppRoute("Commissions")
    data object Agenda : AppRoute("Agenda")
    data object Profile : AppRoute("Profile")
    data object SubscriptionStatus : AppRoute("SubscriptionStatus")

    data class Invoices(val clientId: String) : AppRoute("Invoices") {
        companion object {
            const val BASE_ROUTE = "Invoices/{clientId}"
            fun createRoute(clientId: String) = "Invoices/$clientId"
        }
    }

    data class DetailInvoice(val invoiceNumber: String) :
        AppRoute("DetailInvoice/$invoiceNumber") {
        companion object {
            const val BASE_ROUTE = "DetailInvoice/{invoiceNumber}"
            fun createRoute(invoiceNumber: String) = "DetailInvoice/$invoiceNumber"
        }
    }

    data class Orders(val factoryName: String) : AppRoute("Orders/$factoryName") {
        companion object {
            const val BASE_ROUTE = "Orders/{factoryName}"
            fun createRoute(factoryName: String) = "Orders/$factoryName"
        }
    }

    data class OrderDetail(val clientId: String, val orderId: String, val factoryName: String) :
        AppRoute("OrderDetail/$clientId/$orderId/$factoryName") {
        companion object {
            const val BASE_ROUTE = "OrderDetail/{clientId}/{orderId}/{factoryName}"
            fun createRoute(clientId: String, orderId: String, factoryName: String) =
                "OrderDetail/$clientId/$orderId/$factoryName"
        }
    }
}

