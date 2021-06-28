package com.halalin.profile.repository

import androidx.lifecycle.LiveData
import com.halalin.cart.model.Item
import com.halalin.util.ListenableRepository
import com.halalin.vendor.model.Vendor

interface UserRepository : ListenableRepository {
    val cartItemList: LiveData<List<Item>>
    suspend fun addItemToCart(item: Item)
    suspend fun updateCartItemQuantity(itemId: String, quantity: Int)
    suspend fun removeItemFromCart(itemId: String)

    val favoriteVendorList: LiveData<List<Vendor>>
    suspend fun addVendorToFavorite(vendorId: String)
    suspend fun isVendorFavorite(vendorId: String): Boolean
    suspend fun removeVendorFromFavorite(vendorId: String)
}
