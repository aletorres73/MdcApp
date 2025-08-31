package com.mdcapp.domain.usescases.ordersusescases

import com.mdcapp.data.model.OrderModel
import com.mdcapp.domain.repositories.OrderRepository

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

    inner class GetOrderBranch {
        suspend operator fun invoke(orderId: String): String {
            return repository.getOrderBranch(orderId)
        }
    }
}