package com.mdcapp.domain.remote

import com.mdcapp.data.model.FactoryModel
import com.mdcapp.data.remote.toDomain
import com.mdcapp.domain.service.HomeService

class HomeRepository(private val service: HomeService) {
    suspend fun getFactoriesList(): List<FactoryModel> {
        return service.fetchAllFactories().map { it.toDomain() }
    }
}