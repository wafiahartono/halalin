package com.halalin.vendor.fragment.search

import androidx.lifecycle.*
import androidx.lifecycle.ViewModel
import com.halalin.service.model.Service
import com.halalin.service.repository.FirebaseServiceRepository
import com.halalin.service.repository.ServiceRepository
import com.halalin.util.Resource
import com.halalin.util.loge
import com.halalin.vendor.model.SearchFilter
import com.halalin.vendor.repository.FirebaseVendorRepository
import com.halalin.vendor.repository.VendorRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ViewModel : ViewModel() {
    private val serviceRepository: ServiceRepository = FirebaseServiceRepository
    private val vendorRepository: VendorRepository = FirebaseVendorRepository

    private val _serviceList = MutableLiveData<Resource<List<Service>>>()
    val serviceList: LiveData<Resource<List<Service>>> = _serviceList

    private val searchFilter = MutableLiveData<SearchFilter>()

    val vendorList = searchFilter.switchMap {
        return@switchMap liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
            emit(Resource.Loading())
            emit(
                try {
                    Resource.Success(vendorRepository.searchVendor(it))
                } catch (e: Exception) {
                    loge("Search vendor failed", e)
                    Resource.Failure()
                }
            )
        }
    }

    fun fetchServiceList() = viewModelScope.launch(Dispatchers.IO) {
        _serviceList.postValue(
            try {
                Resource.Success(serviceRepository.getServiceList())
            } catch (e: Exception) {
                loge("Fetch service list failed", e)
                Resource.Failure()
            }
        )
    }

    fun getSearchFilter(): SearchFilter? {
        return searchFilter.value
    }

    fun searchVendor(searchFilter: SearchFilter) {
        this.searchFilter.value = searchFilter
    }

    fun refreshSearchVendor() {
        searchVendor(searchFilter.value!!)
    }
}
