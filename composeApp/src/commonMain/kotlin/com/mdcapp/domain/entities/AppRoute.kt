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
    data object CreateOrder : AppRoute("CreateOrder")
    data object Factories : AppRoute("Factories")

    data class AddInvoice(val orderId: String) : AppRoute("AddInvoice") {
        companion object {
            const val BASE_ROUTE = "AddInvoice/{orderId}"
            fun createRoute(orderId: String) = "AddInvoice/$orderId"
        }
    }

    data object Clients : AppRoute("Clients")
    data object InvoicesPaged : AppRoute("InvoicesPaged")


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

    data class OrderDetail(val orderId: String, val factoryName: String) :
        AppRoute("OrderDetail/$orderId/$factoryName") {
        companion object {
            const val BASE_ROUTE = "OrderDetail/{orderId}/{factoryName}"
            fun createRoute(orderId: String, factoryName: String) =
                "OrderDetail/$orderId/$factoryName"
        }
    }
}
