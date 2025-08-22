package com.mdcapp.domain.entities

sealed class AppRoute(val route: String) {

    data object Home : AppRoute("Home")
    data object Clients : AppRoute("Clients")


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
