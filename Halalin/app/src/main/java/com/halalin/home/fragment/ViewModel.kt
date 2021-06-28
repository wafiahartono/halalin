package com.halalin.home.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halalin.service.model.Service
import com.halalin.service.repository.FirebaseServiceRepository
import com.halalin.service.repository.ServiceRepository
import com.halalin.util.Resource
import com.halalin.util.loge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ViewModel : ViewModel() {
    private val serviceRepository: ServiceRepository = FirebaseServiceRepository

    private val _serviceList = MutableLiveData<Resource<List<Service>>>()
    val serviceList: LiveData<Resource<List<Service>>> = _serviceList

    fun fetchServiceList() = viewModelScope.launch(Dispatchers.IO) {
        _serviceList.postValue(Resource.Loading())
        _serviceList.postValue(
            try {
                Resource.Success(serviceRepository.getServiceList())
            } catch (e: Exception) {
                loge("Fetch service list failed", e)
                Resource.Failure()
            }
        )
    }
}
