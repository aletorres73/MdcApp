package com.mdcapp.domain.usescases.ordersusescases

import com.mdcapp.data.model.OrderModel
import com.mdcapp.domain.remote.OrderRepository

class OrdersUseCase(private val repository: OrderRepository) {
    inner class GetAllOrders {
        suspend operator fun invoke(): List<OrderModel> {
            return repository.getAllOrders()
        }
    }

    inner class GetOrdersByFactory {
        suspend operator fun invoke(name: String): List<OrderModel> {
            return repository.getOrdersByFactory(name)
        }
    }
}