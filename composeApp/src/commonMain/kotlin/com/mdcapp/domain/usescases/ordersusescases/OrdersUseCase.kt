package com.mdcapp.domain.usescases.ordersusescases

import com.mdcapp.data.model.OrderModel
import com.mdcapp.domain.repositories.OrderRepository
import kotlinx.coroutines.flow.Flow

class OrdersUseCase(private val repository: OrderRepository) {
    inner class GetAllOrders {
        suspend operator fun invoke(): List<OrderModel> {
            return repository.getAllOrders()
        }
    }

    inner class ObserveAllOrders {
        operator fun invoke(): Flow<List<OrderModel>> {
            return repository.observeAllOrders()
        }
    }

    inner class GetOrdersByFactory {
        suspend operator fun invoke(name: String): List<OrderModel> {
            return repository.getOrdersByFactory(name)
        }
    }

    inner class ObserveOrdersByFactory {
        operator fun invoke(name: String): Flow<List<OrderModel>> {
            return repository.observeOrdersByFactory(name)
        }
    }

    inner class GetOrderBranch {
        suspend operator fun invoke(clientId: String, orderId: String): String {
            return repository.getOrderBranch(clientId, orderId)
        }
    }
}