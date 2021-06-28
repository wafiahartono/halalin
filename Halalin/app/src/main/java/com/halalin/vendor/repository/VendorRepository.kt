package com.halalin.vendor.repository

import com.halalin.vendor.model.SearchFilter
import com.halalin.vendor.model.Vendor
import com.halalin.vendor.product.model.Product
import com.halalin.vendor.review.model.Review

interface VendorRepository {
    suspend fun searchVendor(searchFilter: SearchFilter): List<Vendor>
    suspend fun getVendor(id: String): Vendor?
    suspend fun getProductList(vendorId: String): List<Product>
    suspend fun getReviewList(vendorId: String): List<Review>
}
