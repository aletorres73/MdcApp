package com.mdcapp.domain.usescases

import com.mdcapp.data.model.Order
import com.mdcapp.domain.remote.OrderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class GetAllOrdersUseCase(private val repository: OrderRepository) {

    suspend operator fun invoke(): List<Order> {
        return repository.getAllOrders()
    }
}