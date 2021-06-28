package com.halalin.profile.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.halalin.auth.repository.AuthRepository
import com.halalin.auth.repository.FirebaseAuthRepository
import com.halalin.cart.model.Item
import com.halalin.util.logd
import com.halalin.util.logw
import com.halalin.vendor.model.Vendor
import kotlinx.coroutines.tasks.await

object FirebaseUserRepository : UserRepository {
    private val authRepository: AuthRepository = FirebaseAuthRepository

    private val _cartItemList = MutableLiveData<List<Item>>()
    override val cartItemList: LiveData<List<Item>> get() = _cartItemList

    override suspend fun addItemToCart(item: Item) {
        val foundItem = _cartItemList.value?.find {
            it.product!!.id == item.product!!.id && it.vendor!!.id == item.vendor!!.id
        }
        if (foundItem != null) {
            updateCartItemQuantity(foundItem.id!!, foundItem.quantity!! + item.quantity!!)
            return
        }

        Firebase.firestore
            .collection("users").document(authRepository.user!!.id!!)
            .collection("cart_items").document()
            .set(
                hashMapOf(
                    "product" to hashMapOf("id" to item.product!!.id),
                    "quantity" to item.quantity,
                    "vendor" to hashMapOf("id" to item.vendor!!.id)
                )
            ).await()
    }

    override suspend fun updateCartItemQuantity(itemId: String, quantity: Int) {
        Firebase.firestore
            .collection("users").document(authRepository.user!!.id!!)
            .collection("cart_items").document(itemId)
            .update("quantity", quantity).await()
    }

    override suspend fun removeItemFromCart(itemId: String) {
        Firebase.firestore
            .collection("users").document(authRepository.user!!.id!!)
            .collection("cart_items").document(itemId)
            .delete().await()
    }

    private val _favoriteVendorList = MutableLiveData<List<Vendor>>()
    override val favoriteVendorList: LiveData<List<Vendor>> get() = _favoriteVendorList

    override suspend fun addVendorToFavorite(vendorId: String) {
        Firebase.firestore
            .collection("users").document(authRepository.user!!.id!!)
            .collection("favorite_vendors").document()
            .set(hashMapOf("vendor" to hashMapOf("id" to vendorId))).await()
    }

    override suspend fun isVendorFavorite(vendorId: String) =
        _favoriteVendorList.value?.indexOfFirst { it.id == vendorId } != -1

    override suspend fun removeVendorFromFavorite(vendorId: String) {
        Firebase.firestore
            .collection("users").document(authRepository.user!!.id!!)
            .collection("favorite_vendors")
            .whereEqualTo("vendor.id", vendorId)
            .limit(1).get().await()
            .documents.forEach { it.reference.delete().await() }
    }

    private lateinit var cartItemListenerRegistration: ListenerRegistration
    private lateinit var favoriteVendorListenerRegistration: ListenerRegistration

    override fun startListening() {
        logd("start listening to firestore. ${Firebase.firestore}")
        cartItemListenerRegistration = Firebase.firestore
            .collection("users").document(authRepository.user!!.id!!)
            .collection("cart_items")
            .addSnapshotListener { query, e ->
                if (e != null) {
                    logw("Add snapshot listener to cart item failed", e)
                    return@addSnapshotListener
                }

                if (query != null) {
                    logd("before _cartItemList.value = \n${_cartItemList.value}")
                    _cartItemList.value = query.map { doc ->
                        Item(
                            id = doc.id,
                            product = com.halalin.vendor.product.model.Product(
                                id = doc.getString("product.id"),
                                imageUrl = doc.getString("product.image_url"),
                                name = doc.getString("product.name"),
                                price = doc.getDouble("product.price")!!.toFloat()
                            ),
                            quantity = doc.getLong("quantity")!!.toInt(),
                            vendor = Vendor(
                                id = doc.getString("vendor.id"),
                                name = doc.getString("vendor.name")
                            )
                        )
                    }.plus(_cartItemList.value ?: emptyList())
                    logd("after _cartItemList.value = \n${_cartItemList.value}")
                }
            }

        favoriteVendorListenerRegistration = Firebase.firestore
            .collection("users").document(authRepository.user!!.id!!)
            .collection("favorite_vendors")
            .addSnapshotListener { query, e ->
                if (e != null) {
                    logw("Add snapshot listener to favorite vendor failed", e)
                    return@addSnapshotListener
                }

                if (query != null) {
                    logd("before _favoriteVendorList.value = \n${_favoriteVendorList.value}")
                    _favoriteVendorList.value = query.map { doc ->
                        Vendor(
                            id = doc.getString("vendor.id"),
                            location = doc.getString("vendor.location"),
                            logoUrl = doc.getString("vendor.logo_url"),
                            name = doc.getString("vendor.name"),
                            rating = doc.getDouble("vendor.rating")?.toFloat() ?: 0F,
                            reviewNumber = doc.getLong("vendor.review_number")?.toInt() ?: 0
                        )
                    }.plus(_favoriteVendorList.value ?: emptyList())
                    logd("after _favoriteVendorList.value = \n${_favoriteVendorList.value}")
                }
            }
    }

    override fun stopListening() {
        logd("stop listening to firestore")
        cartItemListenerRegistration.remove()
        favoriteVendorListenerRegistration.remove()
    }
}
