package com.halalin.service.repository

import com.halalin.service.model.Service

interface ServiceRepository {
    suspend fun getServiceList(): List<Service>
    suspend fun getService(id: String): Service?
}
