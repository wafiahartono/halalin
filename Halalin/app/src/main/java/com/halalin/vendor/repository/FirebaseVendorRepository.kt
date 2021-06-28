package com.halalin.vendor.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.halalin.auth.model.User
import com.halalin.service.model.Service
import com.halalin.util.logd
import com.halalin.vendor.model.SearchFilter
import com.halalin.vendor.model.Vendor
import com.halalin.vendor.model.Vendor.Contact
import com.halalin.vendor.model.Vendor.PriceRange
import com.halalin.vendor.product.model.Product
import com.halalin.vendor.review.model.Review
import kotlinx.coroutines.tasks.await

object FirebaseVendorRepository : VendorRepository {
    override suspend fun searchVendor(searchFilter: SearchFilter): List<Vendor> {
        var query = Firebase.firestore.collection("vendor_search_snippets") as Query
        if (searchFilter.serviceId != null) {
            query = query.whereArrayContains("services", searchFilter.serviceId)
        }
        if (searchFilter.sort != null) {
            query = query.orderBy(
                when (searchFilter.sort.field) {
                    SearchFilter.Sort.Field.LOCATION -> "location"
                    SearchFilter.Sort.Field.NAME -> "name"
                    SearchFilter.Sort.Field.RATING -> "rating"
                    SearchFilter.Sort.Field.REVIEW_NUMBER -> "review_number"
                },
                when (searchFilter.sort.order) {
                    SearchFilter.Sort.Order.ASC -> Query.Direction.ASCENDING
                    SearchFilter.Sort.Order.DESC -> Query.Direction.DESCENDING
                }
            )
        }
        return query.get().await().documents.map { doc ->
            Vendor(
                id = doc.id,
                location = doc.getString("location"),
                logoUrl = doc.getString("logo_url"),
                name = doc.getString("name"),
                rating = doc.getDouble("rating")?.toFloat() ?: 0F,
                reviewNumber = doc.getLong("review_number")?.toInt() ?: 0,
                serviceList = (doc.get("services") as List<*>).map { serviceId ->
                    Service(serviceId.toString())
                }
            )
        }
    }

    override suspend fun getVendor(id: String): Vendor? {
        val doc = Firebase.firestore.collection("vendors").document(id).get().await()
        return if (doc.exists()) Vendor(
            contacts = (doc.get("contacts") as Map<*, *>).entries.associate { field ->
                logd("contacts entries associate: $field")
                Contact.valueOf(field.key.toString().toUpperCase()) to field.value.toString()
            },
            description = doc.getString("description"),
            id = doc.id,
            latestReviewList = (doc.get("latest_reviews") as? Map<*, *>)
                ?.filterKeys { key -> key?.toString()?.toIntOrNull() != null }
                ?.values?.map { review ->
                    review as Map<*, *>
                    val userMap = (review["user"] as Map<*, *>).entries.associate { field ->
                        field.key.toString() to field.value.toString()
                    }
                    return@map Review(
                        comment = review["comment"].toString(),
                        date = (review["date_and_time"] as Timestamp).toDate(),
                        id = review["id"].toString(),
                        rating = review["rating"].toString().toInt(),
                        user = User(
                            displayName = userMap["display_name"].toString(),
                            id = userMap["id"].toString(),
                            profilePictureUrl = userMap["display_picture_url"].toString()
                        )
                    )
                }
                ?.sortedByDescending { review -> review.date!!.time },
            location = doc.getString("location"),
            logoUrl = doc.getString("logo_url"),
            name = doc.getString("name"),
            priceRange = doc.getString("price_range")?.let { PriceRange.valueOf(it) },
            rating = doc.getDouble("rating")?.toFloat() ?: 0F,
            reviewNumber = doc.getLong("review_number")?.toInt() ?: 0,
            serviceList = (doc.get("services") as List<*>).map { serviceId -> Service(id = serviceId.toString()) }
        ) else null
    }

    override suspend fun getProductList(vendorId: String): List<Product> {
        return Firebase.firestore
            .collection("vendors").document(vendorId)
            .collection("products")
            .orderBy("name")
            .get().await()
            .documents.map { doc ->
                Product(
                    description = doc.getString("description"),
                    id = doc.id,
                    imageUrl = doc.getString("image_url"),
                    name = doc.getString("name"),
                    price = doc.getDouble("price")!!.toFloat()
                )
            }
    }

    override suspend fun getReviewList(vendorId: String): List<Review> {
        return Firebase.firestore
            .collection("vendors").document(vendorId)
            .collection("reviews")
            .orderBy("date_and_time", Query.Direction.DESCENDING)
            .get().await()
            .documents.map { doc ->
                Review(
                    comment = doc.get("comment").toString(),
                    date = (doc.get("date_and_time") as Timestamp).toDate(),
                    id = doc.id,
                    rating = doc.get("rating").toString().toInt(),
                    user = User(
                        displayName = doc.get("user.display_name").toString(),
                        id = doc.get("user.id").toString(),
                        profilePictureUrl = doc.get("user.display_picture_url").toString()
                    )
                )
            }
    }
}
