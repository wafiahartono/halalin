package com.halalin.vendor.product.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halalin.cart.model.Item
import com.halalin.profile.repository.FirebaseUserRepository
import com.halalin.profile.repository.UserRepository
import com.halalin.util.ClearableViewModel
import com.halalin.util.Resource
import com.halalin.util.Event
import com.halalin.util.loge
import com.halalin.vendor.model.Vendor
import com.halalin.vendor.product.model.Product
import com.halalin.vendor.repository.FirebaseVendorRepository
import com.halalin.vendor.repository.VendorRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ViewModel : ViewModel(), ClearableViewModel {
    private val vendorRepository: VendorRepository = FirebaseVendorRepository
    private val userRepository: UserRepository = FirebaseUserRepository

    private var vendorId: String? = null

    private val _productList = MutableLiveData<Resource<List<Product>>?>()
    val productList: LiveData<Resource<List<Product>>?> = _productList

    private val _addItemToCartResult = MutableLiveData<Event<Resource<Unit>>>()
    val addItemToCartResult: LiveData<Event<Resource<Unit>>> = _addItemToCartResult

    fun fetchProductList(vendorId: String) = viewModelScope.launch(Dispatchers.IO) {
        this@ViewModel.vendorId = vendorId

        _productList.postValue(Resource.Loading())
        _productList.postValue(
            try {
                Resource.Success(vendorRepository.getProductList(vendorId))
            } catch (e: Exception) {
                loge("Fetch product list failed", e)
                Resource.Failure()
            }
        )
    }

    fun refreshFetchProductList() {
        fetchProductList(vendorId!!)
    }

    fun addItemToCart(item: Item) = viewModelScope.launch(Dispatchers.IO) {
        _addItemToCartResult.postValue(
            Event(
                try {
                    userRepository.addItemToCart(item.copy(vendor = Vendor(id = vendorId)))
                    Resource.Success(Unit)
                } catch (e: Exception) {
                    loge("Add item to cart failed", e)
                    Resource.Failure()
                }
            )
        )
    }

    override fun clearResources() {
        vendorId = null
        _productList.value = null
    }
}
