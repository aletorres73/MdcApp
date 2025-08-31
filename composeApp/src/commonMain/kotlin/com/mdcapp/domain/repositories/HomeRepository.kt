package com.mdcapp.domain.repositories

import com.mdcapp.data.model.FactoryModel
import com.mdcapp.data.remote.toDomain
import com.mdcapp.data.service.HomeService

class HomeRepository(private val service: HomeService) {
    suspend fun getFactoriesList(): List<FactoryModel> {
        return service.fetchAllFactories().map { it.toDomain() }
    }
}