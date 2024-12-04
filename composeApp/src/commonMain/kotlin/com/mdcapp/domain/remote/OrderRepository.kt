package com.mdcapp.domain.remote

import com.mdcapp.data.model.OrderModel
import com.mdcapp.data.remote.toDomain
import com.mdcapp.domain.service.OrderService

class OrderRepository(private val service: OrderService) {
    suspend fun getAllOrders(): List<OrderModel> {
        return service.fetchAllOrders().map { it.toDomain() }
    }
}