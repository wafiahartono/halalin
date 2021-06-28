package com.halalin.service.repository

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.halalin.service.model.Service
import kotlinx.coroutines.tasks.await

object FirebaseServiceRepository : ServiceRepository {
    private var serviceList: List<Service>? = null

    private suspend fun refreshServiceList() {
        serviceList = Firebase.firestore.collection("services")
            .orderBy("name")
            .get().await()
            .documents.map {
                Service(
                    iconUrl = it.getString("icon_url"),
                    id = it.id,
                    imageUrl = it.getString("image_url"),
                    name = it.getString("name")
                )
            }
    }

    override suspend fun getServiceList(): List<Service> {
        if (serviceList == null) refreshServiceList()
        return serviceList!!
    }

    override suspend fun getService(id: String): Service? {
        if (serviceList == null) refreshServiceList()
        return serviceList!!.find { it.id == id }
    }
}
