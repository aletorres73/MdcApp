package com.mdcapp.domain.usescases

import com.mdcapp.data.model.OrderModel
import com.mdcapp.domain.remote.OrderRepository

class OrdersUseCase(private val repository: OrderRepository) {
    inner class GetAllOrders {
        suspend operator fun invoke(): List<OrderModel> {
            return repository.getAllOrders()
        }
    }
}