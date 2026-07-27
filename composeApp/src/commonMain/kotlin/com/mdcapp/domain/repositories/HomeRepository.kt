package com.mdcapp.domain.repositories

import com.mdcapp.data.remote.toFactoryDomain
import com.mdcapp.data.service.HomeService
import com.mdcapp.domain.entities.FactoryModel

class HomeRepository(private val service: HomeService) {
    suspend fun getFactoriesList(): List<FactoryModel> {
        return service.fetchAllFactories().map { it.toFactoryDomain() }
    }
}