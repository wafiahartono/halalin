package com.halalin.vendor.fragment.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halalin.auth.repository.AuthRepository
import com.halalin.auth.repository.FirebaseAuthRepository
import com.halalin.profile.repository.FirebaseUserRepository
import com.halalin.profile.repository.UserRepository
import com.halalin.service.repository.FirebaseServiceRepository
import com.halalin.util.*
import com.halalin.vendor.model.Vendor
import com.halalin.vendor.repository.FirebaseVendorRepository
import com.halalin.vendor.repository.VendorRepository
import com.halalin.vendor.review.model.Review
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ViewModel : ViewModel(), ClearableViewModel {
    private val vendorRepository: VendorRepository = FirebaseVendorRepository
    private val userRepository: UserRepository = FirebaseUserRepository
    private val authRepository: AuthRepository = FirebaseAuthRepository

    private var vendorId: String? = null

    private val _vendor = MutableLiveData<Resource<Vendor?>?>()
    val vendor: LiveData<Resource<Vendor?>?> = _vendor

    private val _isFavorite = MutableLiveData<Resource<Boolean>?>()
    val isFavorite: LiveData<Resource<Boolean>?> = _isFavorite

    private val _setFavoriteResult = MutableLiveData<Event<Resource<Unit>>>()
    val setFavoriteResult: LiveData<Event<Resource<Unit>>> = _setFavoriteResult

    private val _reviewList = MutableLiveData<Resource<List<Review>>?>()
    val reviewList: LiveData<Resource<List<Review>>?> = _reviewList

    fun isGuest(): Boolean {
        return authRepository.isGuest
    }

    fun fetchVendor(vendorId: String) = viewModelScope.launch(Dispatchers.IO) {
        logd("fetchVendor($vendorId)")
        this@ViewModel.vendorId = vendorId

        _vendor.postValue(Resource.Loading())
        _vendor.postValue(
            try {
                val temp = vendorRepository.getVendor(vendorId)
                Resource.Success(
                    temp?.copy(serviceList = temp.serviceList!!.map {
                        FirebaseServiceRepository.getService(it.id!!)!!
                    })
                )
            } catch (e: Exception) {
                loge("Fetch vendor failed", e)
                Resource.Failure()
            }
        )

        _isFavorite.postValue(
            try {
                Resource.Success(userRepository.isVendorFavorite(vendorId))
            } catch (e: Exception) {
                loge("Fetch isFavorite failed", e)
                Resource.Failure()
            }
        )
    }

    fun refreshFetchVendor() {
        fetchVendor(vendorId!!)
    }

    fun setFavorite(add: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        _setFavoriteResult.postValue(
            Event(
                try {
                    if (add) userRepository.addVendorToFavorite(vendorId!!)
                    else userRepository.removeVendorFromFavorite(vendorId!!)
                    Resource.Success(Unit)
                } catch (e: Exception) {
                    loge("Set favorite failed", e)
                    Resource.Failure()
                }
            )
        )
    }

    fun fetchReviewList() = viewModelScope.launch(Dispatchers.IO) {
        _reviewList.postValue(Resource.Loading())
        _reviewList.postValue(
            try {
                Resource.Success(vendorRepository.getReviewList(vendorId!!))
            } catch (e: Exception) {
                loge("Fetch review list failed", e)
                Resource.Failure()
            }
        )
    }

    override fun clearResources() {
        logd("clearResources")
        vendorId = null
        _vendor.value = null
        _isFavorite.value = null
        _reviewList.value = null
    }
}
